package com.pravinmali.orderprocessing.service;

import com.pravinmali.orderprocessing.entity.Order;
import com.pravinmali.orderprocessing.entity.OrderItem;
import com.pravinmali.orderprocessing.entity.OrderStatus;
import com.pravinmali.orderprocessing.exception.InvoiceNotEligibleException;
import com.pravinmali.orderprocessing.exception.OrderNotFoundException;
import com.pravinmali.orderprocessing.repository.OrderRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Handles PDF invoice generation for eligible orders using Apache PDFBox 3.x.
 *
 * <p>Invoice generation is only permitted for orders with status:
 * CONFIRMED, SHIPPED, or DELIVERED.</p>
 */
@Service
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    private static final Set<OrderStatus> INVOICE_ELIGIBLE_STATUSES =
            Set.of(OrderStatus.CONFIRMED, OrderStatus.SHIPPED, OrderStatus.DELIVERED);

    // Layout constants (points — PDFBox uses 1pt = 1/72 inch)
    private static final float PAGE_WIDTH  = PDRectangle.A4.getWidth();   // ~595pt
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();  // ~842pt
    private static final float MARGIN = 50f;
    private static final float CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;

    // Column X positions for line-item table
    private static final float COL_PRODUCT   = MARGIN;
    private static final float COL_QTY       = MARGIN + CONTENT_WIDTH * 0.50f;
    private static final float COL_UNIT      = MARGIN + CONTENT_WIDTH * 0.65f;
    private static final float COL_TOTAL     = MARGIN + CONTENT_WIDTH * 0.82f;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final OrderRepository orderRepository;

    public InvoiceService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // =========================================================================
    // PUBLIC API
    // =========================================================================

    @Transactional(readOnly = true)
    public byte[] generateInvoice(UUID orderId) throws IOException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!INVOICE_ELIGIBLE_STATUSES.contains(order.getStatus())) {
            throw new InvoiceNotEligibleException(order.getStatus());
        }

        log.debug("Generating invoice for order {}", orderId);
        return buildPdf(order);
    }

    // =========================================================================
    // PDF CONSTRUCTION
    // =========================================================================

    private byte[] buildPdf(Order order) throws IOException {
        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            // Fonts
            PDType1Font fontBold    = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font fontOblique = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                float y = PAGE_HEIGHT - MARGIN;

                // ----- HEADER ------------------------------------------------
                y = drawHeader(cs, fontBold, fontRegular, y, order);

                // ----- DIVIDER -----------------------------------------------
                y -= 10;
                drawHorizontalLine(cs, y, Color.DARK_GRAY);
                y -= 15;

                // ----- CUSTOMER INFO -----------------------------------------
                y = drawCustomerSection(cs, fontBold, fontRegular, y, order);
                y -= 20;

                // ----- LINE ITEMS TABLE --------------------------------------
                y = drawItemsTable(cs, fontBold, fontRegular, fontOblique, y, order.getItems());
                y -= 10;

                // ----- TOTALS ------------------------------------------------
                drawTotals(cs, fontBold, fontRegular, y, order);
            }

            doc.save(out);
            return out.toByteArray();
        }
    }

    // =========================================================================
    // SECTION RENDERERS
    // =========================================================================

    private float drawHeader(PDPageContentStream cs, PDType1Font bold, PDType1Font regular,
                             float y, Order order) throws IOException {
        // Company name
        drawText(cs, bold, 20, MARGIN, y, "Pravin Mali — Order Processing Co.");
        y -= 28;

        // "TAX INVOICE" title
        drawText(cs, bold, 16, MARGIN, y, "TAX INVOICE");
        y -= 20;

        // Order ID and date on the same line
        String orderId  = "Order ID : " + order.getId();
        String dateStr  = "Date     : " + (order.getCreatedAt() != null
                ? order.getCreatedAt().format(DATE_FMT) : "N/A");
        String statusStr = "Status   : " + order.getStatus();

        drawText(cs, regular, 9, MARGIN, y, orderId);
        y -= 13;
        drawText(cs, regular, 9, MARGIN, y, dateStr);
        y -= 13;
        drawText(cs, regular, 9, MARGIN, y, statusStr);

        return y;
    }

    private float drawCustomerSection(PDPageContentStream cs, PDType1Font bold, PDType1Font regular,
                                      float y, Order order) throws IOException {
        drawText(cs, bold, 10, MARGIN, y, "BILL TO");
        y -= 14;
        drawText(cs, regular, 9, MARGIN, y, order.getCustomer().getName());
        y -= 12;
        drawText(cs, regular, 9, MARGIN, y, order.getCustomer().getEmail());
        y -= 12;
        if (order.getCustomer().getPhone() != null) {
            drawText(cs, regular, 9, MARGIN, y, order.getCustomer().getPhone());
            y -= 12;
        }
        if (order.getCustomer().getAddress() != null) {
            drawText(cs, regular, 9, MARGIN, y, order.getCustomer().getAddress());
            y -= 12;
        }
        return y;
    }

    private float drawItemsTable(PDPageContentStream cs, PDType1Font bold, PDType1Font regular,
                                 PDType1Font oblique, float y, List<OrderItem> items) throws IOException {
        // Table header background
        drawFilledRect(cs, MARGIN, y - 2, CONTENT_WIDTH, 16, new Color(52, 73, 94));

        // Column headers
        drawColoredText(cs, bold, 9, COL_PRODUCT, y + 1, "PRODUCT", Color.WHITE);
        drawColoredText(cs, bold, 9, COL_QTY,     y + 1, "QTY", Color.WHITE);
        drawColoredText(cs, bold, 9, COL_UNIT,    y + 1, "UNIT PRICE", Color.WHITE);
        drawColoredText(cs, bold, 9, COL_TOTAL,   y + 1, "LINE TOTAL", Color.WHITE);
        y -= 18;

        // Rows
        boolean shaded = false;
        for (OrderItem item : items) {
            if (shaded) {
                drawFilledRect(cs, MARGIN, y - 2, CONTENT_WIDTH, 14, new Color(240, 240, 240));
            }
            drawText(cs, regular, 9, COL_PRODUCT, y, truncate(item.getProductName(), 35));
            drawText(cs, regular, 9, COL_QTY,     y, String.valueOf(item.getQuantity()));
            drawText(cs, regular, 9, COL_UNIT,    y, formatAmount(item.getUnitPrice()));
            drawText(cs, regular, 9, COL_TOTAL,   y, formatAmount(item.getLineTotal()));
            y -= 15;
            shaded = !shaded;
        }

        // Bottom border
        drawHorizontalLine(cs, y, Color.GRAY);
        return y;
    }

    private void drawTotals(PDPageContentStream cs, PDType1Font bold, PDType1Font regular,
                            float y, Order order) throws IOException {
        y -= 10;
        float labelX = MARGIN + CONTENT_WIDTH * 0.60f;
        float valueX = MARGIN + CONTENT_WIDTH * 0.82f;

        drawText(cs, regular, 9, labelX, y, "Subtotal");
        drawText(cs, regular, 9, valueX, y, formatAmount(order.getSubtotal()));
        y -= 14;

        String taxLabel = "GST (" + order.getTaxRate().stripTrailingZeros().toPlainString() + "%)";
        drawText(cs, regular, 9, labelX, y, taxLabel);
        drawText(cs, regular, 9, valueX, y, formatAmount(order.getTaxAmount()));
        y -= 5;

        drawHorizontalLine(cs, y, Color.DARK_GRAY);
        y -= 14;

        drawText(cs, bold, 11, labelX, y, "TOTAL");
        drawText(cs, bold, 11, valueX, y, "INR " + formatAmount(order.getTotal()));
    }

    // =========================================================================
    // DRAWING UTILITIES
    // =========================================================================

    private void drawText(PDPageContentStream cs, PDType1Font font, float size,
                          float x, float y, String text) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.setNonStrokingColor(Color.BLACK);
        cs.newLineAtOffset(x, y);
        cs.showText(text != null ? text : "");
        cs.endText();
    }

    private void drawColoredText(PDPageContentStream cs, PDType1Font font, float size,
                                 float x, float y, String text, Color color) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.setNonStrokingColor(color);
        cs.newLineAtOffset(x, y);
        cs.showText(text != null ? text : "");
        cs.endText();
    }

    private void drawHorizontalLine(PDPageContentStream cs, float y, Color color) throws IOException {
        cs.setStrokingColor(color);
        cs.setLineWidth(0.5f);
        cs.moveTo(MARGIN, y);
        cs.lineTo(MARGIN + CONTENT_WIDTH, y);
        cs.stroke();
    }

    private void drawFilledRect(PDPageContentStream cs, float x, float y,
                                float width, float height, Color color) throws IOException {
        cs.setNonStrokingColor(color);
        cs.addRect(x, y, width, height);
        cs.fill();
    }

    private String formatAmount(BigDecimal amount) {
        return amount != null ? String.format("%,.2f", amount) : "0.00";
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength - 1) + "…" : text;
    }
}

package com.pravinmali.orderprocessing.controller;

import com.pravinmali.orderprocessing.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.UUID;

/**
 * REST controller for PDF invoice generation and download.
 */
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Invoice", description = "PDF invoice generation")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    /**
     * Download a PDF invoice for the given order.
     *
     * <p>Returns 200 with the PDF binary and correct headers for browser download.
     * Returns 404 if the order does not exist.
     * Returns 409 if the order status is PENDING or CANCELLED.</p>
     */
    @GetMapping("/{id}/invoice")
    @Operation(summary = "Download PDF invoice for an order")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable UUID id) throws IOException {
        byte[] pdfBytes = invoiceService.generateInvoice(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"invoice-" + id + ".pdf\"")
                .body(pdfBytes);
    }
}

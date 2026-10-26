package com.pravinmali.orderprocessing.service;

import com.pravinmali.orderprocessing.entity.Order;
import com.pravinmali.orderprocessing.entity.OrderStatus;
import com.pravinmali.orderprocessing.exception.InvoiceNotEligibleException;
import com.pravinmali.orderprocessing.exception.OrderNotFoundException;
import com.pravinmali.orderprocessing.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Handles PDF invoice generation for eligible orders.
 *
 * <p>Invoice generation is only permitted for orders with status:
 * CONFIRMED, SHIPPED, or DELIVERED.
 * PENDING and CANCELLED orders will result in an {@link InvoiceNotEligibleException}.</p>
 */
@Service
public class InvoiceService {

    private static final Set<OrderStatus> INVOICE_ELIGIBLE_STATUSES =
            Set.of(OrderStatus.CONFIRMED, OrderStatus.SHIPPED, OrderStatus.DELIVERED);

    private final OrderRepository orderRepository;

    public InvoiceService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Generates a PDF invoice for the given order.
     *
     * @param orderId the UUID of the order
     * @return PDF content as a byte array
     * @throws OrderNotFoundException     if the order does not exist
     * @throws InvoiceNotEligibleException if the order status does not qualify
     * @throws IOException                 if PDF generation fails
     */
    @Transactional(readOnly = true)
    public byte[] generateInvoice(UUID orderId) throws IOException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!INVOICE_ELIGIBLE_STATUSES.contains(order.getStatus())) {
            throw new InvoiceNotEligibleException(order.getStatus());
        }

        return buildPdf(order);
    }

    /**
     * PDF construction logic using Apache PDFBox.
     * Full implementation in Module 8.
     */
    private byte[] buildPdf(Order order) throws IOException {
        // TODO: implement PDF generation with PDFBox (Module 8)
        throw new UnsupportedOperationException("PDF generation not yet implemented");
    }
}

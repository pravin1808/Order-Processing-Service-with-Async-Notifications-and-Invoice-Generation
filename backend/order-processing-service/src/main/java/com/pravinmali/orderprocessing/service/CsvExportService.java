package com.pravinmali.orderprocessing.service;

import com.pravinmali.orderprocessing.entity.Order;
import com.pravinmali.orderprocessing.repository.OrderRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates a CSV export of all orders, ordered by creation date descending.
 *
 * <p>Each row includes: Order ID, Customer Name, Customer Email, Status,
 * Item Count, Subtotal, Tax Amount, Total, Created At.</p>
 */
@Service
public class CsvExportService {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String[] HEADERS = {
            "Order ID", "Customer Name", "Customer Email",
            "Status", "Item Count", "Subtotal", "Tax Amount", "Total", "Created At"
    };

    private final OrderRepository orderRepository;

    public CsvExportService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Fetches all orders and serializes them to CSV bytes (UTF-8, RFC 4180 format).
     *
     * @return byte array of the complete CSV file
     * @throws IOException if writing to the output stream fails
     */
    @Transactional(readOnly = true)
    public byte[] exportAllOrders() throws IOException {
        List<Order> orders = orderRepository.findAll(
                Sort.by(Sort.Direction.DESC, "createdAt"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.RFC4180
                     .builder()
                     .setHeader(HEADERS)
                     .build())) {

            for (Order order : orders) {
                printer.printRecord(
                        order.getId(),
                        order.getCustomer().getName(),
                        order.getCustomer().getEmail(),
                        order.getStatus(),
                        order.getItems().size(),
                        order.getSubtotal(),
                        order.getTaxAmount(),
                        order.getTotal(),
                        order.getCreatedAt() != null
                                ? order.getCreatedAt().format(DATE_FMT) : ""
                );
            }
        }

        return out.toByteArray();
    }
}

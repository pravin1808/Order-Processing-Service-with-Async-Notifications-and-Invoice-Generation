package com.pravinmali.orderprocessing.config;

import com.pravinmali.orderprocessing.entity.Customer;
import com.pravinmali.orderprocessing.entity.Role;
import com.pravinmali.orderprocessing.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds default administrator and customer accounts on application startup
 * if they do not already exist.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedAdminUser();
        seedCustomerUser();
    }

    private void seedAdminUser() {
        String adminEmail = "admin@orderflow.com";
        Customer admin = customerRepository.findByEmail(adminEmail)
                .orElseGet(() -> Customer.builder()
                        .name("System Admin")
                        .email(adminEmail)
                        .phone("+91 99999 00000")
                        .address("Headquarters, Pune")
                        .role(Role.ADMIN)
                        .build());

        admin.setPasswordHash(passwordEncoder.encode("Admin@1234"));
        customerRepository.save(admin);
        log.info("Initialized and synchronized default ADMIN account: {}", adminEmail);
    }

    private void seedCustomerUser() {
        String customerEmail = "pravin@example.com";
        Customer customer = customerRepository.findByEmail(customerEmail)
                .orElseGet(() -> Customer.builder()
                        .name("Pravin Mali")
                        .email(customerEmail)
                        .phone("+91 98765 43210")
                        .address("FC Road, Pune, Maharashtra - 411004")
                        .role(Role.CUSTOMER)
                        .build());

        customer.setPasswordHash(passwordEncoder.encode("Pass@1234"));
        customerRepository.save(customer);
        log.info("Initialized and synchronized default CUSTOMER account: {}", customerEmail);
    }
}

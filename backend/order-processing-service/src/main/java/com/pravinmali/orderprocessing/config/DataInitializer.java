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
        if (customerRepository.findByEmail(adminEmail).isEmpty()) {
            Customer admin = Customer.builder()
                    .name("System Admin")
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode("Admin@1234"))
                    .phone("+91 99999 00000")
                    .address("Headquarters, Pune")
                    .role(Role.ADMIN)
                    .build();

            customerRepository.save(admin);
            log.info("Initialized default ADMIN account: {}", adminEmail);
        }
    }

    private void seedCustomerUser() {
        String customerEmail = "pravin@example.com";
        if (customerRepository.findByEmail(customerEmail).isEmpty()) {
            Customer customer = Customer.builder()
                    .name("Pravin Mali")
                    .email(customerEmail)
                    .passwordHash(passwordEncoder.encode("Pass@1234"))
                    .phone("+91 98765 43210")
                    .address("FC Road, Pune, Maharashtra - 411004")
                    .role(Role.CUSTOMER)
                    .build();

            customerRepository.save(customer);
            log.info("Initialized default CUSTOMER account: {}", customerEmail);
        }
    }
}

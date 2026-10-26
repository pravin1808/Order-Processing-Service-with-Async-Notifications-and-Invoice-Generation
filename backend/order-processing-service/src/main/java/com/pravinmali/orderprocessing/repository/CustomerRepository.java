package com.pravinmali.orderprocessing.repository;

import com.pravinmali.orderprocessing.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Customer} entities.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    /**
     * Find a customer by their email address.
     * Used during order creation to look up an existing customer,
     * and later during JWT authentication to resolve the logged-in user.
     */
    Optional<Customer> findByEmail(String email);

    /** Check existence by email — avoids loading the full entity when not needed. */
    boolean existsByEmail(String email);
}

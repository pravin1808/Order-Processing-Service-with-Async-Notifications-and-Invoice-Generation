package com.pravinmali.orderprocessing.security;

import com.pravinmali.orderprocessing.entity.Customer;
import com.pravinmali.orderprocessing.repository.CustomerRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Bridges our {@link Customer} entity with Spring Security's {@link UserDetailsService}.
 * The "username" in Spring Security is the customer's email address.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final CustomerRepository customerRepository;

    public UserDetailsServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No customer with email: " + email));

        return new User(
                customer.getEmail(),
                customer.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + customer.getRole().name()))
        );
    }
}

package com.pravinmali.orderprocessing.service;

import com.pravinmali.orderprocessing.dto.request.LoginRequest;
import com.pravinmali.orderprocessing.dto.request.RegisterRequest;
import com.pravinmali.orderprocessing.dto.response.AuthResponse;
import com.pravinmali.orderprocessing.entity.Customer;
import com.pravinmali.orderprocessing.entity.Role;
import com.pravinmali.orderprocessing.repository.CustomerRepository;
import com.pravinmali.orderprocessing.security.JwtUtil;
import com.pravinmali.orderprocessing.security.UserDetailsServiceImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles customer registration and JWT login.
 */
@Service
public class AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthService(CustomerRepository customerRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager,
                       UserDetailsServiceImpl userDetailsService) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Register a new customer. Defaults to CUSTOMER role.
     * Throws {@link IllegalArgumentException} if email is already in use.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (customerRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email already registered: " + request.email());
        }

        Customer customer = Customer.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .address(request.address())
                .role(Role.CUSTOMER)
                .build();

        customer = customerRepository.save(customer);

        UserDetails userDetails = userDetailsService.loadUserByUsername(customer.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.of(token, customer.getId(), customer.getName(),
                customer.getEmail(), customer.getRole());
    }

    /**
     * Authenticate an existing customer and return a JWT.
     * Spring Security's {@link AuthenticationManager} validates credentials.
     */
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        Customer customer = customerRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Customer not found after auth"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.of(token, customer.getId(), customer.getName(),
                customer.getEmail(), customer.getRole());
    }
}

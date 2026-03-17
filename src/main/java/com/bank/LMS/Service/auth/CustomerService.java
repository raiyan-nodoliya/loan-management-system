package com.bank.LMS.Service.auth;

import com.bank.LMS.Entity.Customer;
import com.bank.LMS.Repository.CustomerRepository;
import com.bank.LMS.Service.config.MailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.regex.Pattern;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");
    private static final Pattern PINCODE_PATTERN = Pattern.compile("^[0-9]{4,12}$");

    public CustomerService(CustomerRepository customerRepository,
                           PasswordEncoder passwordEncoder,
                           MailService mailService) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    @Transactional
    public Customer registerCustomer(String fullName,
                                     String email,
                                     String phone,
                                     String dob,
                                     String gender,
                                     String address,
                                     String city,
                                     String state,
                                     String pincode,
                                     String password,
                                     String confirmPassword) {

        fullName = fullName == null ? null : fullName.trim();
        email = email == null ? null : email.trim();
        phone = phone == null ? null : phone.trim();
        dob = dob == null ? null : dob.trim();
        address = address == null ? null : address.trim();
        city = city == null ? null : city.trim();
        state = state == null ? null : state.trim();
        pincode = pincode == null ? null : pincode.trim();
        gender = gender == null ? null : gender.trim();

        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (!Pattern.compile("^[A-Za-z][A-Za-z\\s.'-]{1,99}$").matcher(fullName).matches()) {
            throw new IllegalArgumentException("Name must contain only letters and spaces");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone is required");
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("Enter valid 10-digit phone number");
        }
        if (gender == null || gender.isBlank()) {
            throw new IllegalArgumentException("Gender is required");
        }

        if (dob == null || dob.isBlank()) {
            throw new IllegalArgumentException("DOB is required");
        }

        LocalDate dobDate;
        try {
            dobDate = LocalDate.parse(dob);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid DOB format");
        }

        if (dobDate.isAfter(LocalDate.now().minusYears(18))) {
            throw new IllegalArgumentException("Customer must be at least 18 years old");
        }

        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City is required");
        }
        if (city.length() > 80) {
            throw new IllegalArgumentException("City must be within 80 characters");
        }

        if (state == null || state.isBlank()) {
            throw new IllegalArgumentException("State is required");
        }
        if (state.length() > 80) {
            throw new IllegalArgumentException("State must be within 80 characters");
        }

        if (pincode == null || pincode.isBlank()) {
            throw new IllegalArgumentException("Pincode is required");
        }
        if (!PINCODE_PATTERN.matcher(pincode).matches()) {
            throw new IllegalArgumentException("Pincode must be 4 to 12 digits");
        }

        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Address is required");
        }
        if (address.length() < 10) {
            throw new IllegalArgumentException("Address must be at least 10 characters");
        }
        if (address.length() > 255) {
            throw new IllegalArgumentException("Address must be within 255 characters");
        }

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        if (confirmPassword == null || !password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Password and Confirm Password do not match");
        }

        if (customerRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        if (customerRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("Phone already registered");
        }

        Customer customer = Customer.builder()
                .name(fullName)
                .email(email.toLowerCase())
                .phone(phone)
                .dob(dobDate)
                .gender(gender)
                .address(address)
                .city(city)
                .state(state)
                .pincode(pincode)
                .passwordHash(passwordEncoder.encode(password))
                .active(true)
                .build();

        Customer saved = customerRepository.save(customer);

        try {
            mailService.sendRegistrationSuccess(saved.getEmail(), saved.getName());
        } catch (Exception e) {
            System.out.println("MAIL FAILED (ignored): " + e.getMessage());
        }

        return saved;
    }

    @Transactional
    public Customer authenticateCustomer(String email, String password) {

        Customer customer = customerRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("Email not found"));

        if (!customer.isActive()) {
            throw new IllegalArgumentException("Your account is disabled");
        }

        if (!passwordEncoder.matches(password, customer.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid password");
        }

        System.out.println("HASH CHECK: " + passwordEncoder.matches(password, customer.getPasswordHash()));
        return customer;
    }
}
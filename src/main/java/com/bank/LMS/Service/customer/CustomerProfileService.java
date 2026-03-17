package com.bank.LMS.Service.customer;

import com.bank.LMS.Entity.Customer;
import com.bank.LMS.Repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;

@Service
public class CustomerProfileService {

    private final CustomerRepository customerRepository;

    public CustomerProfileService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer getCustomerOrThrow(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    public void updateProfile(Long customerId,
                              String fullName,
                              String email,
                              String phone,
                              String dob,
                              String address,
                              String city,
                              String state,
                              String pincode) {

        Customer customer = getCustomerOrThrow(customerId);

        if (customerRepository.existsByEmailIgnoreCaseAndCustomerIdNot(email, customerId)) {
            throw new RuntimeException("Email already exists");
        }

        if (customerRepository.existsByPhoneAndCustomerIdNot(phone, customerId)) {
            throw new RuntimeException("Phone number already exists");
        }

        LocalDate parsedDob;
        try {
            parsedDob = LocalDate.parse(dob);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Invalid date of birth");
        }

        if (parsedDob.isAfter(LocalDate.now())) {
            throw new RuntimeException("DOB cannot be in the future");
        }

        int age = Period.between(parsedDob, LocalDate.now()).getYears();
        if (age < 18) {
            throw new RuntimeException("Customer must be at least 18 years old");
        }

        customer.setName(fullName);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setDob(parsedDob);
        customer.setAddress(address);
        customer.setCity(city);
        customer.setState(state);
        customer.setPincode(pincode);

        customerRepository.save(customer);
    }
}
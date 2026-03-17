package com.bank.LMS.Repository;

import com.bank.LMS.Entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);

    boolean existsByEmailIgnoreCaseAndCustomerIdNot(String email, Long customerId);

    boolean existsByPhoneAndCustomerIdNot(String phone, Long customerId);
}
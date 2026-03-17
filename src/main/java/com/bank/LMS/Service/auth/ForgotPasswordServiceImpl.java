package com.bank.LMS.Service.auth;

import com.bank.LMS.Entity.Customer;
import com.bank.LMS.Entity.StaffUsers;
import com.bank.LMS.Repository.CustomerRepository;
import com.bank.LMS.Repository.StaffUsersRepository;
import com.bank.LMS.Service.config.MailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

    private final CustomerRepository customerRepository;
    private final StaffUsersRepository staffUsersRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    public ForgotPasswordServiceImpl(CustomerRepository customerRepository,
                                     StaffUsersRepository staffUsersRepository,
                                     MailService mailService,
                                     PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.staffUsersRepository = staffUsersRepository;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean emailExists(String email) {
        return customerRepository.findByEmailIgnoreCase(email).isPresent()
                || staffUsersRepository.findByEmail(email).isPresent();
    }

    @Override
    public String findUserTypeByEmail(String email) {
        if (customerRepository.findByEmailIgnoreCase(email).isPresent()) {
            return "CUSTOMER";
        }
        if (staffUsersRepository.findByEmail(email).isPresent()) {
            return "STAFF";
        }
        return null;
    }

    @Override
    public String generateOtp() {
        int number = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return String.valueOf(number);
    }

    @Override
    public void sendOtpToEmail(String email, String otp) {
        mailService.sendForgotPasswordOtp(email, otp);
    }

    @Override
    public boolean updatePassword(String email, String newPassword) {

        Optional<Customer> customerOpt = customerRepository.findByEmailIgnoreCase(email);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            customer.setPasswordHash(passwordEncoder.encode(newPassword));
            customerRepository.save(customer);
            return true;
        }

        Optional<StaffUsers> staffOpt = staffUsersRepository.findByEmail(email);
        if (staffOpt.isPresent()) {
            StaffUsers staff = staffOpt.get();
            staff.setPasswordHash(passwordEncoder.encode(newPassword));
            staffUsersRepository.save(staff);
            return true;
        }

        return false;
    }
}
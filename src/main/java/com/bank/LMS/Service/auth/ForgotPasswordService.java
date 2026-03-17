
package com.bank.LMS.Service.auth;

public interface ForgotPasswordService {

    boolean emailExists(String email);

    String findUserTypeByEmail(String email);

    String generateOtp();

    void sendOtpToEmail(String email, String otp);

    boolean updatePassword(String email, String newPassword);
}
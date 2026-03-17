package com.bank.LMS.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSize(MaxUploadSizeExceededException ex,
                                HttpServletRequest request,
                                RedirectAttributes ra) {
        ra.addFlashAttribute("toastMessage", "File size is too large");
        ra.addFlashAttribute("toastType", "error");
        return "redirect:" + request.getHeader("Referer");
    }
}
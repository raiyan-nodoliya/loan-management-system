package com.bank.LMS.Controller.home;

import com.bank.LMS.Repository.LoanTypeRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class homeController {

    private final LoanTypeRepository loanTypeRepository;

    public homeController(LoanTypeRepository loanTypeRepository) {
        this.loanTypeRepository = loanTypeRepository;
    }

    @GetMapping("/")
    public String home(Model model){

        model.addAttribute("loanTypes", loanTypeRepository.findByActiveTrueOrderByLoanTypeNameAsc());
        return "public/index";
    }

    @GetMapping("/contact")
    public String contact(){
        return "public/contact";
    }

//    @GetMapping("/customer_login")
//    public String customer_login(){
//        return "auth/customer_login";
//    }

    @GetMapping("/emi_calculator")
    public String emi_calculator(){
        return "public/emi_calculator";
    }


    @GetMapping("/faq_sqaure")
    public String faq_sqaure(){
        return "public/faq_sqaure";
    }


    @GetMapping({"/loan_card"})
    public String loanCard(Model model) {

        model.addAttribute("loanTypes", loanTypeRepository.findByActiveTrueOrderByLoanTypeNameAsc());
        return "public/loan_card";
    }


//    @GetMapping("/register")
//    public String register(){
//        return "auth/register";
//    }

//    @GetMapping("/staff_login")
//    public String staff_login(){
//        return "auth/staff_login";
//    }



//    @GetMapping("/track_application")
//    public String track_application(){
//        return "public/track_application";
//    }


}

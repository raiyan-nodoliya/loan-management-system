package com.bank.LMS.config;

import com.bank.LMS.Controller.admin.RoleBasedSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder encoder
    ) {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider(userDetailsService);
        auth.setPasswordEncoder(encoder);
        return auth;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           RoleBasedSuccessHandler successHandler,
                                           DaoAuthenticationProvider daoAuthenticationProvider) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authenticationProvider(daoAuthenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**","/js/**","/images/**").permitAll()

                        // staff login public
                        .requestMatchers("/auth/staff_login", "/staff/login").permitAll()

                        // public + customer endpoints
                        .requestMatchers(
                                "/",
                                "/loan_card",
                                "/emi_calculator",
                                "/track_application",
                                "/faq_sqaure",
                                "/contact",
                                "/register",
                                "/customer_login",
                                "/customer/login",

                                "/customer/logout",

                                // customer routes
                                "/apply/**",
                                "/customer/**",
                                "/my_applications/**",
                                "/my_loans/**",
                                "/my_profile/**"
                        ).permitAll()

                        // staff secured
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/manager/**").hasRole("BRANCH_MANAGER")
                        .requestMatchers("/officer/**").hasRole("BANK_OFFICER")
                        .requestMatchers("/risk/**").hasRole("RISK_OFFICER")

                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/auth/staff_login")
                        .loginProcessingUrl("/staff/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(successHandler)
                        .failureUrl("/auth/staff_login?error=true")
                        .permitAll()
                );

        return http.build();
    }
}
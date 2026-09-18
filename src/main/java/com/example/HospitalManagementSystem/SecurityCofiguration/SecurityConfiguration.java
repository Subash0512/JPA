package com.example.HospitalManagementSystem.SecurityCofiguration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfiguration(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // =========================================================
    // AUTHENTICATION PROVIDER
    // =========================================================

    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(
                userDetailsService
        );

        provider.setPasswordEncoder(
                passwordEncoder
        );

        return provider;
    }

    // =========================================================
    // AUTHENTICATION MANAGER
    // =========================================================

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }

    // =========================================================
    // SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http)
            throws Exception {

        http

            // =====================================================
            // CSRF
            // =====================================================

            .csrf(csrf -> csrf.disable())


            // =====================================================
            // SESSION MANAGEMENT
            // =====================================================

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )


            // =====================================================
            // AUTHENTICATION PROVIDER
            // =====================================================

            .authenticationProvider(
                authenticationProvider()
            )


            // =====================================================
            // AUTHORIZATION
            // =====================================================

            .authorizeHttpRequests(auth -> auth

                // -------------------------------------------------
                // PUBLIC LOGIN PAGE
                // -------------------------------------------------

                .requestMatchers(
                    "/login"
                )
                .permitAll()


                // -------------------------------------------------
                // PUBLIC STATIC RESOURCES
                // -------------------------------------------------

                .requestMatchers(
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico"
                )
                .permitAll()


                // -------------------------------------------------
                // LOGIN API
                // -------------------------------------------------

                .requestMatchers(
                    "/auth/login"
                )
                .permitAll()


                // -------------------------------------------------
                // PATIENT REGISTRATION
                // -------------------------------------------------

                .requestMatchers(
                    "/auth/register/patient"
                )
                .permitAll()


                // -------------------------------------------------
                // FORGOT PASSWORD / RESET PASSWORD PAGES
                // -------------------------------------------------

                .requestMatchers(
                    "/forgot-password",
                    "/reset-password"
                )
                .permitAll()


                // -------------------------------------------------
                // FORGOT PASSWORD / RESET PASSWORD APIs
                // -------------------------------------------------

                .requestMatchers(
                    "/auth/forgot-password",
                    "/auth/reset-password"
                )
                .permitAll()


                // -------------------------------------------------
                // DOCTOR REGISTRATION
                // ADMIN ONLY
                // -------------------------------------------------

                .requestMatchers(
                    "/auth/register/doctor"
                )
                .hasRole("ADMIN")


                // -------------------------------------------------
                // DASHBOARD PAGE
                // -------------------------------------------------

                .requestMatchers(
                    "/dashboard"
                )
                .permitAll()


                // -------------------------------------------------
                // ALL FRONTEND UI PAGES
                //
                // JWT is stored in localStorage.
                // JavaScript sends JWT with protected API calls.
                // -------------------------------------------------

                .requestMatchers(
                    "/ui/**"
                )
                .permitAll()


                // -------------------------------------------------
                // PATIENT APIs
                // -------------------------------------------------

                .requestMatchers(
                    "/patients/**"
                )
                .hasAnyRole(
                    "PATIENT",
                    "DOCTOR",
                    "ADMIN"
                )


                // -------------------------------------------------
                // DOCTOR APIs
                // -------------------------------------------------

                .requestMatchers(
                    "/doctors/**"
                )
                .hasAnyRole(
                    "PATIENT",
                    "DOCTOR",
                    "ADMIN"
                )


                // -------------------------------------------------
                // APPOINTMENT APIs
                // -------------------------------------------------

                .requestMatchers(
                    "/appointments/**"
                )
                .hasAnyRole(
                    "PATIENT",
                    "DOCTOR",
                    "ADMIN"
                )


                // -------------------------------------------------
                // MEDICAL RECORD APIs
                // -------------------------------------------------

                .requestMatchers(
                    "/medicalrecords/**"
                )
                .hasAnyRole(
                    "PATIENT",
                    "DOCTOR",
                    "ADMIN"
                )


                // -------------------------------------------------
                // BILLING APIs
                // -------------------------------------------------

                .requestMatchers(
                    "/billing/**"
                )
                .hasAnyRole(
                    "PATIENT",
                    "ADMIN"
                )


                // -------------------------------------------------
                // NOTIFICATION APIs
                //
                // Accessible by:
                // ADMIN
                // DOCTOR
                // PATIENT
                //
                // Notifications do NOT appear in sidebar.
                // They are accessed only through the header bell.
                // -------------------------------------------------

                .requestMatchers(
                    "/notifications/**"
                )
                .hasAnyRole(
                    "ADMIN",
                    "DOCTOR",
                    "PATIENT"
                )


                // -------------------------------------------------
                // EVERYTHING ELSE
                // -------------------------------------------------

                .anyRequest()
                .authenticated()
            )


            // =====================================================
            // JWT FILTER
            // =====================================================

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
package com.example.HospitalManagementSystem.SecurityCofiguration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests()
                .requestMatchers("/patients/**").hasAnyRole("ADMIN", "RECEPTIONIST")
                .requestMatchers("/appointments/**").hasAnyRole("ADMIN", "DOCTOR", "RECEPTIONIST")
                .requestMatchers("/doctors/**").hasRole("ADMIN")
                .requestMatchers("/billing/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            .and()
            .httpBasic(); // For basic authentication
        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails admin = User.withDefaultPasswordEncoder()
            .username("admin")
            .password("adminpass")
            .roles("ADMIN")
            .build();

        UserDetails doctor = User.withDefaultPasswordEncoder()
            .username("doctor")
            .password("doctorpass")
            .roles("DOCTOR")
            .build();

        UserDetails receptionist = User.withDefaultPasswordEncoder()
            .username("receptionist")
            .password("recpass")
            .roles("RECEPTIONIST")
            .build();

        return new InMemoryUserDetailsManager(admin, doctor, receptionist);
    }
}

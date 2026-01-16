package com.chronos.job_scheduler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class AuthConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity){
        try{
            httpSecurity.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                            .requestMatchers("/job/**")
                            .permitAll()
                            .anyRequest()
                            .authenticated())
                    .formLogin(formLogin -> formLogin.defaultSuccessUrl("/", true)
                            .permitAll());
            return httpSecurity.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

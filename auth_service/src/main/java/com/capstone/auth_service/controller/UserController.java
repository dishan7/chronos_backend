package com.capstone.auth_service.controller;

import com.capstone.auth_service.dto.UserDto;
import com.capstone.auth_service.entity.User;
import com.capstone.auth_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class UserController {

    @Autowired
    private UserService _userService;

    @Value("${app.auth.base-url}")
    private String baseUrl;

    @PostMapping("/registerUser")
    public ResponseEntity<String> registerUser(@RequestBody @Valid UserDto userDto){
        User registeredUser = _userService.registerUser(userDto);
        String verificationTokenString = UUID.randomUUID().toString();
        _userService.saveVerificationToken(registeredUser, verificationTokenString);
        String verificationTokenUrl = baseUrl + "/verifyRegisteredUser?verificationToken=" + verificationTokenString;
        System.out.println(verificationTokenUrl);
        return ResponseEntity.status(200).body("verify using the link sent");
    }

    @GetMapping("/verifyRegisteredUser")
    public ResponseEntity<String> verifyRegisteredUser(@RequestParam(name = "verificationToken") String verificationTokenString) throws Exception {
        boolean isTokenValid = _userService.isTokenValid(verificationTokenString);
        if(!isTokenValid) throw new Exception("User not verified!");
        _userService.enableUser(verificationTokenString);
        return ResponseEntity.status(200).body("User verified successfully!");
    }

    @PostMapping("/signIn")
    public ResponseEntity<String> loginUser(@RequestParam(name="username") String username,
                                            @RequestParam(name="password") String password) throws Exception{
        String token = _userService.loginUser(username, password);
        return ResponseEntity.status(200).body(token);
    }

    @GetMapping("/signInTest")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> signInTest(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        authentication.getAuthorities().forEach(authority -> {
            System.out.println(authority.getAuthority());
        });
        return ResponseEntity.status(200).body(username);
    }
}

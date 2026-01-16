package com.capstone.auth_service.service;

import com.capstone.auth_service.dto.UserDto;
import com.capstone.auth_service.entity.User;
import com.capstone.auth_service.entity.VerificationToken;
import com.capstone.auth_service.enums.ROLE;
import com.capstone.auth_service.exception.UserNotFoundException;
import com.capstone.auth_service.repository.UserRepository;
import com.capstone.auth_service.repository.VerificationTokenRepository;
import com.capstone.auth_service.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private UserRepository _userRepository;

    @Autowired
    private PasswordEncoder _passwordEncoder;

    @Autowired
    private VerificationTokenRepository _verificationTokenRepository;

    public User registerUser(UserDto userDto){
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(_passwordEncoder.encode(userDto.getPassword()));
        user.setRole(ROLE.valueOf(userDto.getRole()));
        user.setEnabled(false);
        return _userRepository.save(user);
    }

    public void saveVerificationToken(User registeredUser, String verificationTokenString){
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setUser(registeredUser);
        verificationToken.setVerificationTokenString(verificationTokenString);
        _verificationTokenRepository.save(verificationToken);
    }

    public boolean isTokenValid(String verificationTokenString){
        VerificationToken verificationToken =  _verificationTokenRepository.findByVerificationTokenString(verificationTokenString);
        Duration duration = Duration.between(verificationToken.getCreatedAt(), LocalDateTime.now());
        return duration.toHours() <= 24;
    }

    public void enableUser(String verificationTokenString){
        VerificationToken verificationToken =  _verificationTokenRepository.findByVerificationTokenString(verificationTokenString);
        User registeredUser = verificationToken.getUser();
        registeredUser.setEnabled(true);
        _verificationTokenRepository.delete(verificationToken);
        _userRepository.save(registeredUser);
    }

    public String loginUser(String username, String password) throws Exception{
        User savedUser = _userRepository.findByUsername(username).orElse(null);
        if(savedUser==null) throw new UserNotFoundException("No user found with username " + username);
        if(!savedUser.isEnabled()) throw new Exception("Please verify the user first!");
        boolean passwordMatch = _passwordEncoder.matches(password, savedUser.getPassword());
        if(!passwordMatch) throw new Exception("Wrong password!");
        return TokenUtil.generateToken(savedUser, savedUser.getRole().name());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(Exception e){
        return ResponseEntity.status(404).body(e.getMessage());
    }
}

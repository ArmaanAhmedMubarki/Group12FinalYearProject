package com.group12.athleticaX.service;

import com.group12.athleticaX.model.User;
import com.group12.athleticaX.repository.UserRepository;
import com.group12.athleticaX.util.JwtUtil;
import com.group12.athleticaX.util.OtpStore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpStore otpStore;

    @Autowired
    private EmailService emailService;

    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    // -------------------------
    // REGISTER (Send OTP)
    // -------------------------
    public User registerUser(User user) {

        String email = user.getEmail().toLowerCase();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        String otp = generateOtp();

        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setVerified(false);

        userRepository.save(user);

        otpStore.saveOtp(email, otp);
        emailService.sendOtp(email, otp);

        return user;
    }

    // -------------------------
    // LOGIN (Send OTP)
    // -------------------------
    public Map<String, Object> loginUser(User user) {

        Map<String, Object> response = new HashMap<>();

        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());

        if (existingUser.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found");
            return response;
        }

        User dbUser = existingUser.get();

        if (!passwordEncoder.matches(user.getPassword(), dbUser.getPassword())) {
            response.put("success", false);
            response.put("message", "Invalid credentials");
            return response;
        }

        if (!dbUser.isVerified()) {
            response.put("success", false);
            response.put("message", "Please verify email first");
            return response;
        }

        String otp = generateOtp();

        otpStore.saveOtp(dbUser.getEmail(), otp);
        emailService.sendOtp(dbUser.getEmail(), otp);

        response.put("success", true);
        response.put("message", "OTP sent to email");

        return response;
    }

    // -------------------------
    // VERIFY EMAIL OTP (REGISTER)
    // -------------------------
    public void verifyOtp(String email, String otp) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isVerified()) {
            throw new RuntimeException("User already verified");
        }

        if (!otpStore.validateOtp(email, otp)) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        user.setVerified(true);
        userRepository.save(user);
    }

    // -------------------------
    // VERIFY LOGIN OTP (2FA)
    // -------------------------
    public Map<String, Object> verifyLoginOtp(String email, String otp) {

        Map<String, Object> response = new HashMap<>();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!otpStore.validateOtp(email, otp)) {
            response.put("success", false);
            response.put("message", "Invalid or expired OTP");
            return response;
        }

        String token = jwtUtil.generateToken(user.getEmail());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("email", user.getEmail());
        userInfo.put("role", user.getRole());

        response.put("success", true);
        response.put("token", token);
        response.put("user", userInfo);

        return response;
    }

    // -------------------------
    // RESEND OTP
    // -------------------------
    public void resendLoginOtp(String email) {

    userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (!otpStore.canResendOtp(email)) {
        long seconds = otpStore.getRemainingSeconds(email);
        throw new RuntimeException("Please wait " + seconds + " seconds before requesting OTP again");
      }

    String otp = generateOtp();

    otpStore.saveOtp(email, otp);
    emailService.sendOtp(email, otp);
    }

    // -------------------------
    // SPRING SECURITY
    // -------------------------
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }

    // -------------------------
    // UTIL
    // -------------------------
    private String generateOtp() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }
}
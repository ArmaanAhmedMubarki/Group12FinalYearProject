package com.group12.athleticaX.service;

import com.group12.athleticaX.model.User;
import com.group12.athleticaX.repository.UserRepository;
import com.group12.athleticaX.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.List;

@Service
public class UserService implements UserDetailsService{

    @Autowired
    private UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    public UserService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }
    // Register user (hash password)
    public User registerUser(User user) {
        // Hash the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // Login user
   public Map<String, Object> loginUser(User user) {
    Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
    Map<String, Object> response = new HashMap<>();

    if (existingUser.isPresent() &&
        passwordEncoder.matches(user.getPassword(), existingUser.get().getPassword())) {

        // ✅ generate JWT token
        String token = jwtUtil.generateToken(existingUser.get().getEmail());

        response.put("success", true);
        response.put("token", token);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("role", existingUser.get().getRole());
        response.put("user", userInfo);

    } else {
        response.put("success", false);
        response.put("message", "Invalid credentials");
    }

    return response;
}


    // Get user by ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Delete user
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Map your role to a SimpleGrantedAuthority
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}

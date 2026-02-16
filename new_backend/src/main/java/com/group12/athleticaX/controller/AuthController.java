package com.group12.athleticaX.controller;

import com.group12.athleticaX.model.User;
import com.group12.athleticaX.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Map<String, Object> registerUser(@RequestBody User user) {

        Map<String, Object> response = new HashMap<>();

        try {
            userService.registerUser(user);

            response.put("success", true);
            response.put("message", "User registered successfully");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user) {

        Map<String, Object> response = new HashMap<>();

        response = userService.loginUser(user);

        return response;
    }
}

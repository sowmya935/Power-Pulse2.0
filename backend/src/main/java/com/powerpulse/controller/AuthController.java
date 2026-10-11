package com.powerpulse.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.powerpulse.model.AppUser;
import com.powerpulse.repository.UserRepository;

@Controller
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam("name") String name,
                           @RequestParam("email") String email,
                           @RequestParam("password") String password,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        String cleanName = name == null ? "" : name.trim();
        String cleanEmail = email == null ? "" : email.trim().toLowerCase();

        String error = null;
        if (cleanName.isEmpty() || cleanEmail.isEmpty()) {
            error = "Name and email are required.";
        } else if (password == null || password.length() < 6) {
            error = "Password must be at least 6 characters.";
        } else if (users.existsByEmail(cleanEmail)) {
            error = "Email already registered. Please login.";
        }

        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("name", cleanName);
            model.addAttribute("email", cleanEmail);
            return "register";
        }

        users.save(new AppUser(cleanName, cleanEmail, passwordEncoder.encode(password)));
        redirectAttributes.addFlashAttribute("message", "Account created! Please login.");
        return "redirect:/login";
    }
}

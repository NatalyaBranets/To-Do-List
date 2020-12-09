package com.softserve.itacademy.security;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
    @GetMapping("/form-login")
    public String login() {
        return "login-page";
    }

    @GetMapping("/accessDenied")
    public String accessDenied() {
        return "403";
    }

}

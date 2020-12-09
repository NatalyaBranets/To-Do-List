package com.softserve.itacademy.controller;

import com.softserve.itacademy.service.RoleService;
import com.softserve.itacademy.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final UserService userService;
    public HomeController(UserService userService) {
        this.userService = userService;
    }

    private final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @GetMapping({"/", "home"})
    public String home(Model model) {
        logger.info("GET 'home', '/': open home");
        model.addAttribute("users", userService.getAll());
        return "home";
    }
}

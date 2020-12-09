package com.softserve.itacademy.controller;

import com.softserve.itacademy.model.User;
import com.softserve.itacademy.service.RoleService;
import com.softserve.itacademy.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final RoleService roleService;

    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/create")
    public String create(Model model) {
        logger.info("GET '/users/create': create user");
        model.addAttribute("user", new User());
        return "create-user";
    }

    @PostMapping("/create")
    public String create(@Validated @ModelAttribute("user") User user, BindingResult result) {
        logger.info("POST '/users/create': create user");
        if (result.hasErrors()) {
            return "create-user";
        }
        user.setPassword(user.getPassword());
        user.setRole(roleService.readById(2));
        User newUser = userService.create(user);
        return "redirect:/todos/all/users/" + newUser.getId();
    }

    @PreAuthorize("hasAuthority('ADMIN')or hasAuthority('USER') and authentication.principal.id == #id")
    @GetMapping("/{id}/read")
    public String read(@PathVariable long id, Model model) {
        logger.info("GET '/users/{}/read': read user[id='{}']",
                id, id);
        User user = userService.readById(id);
        model.addAttribute("user", user);
        return "user-info";
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER') and authentication.principal.id == #id")
    @GetMapping("/{id}/update")
    public String update(@PathVariable long id, Model model) {
        logger.info("GET '/users/{}/update': update user[id='{}']",
                id, id);
        User user = userService.readById(id);
        model.addAttribute("user", user);
        model.addAttribute("roles", roleService.getAll());
        return "update-user";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/{id}/update")
    public String update(@PathVariable long id, Model model, @Validated @ModelAttribute("user") User user, @RequestParam("roleId") long roleId, BindingResult result) {
        logger.info("POST '/users/{}/update': update user[id='{}']",
                id, id);
        User oldUser = userService.readById(id);
        if (result.hasErrors()) {
            user.setRole(oldUser.getRole());
            model.addAttribute("roles", roleService.getAll());
            return "update-user";
        }
        if (oldUser.getRole().getName().equals("USER")) {
            user.setRole(oldUser.getRole());
        } else {
            user.setRole(roleService.readById(roleId));
        }
        userService.update(user);
        return "redirect:/users/" + id + "/read";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable("id") long id) {
        logger.info("GET '/users/{}/delete': delete user[id='{}']", id, id);
        userService.delete(id);
        return "redirect:/users/all";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/all")
    public String getAll(Model model) {
        logger.info("GET '/users/all': all users");
        model.addAttribute("users", userService.getAll());
        return "users-list";
    }
}

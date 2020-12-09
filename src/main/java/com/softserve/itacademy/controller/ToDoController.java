package com.softserve.itacademy.controller;

import com.softserve.itacademy.model.Task;
import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.service.TaskService;
import com.softserve.itacademy.service.ToDoService;
import com.softserve.itacademy.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/todos")
public class ToDoController {

    private final ToDoService todoService;
    private final TaskService taskService;
    private final UserService userService;

    private final Logger logger = LoggerFactory.getLogger(ToDoController.class);

    public ToDoController(ToDoService todoService, TaskService taskService, UserService userService) {
        this.todoService = todoService;
        this.taskService = taskService;
        this.userService = userService;
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER') and authentication.principal.id == #ownerId")
    @GetMapping("/create/users/{owner_id}")
    public String create(@PathVariable("owner_id") long ownerId, Model model) {
        logger.info("GET '/todos/create/users/{}': create todo of owner[id='{}']",
                ownerId, ownerId);
        model.addAttribute("todo", new ToDo());
        model.addAttribute("ownerId", ownerId);
        return "create-todo";
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER') and authentication.principal.id == #ownerId")
    @PostMapping("/create/users/{owner_id}")
    public String create(@PathVariable("owner_id") long ownerId, @Validated @ModelAttribute("todo") ToDo todo, BindingResult result) {
        logger.info("POST '/todos/create/users/{}': create todo of owner[id='{}']",
                ownerId, ownerId);
        if (result.hasErrors()) {
            return "create-todo";
        }
        todo.setCreatedAt(LocalDateTime.now());
        todo.setOwner(userService.readById(ownerId));
        todoService.create(todo);
        return "redirect:/todos/all/users/" + ownerId;
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    @GetMapping("/{id}/tasks/{owner_id}")
    public String read(@PathVariable long id, Model model, @PathVariable("owner_id")  long ownerId) {
        logger.info("GET '/todos/{}/tasks': read todo[id='{}']",
                id, id);
        ToDo todo = todoService.readById(id);
        List<Task> tasks = taskService.getByTodoId(id);
        List<User> users = userService.getAll().stream()
                .filter(user -> user.getId() != todo.getOwner().getId()).collect(Collectors.toList());
        model.addAttribute("todo", todo);
        model.addAttribute("tasks", tasks);
        model.addAttribute("users", users);
        return "todo-tasks";
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER') and authentication.principal.id == #ownerId")
    @GetMapping("/{todo_id}/update/users/{owner_id}")
    public String update(@PathVariable("todo_id") long todoId, @PathVariable("owner_id") long ownerId, Model model) {
        logger.info("GET '/todos/{}/update/users/{}': update todo[id='{}'] of owner[id='{}']",
                todoId, ownerId, todoId, ownerId);
        ToDo todo = todoService.readById(todoId);
        model.addAttribute("todo", todo);
        return "update-todo";
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER') and authentication.principal.id == #ownerId")
    @PostMapping("/{todo_id}/update/users/{owner_id}")
    public String update(@PathVariable("todo_id") long todoId, @PathVariable("owner_id") long ownerId,
                         @Validated @ModelAttribute("todo") ToDo todo, BindingResult result) {
        logger.info("POST '/todos/{}/update/users/{}': update todo[id='{}'] of owner[id='{}']",
                todoId, ownerId, todoId, ownerId);
        if (result.hasErrors()) {
            todo.setOwner(userService.readById(ownerId));
            return "update-todo";
        }
        ToDo oldTodo = todoService.readById(todoId);
        todo.setOwner(oldTodo.getOwner());
        todo.setCollaborators(oldTodo.getCollaborators());
        todoService.update(todo);
        return "redirect:/todos/all/users/" + ownerId;
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER') and authentication.principal.id == #ownerId")
    @GetMapping("/{todo_id}/delete/users/{owner_id}")
    public String delete(@PathVariable("todo_id") long todoId, @PathVariable("owner_id") long ownerId) {
        logger.info("GET '/todos/{}/delete/users/{}': delete todo[id='{}'] of owner[id='{}']",
                todoId, ownerId, todoId, ownerId);
        todoService.delete(todoId);
        return "redirect:/todos/all/users/" + ownerId;
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER') and authentication.principal.id == #userId")
    @GetMapping("/all/users/{user_id}")
    public String getAll(@PathVariable("user_id") long userId, Model model) {
        logger.info("GET '/todos/all/users/{}': get all todos of user[id='{}']",
                userId, userId);
        List<ToDo> todos = todoService.getByUserId(userId);
        model.addAttribute("todos", todos);
        model.addAttribute("user", userService.readById(userId));
        return "todos-user";
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER') and authentication.principal.id == #userId")
    @GetMapping("/{id}/add")
    public String addCollaborator(@PathVariable long id, @RequestParam("user_id") long userId) {
        logger.info("GET '/todos/{}/add': add collaborator, todo[id='{}']",
                id, id);
        ToDo todo = todoService.readById(id);
        List<User> collaborators = todo.getCollaborators();
        collaborators.add(userService.readById(userId));
        todo.setCollaborators(collaborators);
        todoService.update(todo);
        return "redirect:/todos/" + id + "/tasks/"+ todo.getOwner().getId();
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER') and authentication.principal.id == #userId")
    @GetMapping("/{id}/remove")
    public String removeCollaborator(@PathVariable long id, @RequestParam("user_id") long userId) {
        logger.info("GET '/todos/{}/remove': remove collaborator, todo[id='{}']",
                id, id);
        ToDo todo = todoService.readById(id);
        List<User> collaborators = todo.getCollaborators();
        collaborators.remove(userService.readById(userId));
        todo.setCollaborators(collaborators);
        todoService.update(todo);
        return "redirect:/todos/" + id + "/tasks/"+ todo.getOwner().getId();
    }
}

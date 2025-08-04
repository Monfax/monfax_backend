package com.LDE.monFax_backend.controllers;

import com.LDE.monFax_backend.models.User;
import com.LDE.monFax_backend.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> numbersUsers(){
        return ResponseEntity.ok(userService.numberUsers());
    }

    @GetMapping("/registrations/last-30-days")
    public ResponseEntity<Long> getRegistrationsLast30Days() {
        return ResponseEntity.ok(userService.getUsersRegisteredLast30Days());
    }

    @GetMapping("/last/five")
    public List<User> getLast5ConnectedUsers() {
        return userService.getLast5ConnectedUsers();
    }
}

package com.example.Taskmymgmt.Controller;

import com.example.Taskmymgmt.Entity.UserEntity;
import com.example.Taskmymgmt.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// This contoller is not being used as of now as its purpose is being filled by the auth controller

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/getalltask")
    public List<UserEntity> getallUser() {
        return userService.getAllUsers();
    }

    @DeleteMapping("/deleteUserByUsername")
    public void deleteUser(@PathVariable String username) {
        userService.deleteuser(username);
    }

}


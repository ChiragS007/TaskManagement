package com.example.Taskmymgmt.Service;

import com.example.Taskmymgmt.Entity.UserEntity;
import com.example.Taskmymgmt.Repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    public Optional<UserEntity> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public void saveNewUser(UserEntity user) {
        userRepository.save(user);
    }

    public void saveUser(UserEntity user) {
        userRepository.save(user);
    }


    public List<UserEntity> findAllUsersWithTaskInList(ObjectId taskId) {
        return userRepository.findAll().stream()
                .filter(user -> user.getTask_list().stream()
                        .anyMatch(taskRef -> taskRef.getId().equals(taskId)))
                .collect(Collectors.toList());
    }

    public void deleteuser(String username) {
        userRepository.deleteByUsername(username);
    }

    public boolean setRoleToManager(String username) {
        Optional<UserEntity> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            UserEntity user = userOptional.get();
            user.setRoles(UserEntity.ERole.MANAGER); // Assuming "MANAGER" is a predefined role
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public boolean setRoleToEmployee(String username) {
        Optional<UserEntity> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            UserEntity user = userOptional.get();
            user.setRoles(UserEntity.ERole.EMPLOYEE); // Assuming "MANAGER" is a predefined role
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public Optional<UserEntity> findUserWithTaskInList(ObjectId taskId, UserEntity.ERole role) {
        return userRepository.findAllByRoles(role).stream()
                .filter(user -> user.getTask_list().stream()
                        .anyMatch(taskRef -> taskRef.getId().equals(taskId)))
                .findFirst();
    }

}

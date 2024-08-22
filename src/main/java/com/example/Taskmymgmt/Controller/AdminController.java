package com.example.Taskmymgmt.Controller;

import com.example.Taskmymgmt.Config.JwtService;
import com.example.Taskmymgmt.Entity.TaskEntity;
import com.example.Taskmymgmt.Entity.UserEntity;
import com.example.Taskmymgmt.Repository.TaskRepository;
import com.example.Taskmymgmt.Service.TaskService;
import com.example.Taskmymgmt.Service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private JwtService jwtService;

    @GetMapping("/get_tasks")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<List<TaskEntity>> getalltask(@RequestHeader("adminUsername") String adminUsername){

        Optional<UserEntity> adminUser = userService.findByUsername(adminUsername);

        if (adminUser.isPresent()) {
            List<TaskEntity> taskList = adminUser.get().getTask_list().stream()
                    .map(taskRef -> taskRepository.findById(taskRef.getId()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(taskList);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create_task/{assigneeUsername}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addTask(@RequestBody TaskEntity task,
                                          @RequestHeader("Authorization") String authHeader,
                                          @PathVariable("assigneeUsername") String assigneeUsername) {

        String jwt = authHeader.substring(7);

        String adminUsername = jwtService.extractUsername(jwt);

        TaskEntity createdTask = taskService.createTask(task, adminUsername, assigneeUsername);
        return ResponseEntity.ok("Task created with ID: " + createdTask.getId());
    }

    // This I am making to set roles for the username that only an admin can do

    @PutMapping("/set_manager/{assigneeUsername}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> setManagerRole(@PathVariable("assigneeUsername") String assigneeUsername) {
        boolean isRoleUpdated = userService.setRoleToManager(assigneeUsername);
        if (isRoleUpdated) {
            return ResponseEntity.ok("User role updated to Manager for: " + assigneeUsername);
        } else {
            return ResponseEntity.badRequest().body("Failed to update role for: " + assigneeUsername);
        }
    }

    @PutMapping("/set_employee/{assigneeUsername}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> setEmployeeRole(@PathVariable("assigneeUsername") String assigneeUsername) {
        boolean isRoleUpdated = userService.setRoleToEmployee(assigneeUsername);
        if (isRoleUpdated) {
            return ResponseEntity.ok("User role updated to Employee for: " + assigneeUsername);
        } else {
            return ResponseEntity.badRequest().body("Failed to update role for: " + assigneeUsername);
        }
    }

    @PutMapping("/set_admin/{assigneeUsername}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> setAdminRole(@PathVariable("assigneeUsername") String assigneeUsername) {
        boolean isRoleUpdated = userService.setRoleToAdmin(assigneeUsername);
        if (isRoleUpdated) {
            return ResponseEntity.ok("User role updated to Admin for: " + assigneeUsername);
        } else {
            return ResponseEntity.badRequest().body("Failed to update role for: " + assigneeUsername);
        }
    }

    @PutMapping("/review_task/{task_id}/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> reviewTask(@PathVariable("task_id") ObjectId taskId,
                                            @PathVariable("status") String status,
                                             @RequestHeader("Authorization") String authHeader) {

        String jwt = authHeader.substring(7);

        String adminUsername = jwtService.extractUsername(jwt);
        // Fetch the task by ID
        Optional<TaskEntity> taskOptional = taskService.findTaskById(taskId);

        if (!taskOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        TaskEntity task = taskOptional.get();

        // Check if the task's assignee is the current admin
        if (!task.getAssignee().equals(adminUsername)) {
            return ResponseEntity.status(403).body("Admin is not the current assignee of this task.");
        }

        if ("REJECTED".equalsIgnoreCase(status)) {
            // Find an EMPLOYEE whose task list contains the task
            Optional<UserEntity> employeeOptional = userService.findUserWithTaskInList(taskId, UserEntity.ERole.EMPLOYEE);

            if (!employeeOptional.isPresent()) {
                return ResponseEntity.badRequest().body("No employee found with this task.");
            }

            UserEntity employee = employeeOptional.get();

            // Change the assignee to the employee and set the status to 'To Do'
            task.setAssignee(employee.getUsername());
            task.setStatus("To Do");
            taskService.saveTask(task);

            return ResponseEntity.ok("Task reassigned to employee and status set to 'To Do'.");

        } else if ("COMPLETED".equalsIgnoreCase(status)) {
            // Set the task status to Completed
            task.setStatus("Completed");
            taskService.saveTask(task);

            List<UserEntity> usersWithTask = userService.findAllUsersWithTaskInList(taskId);

            for (UserEntity user : usersWithTask) {
                // Remove the task from the user's task list
                user.getTask_list().removeIf(taskRef -> taskRef.getId().equals(taskId));
                userService.saveUser(user);  // Save the updated user
            }

            return ResponseEntity.ok("Task marked as Completed and removed from all users' task lists.");
        } else {
            return ResponseEntity.badRequest().body("Invalid status. Only REJECTED or COMPLETED are allowed.");
        }
    }

}


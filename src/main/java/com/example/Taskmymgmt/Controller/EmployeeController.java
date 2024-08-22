package com.example.Taskmymgmt.Controller;

import com.example.Taskmymgmt.Config.JwtService;
import com.example.Taskmymgmt.Entity.TaskEntity;
import com.example.Taskmymgmt.Entity.UserEntity;
import com.example.Taskmymgmt.Service.TaskService;
import com.example.Taskmymgmt.Service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


// WILL THINK ABOUT IT

@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    // 1. Get all tasks from the task_list of the employee
    @GetMapping("/tasks")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<TaskEntity>> getEmployeeTasks(@RequestHeader("Authorization") String authHeader) {

        String jwt = authHeader.substring(7);

        String employeeUsername = jwtService.extractUsername(jwt);

        Optional<UserEntity> employeeOptional = userService.findByUsername(employeeUsername);

        if (employeeOptional.isPresent()) {
            UserEntity employee = employeeOptional.get();
            List<TaskEntity> taskList = employee.getTask_list().stream()
                    .map(taskRef -> taskService.findTaskById(taskRef.getId()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(taskList);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/perform_task/{task_id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<String> performTask(@PathVariable("task_id") ObjectId taskId,
                                              @RequestBody TaskEntity taskUpdate,
                                              @RequestHeader("Authorization") String authHeader) {

        String jwt = authHeader.substring(7);

        String employeeUsername = jwtService.extractUsername(jwt);

        Optional<TaskEntity> taskOptional = taskService.findTaskById(taskId);
        if (taskOptional.isPresent()) {
            TaskEntity task = taskOptional.get();

            // Check if the current employee is the assignee
            if (!task.getAssignee().equals(employeeUsername)) {
                return ResponseEntity.status(403).body("You are not assigned to this task.");
            }

            // Update task fields
            taskService.updateTaskFields(task, taskUpdate);
            task.setStatus("In Progress");
            taskService.saveTask(task);

            return ResponseEntity.ok("Task updated and status set to 'In Progress'.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/mark_review/{task_id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<String> markTaskForReview(@PathVariable("task_id") ObjectId taskId,
                                                    @RequestHeader("Authorization") String authHeader) {

        String jwt = authHeader.substring(7);

        String employeeUsername = jwtService.extractUsername(jwt);

        Optional<TaskEntity> taskOptional = taskService.findTaskById(taskId);
        if (taskOptional.isPresent()) {
            TaskEntity task = taskOptional.get();

            // Check if the current employee is the assignee
            if (!task.getAssignee().equals(employeeUsername)) {
                return ResponseEntity.status(403).body("You are not assigned to this task.");
            }

            // Find the manager who has this task in their task list
            Optional<UserEntity> managerOptional = userService.findUserWithTaskInList(taskId, UserEntity.ERole.MANAGER);
            if (!managerOptional.isPresent()) {
                return ResponseEntity.badRequest().body("No manager found with this task.");
            }

            UserEntity manager = managerOptional.get();

            // Update task's status and reassign to the manager
            task.setStatus("To Be Reviewed");
            task.setAssignee(manager.getUsername());
            taskService.saveTask(task);

            return ResponseEntity.ok("Task marked for review and reassigned to manager: " + manager.getUsername());
        } else {
            return ResponseEntity.notFound().build();
        }
    }



}

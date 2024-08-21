package com.example.Taskmymgmt.Controller;

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
@RequestMapping("/manager")
public class ManagerController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @Autowired
    private TaskRepository taskRepository;

    @GetMapping("/get_tasks")
    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    public ResponseEntity<List<TaskEntity>> getManagerTasks(@RequestHeader("managerUsername") String managerUsername) {
        Optional<UserEntity> managerOptional = userService.findByUsername(managerUsername);

        if (managerOptional.isPresent()) {
            UserEntity manager = managerOptional.get();
            List<TaskEntity> taskList = manager.getTask_list().stream()
                    .map(taskRef -> taskRepository.findById(taskRef.getId()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(taskList);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/assign_to_employee/{task_id}/{employee_username}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<String> assignTaskToEmployee(@PathVariable("task_id") ObjectId taskId,
                                                       @PathVariable("employee_username") String employeeUsername) {
        Optional<TaskEntity> taskOptional = taskService.findTaskById(taskId);

        if (taskOptional.isPresent()) {
            TaskEntity task = taskOptional.get();
            Optional<UserEntity> employeeOptional = userService.findByUsername(employeeUsername);

            if (employeeOptional.isPresent()) {
                UserEntity employee = employeeOptional.get();

                // Update task's assignee field and save the task
                task.setAssignee(employee.getUsername());
                taskService.saveTask(task);

                // Add the task to the employee's task list
                employee.getTask_list().add(task);
                userService.saveNewUser(employee);  // Save the employee with updated task list

                return ResponseEntity.ok("Task assigned to employee: " + employeeUsername);
            } else {
                return ResponseEntity.badRequest().body("Employee not found");
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @PutMapping("/review_task/{task_id}/{review_status}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<String> reviewTask(@PathVariable("task_id") ObjectId taskId,
                                             @PathVariable("review_status") String reviewStatus,
                                             @RequestHeader("managerUsername") String managerUsername) {
        Optional<TaskEntity> taskOptional = taskService.findTaskById(taskId);

        if (taskOptional.isPresent()) {
            TaskEntity task = taskOptional.get();

            // Check if the task's assignee is the current manager
            if (!task.getAssignee().equals(managerUsername)) {
                return ResponseEntity.status(403).body("Manager is not the current assignee of this task.");
            }

            if ("REVIEW_COMPLETED".equalsIgnoreCase(reviewStatus)) {
                // Assign task back to the admin
                Optional<UserEntity> adminOptional = userService.findUserWithTaskInList(taskId, UserEntity.ERole.ADMIN);

                if (!adminOptional.isPresent()) {
                    return ResponseEntity.badRequest().body("Admin not found with this task.");
                }

                UserEntity admin = adminOptional.get();

                // Update task's status and assignee field
                task.setStatus("Review Completed");
                task.setAssignee(admin.getUsername());
                taskService.saveTask(task);

                return ResponseEntity.ok("Task marked as 'Review Completed' and reassigned to admin: " + admin.getUsername());

            } else if ("REJECTED".equalsIgnoreCase(reviewStatus)) {
                // Reassign task back to the employee and set status to 'To Do'
                Optional<UserEntity> employeeOptional = userService.findUserWithTaskInList(taskId, UserEntity.ERole.EMPLOYEE);

                if (!employeeOptional.isPresent()) {
                    return ResponseEntity.badRequest().body("No employee found with this task.");
                }

                UserEntity employee = employeeOptional.get();

                // Update task's assignee and status field
                task.setAssignee(employee.getUsername());
                task.setStatus("To Do");
                taskService.saveTask(task);

                return ResponseEntity.ok("Task reassigned to employee: " + employee.getUsername() + " and status set to 'To Do'.");
            } else {
                return ResponseEntity.badRequest().body("Invalid review status.");
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}

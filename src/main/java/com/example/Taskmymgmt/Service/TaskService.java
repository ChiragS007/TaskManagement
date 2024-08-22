package com.example.Taskmymgmt.Service;

import com.example.Taskmymgmt.Entity.TaskEntity;
import com.example.Taskmymgmt.Entity.UserEntity;
import com.example.Taskmymgmt.Repository.TaskRepository;
import com.example.Taskmymgmt.Repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    public Optional<TaskEntity> findTaskById(ObjectId taskId) {
        return taskRepository.findById(taskId);
    }

    public void saveTask(TaskEntity task) {
        taskRepository.save(task);
    }

    public TaskEntity createTask(TaskEntity task, String adminUsername, String assigneeUsername) {

        task.setStatus("To Do");
        task.setAssignee(assigneeUsername);

        // Save the task to the task collection
        TaskEntity savedTask = taskRepository.save(task);

        Optional<UserEntity> adminOptional = userRepository.findByUsername(adminUsername);
        if (adminOptional.isPresent()) {
            UserEntity admin = adminOptional.get();
            admin.getTask_list().add(savedTask);
            userRepository.save(admin);
        }

        Optional<UserEntity> assigneeOptional = userRepository.findByUsername(assigneeUsername);
        if (assigneeOptional.isPresent()) {
            UserEntity assignee = assigneeOptional.get();
            assignee.getTask_list().add(savedTask);
            userRepository.save(assignee);
        }

        return savedTask;
    }

    public void updateTaskFields(TaskEntity existingTask, TaskEntity taskUpdate) {
        if (taskUpdate.getTitle() != null) {
            existingTask.setTitle(taskUpdate.getTitle());
        }
        if (taskUpdate.getDescription() != null) {
            existingTask.setDescription(taskUpdate.getDescription());
        }
        if (taskUpdate.getRemarks() != null) {
            existingTask.setRemarks(taskUpdate.getRemarks());
        }
    }
}

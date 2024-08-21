package com.example.Taskmymgmt.Repository;

import com.example.Taskmymgmt.Entity.TaskEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TaskRepository extends MongoRepository<TaskEntity, ObjectId> {
    Optional<TaskEntity> findById(ObjectId id);
}

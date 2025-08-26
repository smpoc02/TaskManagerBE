package com.sdmd.taskmanager.entity;

import com.sdmd.taskmanager.model.Task;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "tasks")
@Data
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String status = Task.StatusEnum.PENDING.getValue();

    @Column(nullable = false)
    private OffsetDateTime deadline = OffsetDateTime.now();
}

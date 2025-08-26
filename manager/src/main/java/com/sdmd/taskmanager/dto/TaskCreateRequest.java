package com.sdmd.taskmanager.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCreateRequest {
    @NotNull
    private String title;
    @NotNull
    private String description;
    private OffsetDateTime deadline;
}
package com.sdmd.taskmanager.mapper;

import com.sdmd.taskmanager.entity.TaskEntity;
import com.sdmd.taskmanager.model.Task;
import com.sdmd.taskmanager.model.Task.StatusEnum;
import org.mapstruct.*;

import java.time.OffsetDateTime;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(source = "status", target = "status", qualifiedByName = "toEntityStatus")
    TaskEntity toEntity(Task dto);

    @Mapping(source = "status", target = "status", qualifiedByName = "toDtoStatus")
    Task toDto(TaskEntity entity);

    @Named("toEntityStatus")
    static String toEntityStatus(StatusEnum statusEnum) {
        return statusEnum != null ? statusEnum.getValue() : StatusEnum.PENDING.getValue();
    }

    @Named("toDtoStatus")
    static StatusEnum toDtoStatus(String status) {
        return status != null ? StatusEnum.fromValue(status) : StatusEnum.PENDING;
    }
}

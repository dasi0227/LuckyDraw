package com.dasi.domain.task.repository;

import com.dasi.domain.task.model.entity.TaskEntity;

import java.util.List;

public interface ITaskRepository {

    List<TaskEntity> queryUnsolvedTask();

    void sendMessage(TaskEntity taskEntity);

    void updateTaskState(TaskEntity taskEntity);

}

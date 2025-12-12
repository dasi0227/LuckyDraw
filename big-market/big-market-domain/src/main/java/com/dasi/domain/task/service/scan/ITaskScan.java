package com.dasi.domain.task.service.scan;

import com.dasi.domain.task.model.entity.TaskEntity;

import java.util.List;

public interface ITaskScan {

    List<TaskEntity> queryUnsolvedTask();

    void sendMessage(TaskEntity taskEntity);

    void updateTaskState(TaskEntity taskEntity);

}

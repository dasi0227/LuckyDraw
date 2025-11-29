package com.dasi.domain.award.service.scan;

import com.dasi.domain.award.model.entity.TaskEntity;

import java.util.List;

public interface ITaskScan {

    List<TaskEntity> queryUnsolvedTask();

    void sendMessage(TaskEntity taskEntity);

    void updateTaskState(TaskEntity taskEntity);

}

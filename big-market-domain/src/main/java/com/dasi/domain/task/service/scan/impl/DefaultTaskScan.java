package com.dasi.domain.task.service.scan.impl;

import com.dasi.domain.task.model.entity.TaskEntity;
import com.dasi.domain.task.repository.ITaskRepository;
import com.dasi.domain.task.service.scan.ITaskScan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class DefaultTaskScan implements ITaskScan {

    @Resource
    private ITaskRepository taskRepository;

    @Override
    public List<TaskEntity> queryUnsolvedTask() {
        return taskRepository.queryUnsolvedTask();
    }

    @Override
    public void sendMessage(TaskEntity taskEntity) {
        taskRepository.sendMessage(taskEntity);
    }

    @Override
    public void updateTaskState(TaskEntity taskEntity) {
        taskRepository.updateTaskState(taskEntity);
    }

}

package com.dasi.domain.award.service.scan.impl;

import com.dasi.domain.award.model.entity.TaskEntity;
import com.dasi.domain.award.repository.IAwardRepository;
import com.dasi.domain.award.service.scan.ITaskScan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class DefaultTaskScan implements ITaskScan {

    @Resource
    private IAwardRepository awardRepository;

    @Override
    public List<TaskEntity> queryUnsolvedTask() {
        return awardRepository.queryUnsolvedTask();
    }

    @Override
    public void sendMessage(TaskEntity taskEntity) {
        awardRepository.sendMessage(taskEntity);
    }

    @Override
    public void updateTaskState(TaskEntity taskEntity) {
        awardRepository.updateTaskState(taskEntity);
    }
}

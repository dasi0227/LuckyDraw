package com.dasi.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.dasi.domain.task.model.entity.TaskEntity;
import com.dasi.domain.task.model.type.TaskState;
import com.dasi.domain.task.repository.ITaskRepository;
import com.dasi.infrastructure.event.EventPublisher;
import com.dasi.infrastructure.persistent.dao.ITaskDao;
import com.dasi.infrastructure.persistent.po.Task;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class TaskRepository implements ITaskRepository {

    @Resource
    private ITaskDao taskDao;

    @Resource
    private EventPublisher eventPublisher;

    @Resource
    private IDBRouterStrategy dbRouterStrategy;

    @Override
    public List<TaskEntity> queryUnsolvedTask() {
        List<Task> taskList = taskDao.queryUnsolvedTask();
        if (taskList == null || taskList.isEmpty()) return null;
        return taskList.stream()
                .map(task -> TaskEntity.builder()
                        .userId(task.getUserId())
                        .messageId(task.getMessageId())
                        .topic(task.getTopic())
                        .message(task.getMessage())
                        .taskState(TaskState.valueOf(task.getTaskState()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void sendMessage(TaskEntity taskEntity) {
        Task task = new Task();
        task.setUserId(taskEntity.getUserId());
        task.setMessageId(taskEntity.getMessageId());
        task.setTopic(taskEntity.getTopic());
        task.setMessage(taskEntity.getMessage());
        task.setTaskState(taskEntity.getTaskState().name());

        try {
            dbRouterStrategy.doRouter(taskEntity.getUserId());

            eventPublisher.publish(taskEntity.getTopic(), taskEntity.getMessage());
            task.setTaskState(TaskState.DISTRIBUTED.name());
            taskDao.updateTaskState(task);
        } catch (Exception e) {
            task.setTaskState(TaskState.FAILED.name());
            taskDao.updateTaskState(task);
            throw new RuntimeException(e);
        } finally {
            dbRouterStrategy.clear();
        }
    }

    @Override
    public void updateTaskState(TaskEntity taskEntity) {
        Task task = new Task();
        task.setUserId(taskEntity.getUserId());
        task.setMessageId(taskEntity.getMessageId());
        task.setTopic(taskEntity.getTopic());
        task.setMessage(taskEntity.getMessage());
        task.setTaskState(taskEntity.getTaskState().name());

        taskDao.updateTaskState(task);
    }

}

package com.dasi.trigger.job;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.dasi.domain.task.model.entity.TaskEntity;
import com.dasi.domain.task.model.type.TaskState;
import com.dasi.domain.task.service.scan.ITaskScan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ScanUnsolvedTaskJob {

    @Resource
    private IDBRouterStrategy dbRouterStrategy;

    @Resource
    private ITaskScan taskScan;

    @Resource
    private ThreadPoolExecutor scanExecutor;

    @Resource
    private ThreadPoolExecutor sendExecutor;

    @Scheduled(cron = "0/5 * * * * ?")
    public void scanUnsolvedTask() {
        try {
            int dbCount = dbRouterStrategy.dbCount();

            for (int dbIdx = 1; dbIdx <= dbCount; dbIdx++) {
                final int finalDbIdx = dbIdx;

                scanExecutor.execute(() -> {
                    try {
                        // 设置当前线程数据库路由
                        dbRouterStrategy.setDBKey(finalDbIdx);
                        dbRouterStrategy.setTBKey(0);

                        // 查询任务
                        List<TaskEntity> taskEntityList = taskScan.queryUnsolvedTask();
                        if (taskEntityList == null || taskEntityList.isEmpty()) {
                            log.debug("【扫描】没有发现未处理任务：dbIdx={}", finalDbIdx);
                            return;
                        }

                        String messageIds = taskEntityList.stream()
                                .map(TaskEntity::getMessageId)
                                .collect(Collectors.joining(","));
                        log.info("【扫描】发现未处理任务：dbIdx={}, messageIds={}", finalDbIdx, messageIds);

                        // 处理每个任务
                        for (TaskEntity taskEntity : taskEntityList) {
                            sendExecutor.execute(() -> {
                                try {
                                    dbRouterStrategy.setDBKey(finalDbIdx);
                                    dbRouterStrategy.setTBKey(0);
                                    taskScan.sendMessage(taskEntity);
                                    log.info("【扫描】重新发送未完成任务成功：dbIdx={}, topic={}, messageId={}", finalDbIdx, taskEntity.getTopic(), taskEntity.getMessageId());
                                } catch (Exception e) {
                                    taskEntity.setTaskState(TaskState.FAILED);
                                    taskScan.updateTaskState(taskEntity);
                                    log.error("【扫描】重新发送未完成任务失败：dbIdx={}, topic={}, messageId={}, error={}", finalDbIdx, taskEntity.getTopic(), taskEntity.getMessageId(), e.getMessage());
                                } finally {
                                    dbRouterStrategy.clear();
                                }
                            });
                        }
                    } finally {
                        dbRouterStrategy.clear();
                    }
                });
            }
        } catch (Exception e) {
            log.error("【扫描】本次任务处理失败：error={}", e.getMessage());
        }
    }
}

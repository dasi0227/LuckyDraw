package com.dasi.trigger.job;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import com.dasi.domain.award.model.entity.TaskEntity;
import com.dasi.domain.award.model.type.TaskState;
import com.dasi.domain.award.service.scan.ITaskScan;
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
    private IDBRouterStrategy dbRouter;

    @Resource
    private ITaskScan taskScan;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Scheduled(cron = "0/5 * * * * ?")
    public void scanUnsolvedTask() {
        try {
            int dbCount = dbRouter.dbCount();

            for (int dbIdx = 1; dbIdx <= dbCount; dbIdx++) {
                int finalDbIdx = dbIdx;
                threadPoolExecutor.execute(() -> {
                    try {
                        dbRouter.setDBKey(finalDbIdx);
                        dbRouter.setTBKey(0);
                        List<TaskEntity> taskEntities = taskScan.queryUnsolvedTask();
                        if (taskEntities.isEmpty()) {
                            log.debug("【定时任务 - scanUnsolvedTask】没有发现未处理奖品：dbIdx = {}", finalDbIdx);
                            return;
                        }

                        String messageIds = taskEntities.stream()
                                .map(TaskEntity::getMessageId)
                                .collect(Collectors.joining(","));
                        log.info("【定时任务 - scanUnsolvedTask】发现未处理奖品：dbIdx = {}, messageIds = {}", finalDbIdx, messageIds);

                        for (TaskEntity taskEntity : taskEntities) {
                            threadPoolExecutor.execute(() -> {
                                try {
                                    taskScan.sendMessage(taskEntity);
                                } catch (Exception e) {
                                    taskEntity.setTaskState(TaskState.FAILED.getCode());
                                    taskScan.updateTaskState(taskEntity);
                                    log.info("【定时任务 - scanUnsolvedTask】处理单个奖品失败：dbIdx = {}, topic = {}, messageId = {}, error = {}", finalDbIdx, taskEntity.getTopic(), taskEntity.getMessageId(), e.getMessage());
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    } finally {
                        dbRouter.clear();
                    }
                });
            }
        } catch (Exception e) {
            log.info("【定时任务 - scanUnsolvedTask】失败：error = {}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            dbRouter.clear();
        }
    }
}

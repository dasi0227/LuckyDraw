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
                        List<TaskEntity> taskEntityList = taskScan.queryUnsolvedTask();
                        if (taskEntityList.isEmpty()) {
                            log.debug("【扫描未处理奖品】没有发现：dbIdx={}", finalDbIdx);
                            return;
                        }

                        String messageIds = taskEntityList.stream()
                                .map(TaskEntity::getMessageId)
                                .collect(Collectors.joining(","));
                        log.info("【扫描未处理奖品】发现：dbIdx={}, messageIds={}", finalDbIdx, messageIds);

                        for (TaskEntity taskEntity : taskEntityList) {
                            threadPoolExecutor.execute(() -> {
                                try {
                                    taskScan.sendMessage(taskEntity);
                                    log.info("【扫描未处理奖品】成功：dbIdx={}, topic={}, messageId={}", finalDbIdx, taskEntity.getTopic(), taskEntity.getMessageId());
                                } catch (Exception e) {
                                    taskEntity.setTaskState(TaskState.FAILED);
                                    taskScan.updateTaskState(taskEntity);
                                    log.error("【扫描未处理奖品】失败：dbIdx={}, topic={}, messageId={}", finalDbIdx, taskEntity.getTopic(), taskEntity.getMessageId());
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
            log.error("【扫描未处理奖品】失败：error={}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            dbRouter.clear();
        }
    }
}

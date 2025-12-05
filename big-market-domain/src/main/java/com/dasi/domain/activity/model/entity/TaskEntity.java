package com.dasi.domain.activity.model.entity;

import com.dasi.domain.activity.model.type.TaskState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskEntity {

    /** 用户id */
    private String userId;

    /** 消息id */
    private String messageId;

    /** 消息主题 */
    private String topic;

    /** 消息主体 */
    private String message;

    /** 任务状态 */
    private TaskState taskState;
}

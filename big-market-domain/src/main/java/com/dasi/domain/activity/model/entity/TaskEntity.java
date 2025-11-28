package com.dasi.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskEntity {

    /** 消息主题 */
    private String topic;

    /** 消息主体 */
    private String message;

    /** 任务状态 */
    private String taskState;

}

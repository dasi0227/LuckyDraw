package com.dasi.infrastructure.persistent.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Task {

    /** 自增id */
    private Long id;

    /** 用户id */
    private String userId;

    /** 消息id */
    private String messageId;

    /** 消息主题 */
    private String topic;

    /** 消息主体 */
    private String message;

    /** 任务状态 */
    private String taskState;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}
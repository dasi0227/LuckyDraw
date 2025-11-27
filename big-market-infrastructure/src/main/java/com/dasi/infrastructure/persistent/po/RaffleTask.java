package com.dasi.infrastructure.persistent.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RaffleTask {

    /** 自增id */
    private Long id;

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
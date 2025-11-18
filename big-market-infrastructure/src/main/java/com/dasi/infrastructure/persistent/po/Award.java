package com.dasi.infrastructure.persistent.po;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Award {

    /** 自增ID */
    private Integer id;

    /** 抽奖奖品ID（策略内部流转使用） */
    private Integer awardId;

    /** 奖品对接标识（对应发奖策略） */
    private String awardKey;

    /** 奖品配置信息（数量/模型/积分区间等） */
    private String awardConfig;

    /** 奖品内容描述 */
    private String awardDesc;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}

/*
CREATE TABLE `award` (
    `id`              int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `award_id`        int(8)           NOT NULL COMMENT '抽奖奖品ID（策略内流转使用）',
    `award_key`       varchar(32)      NOT NULL COMMENT '奖品对接标识（对应发奖策略）',
    `award_config`    varchar(32)      NOT NULL COMMENT '奖品配置值（数量/模型/积分范围等）',
    `award_desc`      varchar(128)     NOT NULL COMMENT '奖品内容描述',
    `create_time`     datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_award_id` (`award_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='奖品定义表：配置具体奖品的发放方式、配置信息和描述';
 */
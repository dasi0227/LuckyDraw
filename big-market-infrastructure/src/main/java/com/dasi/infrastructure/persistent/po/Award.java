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

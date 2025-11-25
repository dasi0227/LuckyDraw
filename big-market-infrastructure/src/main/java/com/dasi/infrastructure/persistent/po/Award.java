package com.dasi.infrastructure.persistent.po;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Award {

    /** 自增ID */
    private Integer id;

    /** 奖品ID */
    private Integer awardId;

    /** 奖品名称 */
    private String awardName;

    /** 奖品配置 */
    private String awardConfig;

    /** 奖品描述 */
    private String awardDesc;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}

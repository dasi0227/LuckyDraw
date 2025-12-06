package com.dasi.infrastructure.persistent.po;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserAccount {

    /** 自增ID */
    private Long id;

    /** 用户ID */
    private String userId;

    /** 用户状态 */
    private String userState;

    /** 用户积分 */
    private Integer userPoint;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}
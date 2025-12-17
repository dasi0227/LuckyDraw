package com.dasi.infrastructure.persistent.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {

    /** 自增id */
    private Long id;

    /** 用户id */
    private String userId;

    /** 用户密码 */
    private String password;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}

package com.dasi.domain.user.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    /** 用户id */
    private String userId;

    /** 加密后的密码 */
    private String password;

    /** 创建时间 */
    private LocalDateTime createTime;

}

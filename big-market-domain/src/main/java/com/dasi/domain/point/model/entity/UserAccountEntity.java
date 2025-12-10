package com.dasi.domain.point.model.entity;

import com.dasi.domain.award.model.type.UserState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountEntity {

    /** 用户ID */
    private String userId;

    /** 用户状态 */
    private UserState userState;

    /** 用户积分 */
    private Integer userPoint;

}

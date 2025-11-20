package com.dasi.domain.strategy.model.entity;

import com.dasi.domain.strategy.model.vo.RuleDecisionVO;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleResultEntity<T extends RuleResultEntity.RuleDataEntity> {

    /** 规则触发模型 */
    private String ruleModel;

    /** 规则结果信息 */
    private String code;
    private String info;

     /** 规则结果数据，继承自 RuleDataEntity */
    private T data;

    /** 结果数据需要继承的类（按需填充） */
    static public class RuleDataEntity {}

    /** 前置结果数据 */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static public class RuleDataBeforeEntity extends RuleDataEntity {
        /** 策略 ID */
        private Long strategyId;

        /** 奖品 ID */
        private Integer awardId;

        /** 权重值 */
        private String ruleWeight;
    }

    /** 后置结果数据 */
    static public class RuleDataAfterEntity extends RuleDataEntity {}


    // ---------------------- 静态方法 ----------------------
    public static <T extends RuleDataEntity> RuleResultEntity<T> allow() {
        return RuleResultEntity.<T>builder()
                .code(RuleDecisionVO.ALLOW.getCode())
                .info(RuleDecisionVO.ALLOW.getInfo())
                .build();
    }

    public static <T extends RuleDataEntity> RuleResultEntity<T> allow(T data) {
        RuleResultEntity<T> result = allow();
        result.data = data;
        return result;
    }
}

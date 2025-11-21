package com.dasi.domain.strategy.model.io;

import com.dasi.domain.strategy.model.enumeration.FilterDecision;
import lombok.*;

@SuppressWarnings("unused")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterResponse<T extends FilterResponse.FilterDataEntity> {

    /** 规则触发模型 */
    private String ruleModel;

    /** 规则结果信息 */
    private String code;
    private String info;

     /** 规则结果数据，继承自 FilterDataEntity */
    private T data;

    /** 结果数据需要继承的类（按需填充） */
    static public class FilterDataEntity {}

    /** 前置结果数据 */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static public class FilterBeforeEntity extends FilterDataEntity {
        /** 策略 ID */
        private Long strategyId;

        /** 奖品 ID */
        private Integer awardId;

        /** 权重值 */
        private String ruleWeight;
    }

//    /** 后置结果数据 */
//    @EqualsAndHashCode(callSuper = true)
//    @Data
//    @Builder
//    @NoArgsConstructor
//    @AllArgsConstructor
//    static public class RuleAfterEntity extends FilterDataEntity {}

    /** 中置结果数据 */
    @EqualsAndHashCode(callSuper = true)
    @Data
    static public class FilterDuringEntity extends FilterDataEntity {}


    // ---------------------- 静态方法 ----------------------
    public static <T extends FilterDataEntity> FilterResponse<T> allow() {
        return FilterResponse.<T>builder()
                .code(FilterDecision.ALLOW.getCode())
                .info(FilterDecision.ALLOW.getInfo())
                .build();
    }

    public static <T extends FilterDataEntity> FilterResponse<T> takeOver(String ruleModel) {
        return FilterResponse.<T>builder()
                .ruleModel(ruleModel)
                .code(FilterDecision.TAKE_OVER.getCode())
                .info(FilterDecision.TAKE_OVER.getInfo())
                .build();
    }

    public static <T extends FilterDataEntity> FilterResponse<T> takeOver(String ruleModel, T data) {
        FilterResponse<T> result = takeOver(ruleModel);
        result.data = data;
        return result;
    }
}

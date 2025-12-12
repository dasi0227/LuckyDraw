package com.dasi.types.constant;

public class ExceptionMessage {

    // 用户积分不够
    public static final String POINT_NOT_ENOUGH = "用户积分不足，请充值";

    // 账户不存在
    public static final String ACCOUNT_NOT_EXISTS = "用户不存在，请检查是否注册或登录成功";
    public static final String DAY_ACCOUNT_NOT_EXISTS = "用户不存在，请检查是否注册或登录成功";
    public static final String MONTH_ACCOUNT_NOT_EXISTS = "用户不存在，请检查是否注册或登录成功";

    // 用户抽奖次数不够
    public static final String TOTAL_SURPLUS_NOT_ENOUGH = "用户剩余总抽奖次数不足，请参与互动任务或使用积分兑换";
    public static final String MONTH_SURPLUS_NOT_ENOUGH = "用户剩余月抽奖次数不足，请参与互动任务或使用积分兑换";
    public static final String DAY_SURPLUS_NOT_ENOUGH = "用户剩余日抽奖次数不足，请参与互动任务或使用积分兑换";

    // 用户今日已参与活动
    public static final String BEHAVIOR_ALREADY_JOINED_TODAY = "用户已参与当前互动任务";

    // 活动已经结束
    public static final String ACTIVITY_ALREADY_ENDED = "活动已经结束，请关注顶部活动时间";

    // 活动还未开始
    public static final String ACTIVITY_NOT_STARTED = "活动尚未开始，请关注顶部活动时间";

}

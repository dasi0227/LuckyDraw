-- 主库 big_market
USE big_market;

DROP TABLE IF EXISTS activity;
CREATE TABLE activity (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    activity_id         BIGINT UNIQUE   NOT NULL COMMENT '活动id',
    strategy_id         BIGINT          NOT NULL COMMENT '策略id',
    activity_name       VARCHAR(32)     NOT NULL COMMENT '活动名称',
    activity_desc       VARCHAR(256)    NOT NULL COMMENT '活动描述',
    activity_state      VARCHAR(32)     NOT NULL COMMENT '活动状态',
    activity_begin_time DATETIME        NOT NULL COMMENT '活动开始时间',
    activity_end_time   DATETIME        NOT NULL COMMENT '活动结束时间',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT ='活动的元信息';
INSERT INTO activity (activity_id, strategy_id, activity_name, activity_desc, activity_state, activity_begin_time, activity_end_time)
VALUES (1001, 1001, '测试活动', '测试活动描述', 'UNDERWAY', '2025-11-25 00:00:00', '2025-12-25 00:00:00');

DROP TABLE IF EXISTS recharge_quota;
CREATE TABLE recharge_quota (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    quota_id          BIGINT UNIQUE   NOT NULL COMMENT '定量id',
    total_count       INT             NOT NULL COMMENT '总次数',
    month_count       INT             NOT NULL COMMENT '每月次数',
    day_count         INT             NOT NULL COMMENT '每日次数',
    create_time       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT ='权益的抽奖次数定量配置';
INSERT INTO recharge_quota (quota_id, total_count, month_count, day_count)
VALUES (2001, 5, 2, 1);

DROP TABLE IF EXISTS recharge_sku;
CREATE TABLE recharge_sku (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    sku_id            BIGINT UNIQUE   NOT NULL COMMENT '库存id',
    activity_id       BIGINT          NOT NULL COMMENT '活动id',
    quota_id          BIGINT          NOT NULL COMMENT '定量id',
    stock_allocate    INT             NOT NULL COMMENT '库存分配',
    stock_surplus     INT             NOT NULL COMMENT '库存剩余',
    create_time       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT ='每个活动不同权益的库存配置';
INSERT INTO recharge_sku (sku_id, activity_id, quota_id, stock_allocate, stock_surplus)
VALUES (3001, 1001, 2001, 100, 10);

-- 分库 big_market_01
USE big_market_01;

DROP TABLE IF EXISTS activity_account;
CREATE TABLE activity_account (
    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    user_id        VARCHAR(32)     NOT NULL COMMENT '用户id',
    activity_id    BIGINT          NOT NULL COMMENT '活动id',
    total_allocate INT             NOT NULL COMMENT '总分配',
    total_surplus  INT             NOT NULL COMMENT '总余额',
    month_allocate INT             NOT NULL COMMENT '月分配',
    month_surplus  INT             NOT NULL COMMENT '月余额',
    day_allocate   INT             NOT NULL COMMENT '天分配',
    day_surplus    INT             NOT NULL COMMENT '天余额',
    create_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uq_user_id_activity_id (user_id, activity_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT ='账户在每个活动获得的抽奖次数';

DROP TABLE IF EXISTS activity_account_month;
CREATE TABLE activity_account_month (
    id                  BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    activity_id         BIGINT           NOT NULL COMMENT '活动id',
    user_id             VARCHAR(32)      NOT NULL COMMENT '用户id',
    `month`             VARCHAR(32)      NOT NULL COMMENT 'yyyy-mm',
    month_allocate      INT              NOT NULL COMMENT '月次数',
    month_surplus       INT              NOT NULL COMMENT '月次数-剩余',
    create_time         datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
UNIQUE KEY uq_user_id_activity_id_month (user_id, activity_id, `month`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT ='当月的用户抽奖消耗';

DROP TABLE IF EXISTS activity_account_day;
CREATE TABLE activity_account_day (
    id                  BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    activity_id         BIGINT           NOT NULL COMMENT '活动id',
    user_id             VARCHAR(32)      NOT NULL COMMENT '用户id',
    `day`               VARCHAR(32)      NOT NULL COMMENT 'yyyy-mm-dd',
    day_allocate        INT              NOT NULL COMMENT '日次数',
    day_surplus         INT              NOT NULL COMMENT '日次数-剩余',
    create_time         datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uq_user_id_activity_id_day (user_id, activity_id, `day`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '当天的用户抽奖消耗';

DROP TABLE IF EXISTS task;
CREATE TABLE task (
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    user_id     VARCHAR(32)      NOT NULL COMMENT '用户id',
    message_id  VARCHAR(32)      NOT NULL COMMENT '消息id',
    topic       VARCHAR(32)      NOT NULL COMMENT '消息主题',
    message     VARCHAR(512)     NOT NULL COMMENT '消息主体',
    task_state  VARCHAR(32)      NOT NULL COMMENT '任务状态',
    create_time DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT ='任务表';

DROP TABLE IF EXISTS recharge_order_000;
CREATE TABLE recharge_order_000 (
    id                   BIGINT UNSIGNED    NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    order_id             VARCHAR(32) UNIQUE NOT NULL COMMENT '充值订单id',
    biz_id               VARCHAR(32) UNIQUE NOT NULL COMMENT '业务幂等id',
    activity_id          BIGINT             NOT NULL COMMENT '活动id',
    quota_id             BIGINT             NOT NULL COMMENT '定量id',
    strategy_id          BIGINT             NOT NULL COMMENT '策略id',
    user_id              VARCHAR(32)        NOT NULL COMMENT '用户id',
    sku_id               BIGINT             NOT NULL COMMENT '库存id',
    total_count          INT                NOT NULL COMMENT '本次下单获得的总次数',
    month_count          INT                NOT NULL COMMENT '本次下单获得的月次数',
    day_count            INT                NOT NULL COMMENT '本次下单获得的日次数',
    recharge_state       VARCHAR(32)        NOT NULL COMMENT '充值状态',
    recharge_time        DATETIME           NOT NULL COMMENT '充值时间',
    create_time          DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time          DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT ='充值抽奖次数订单';

DROP TABLE IF EXISTS raffle_order_000;
CREATE TABLE raffle_order_000 (
    id                 BIGINT UNSIGNED    NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    order_id           VARCHAR(32) UNIQUE NOT NULL COMMENT '订单id',
    user_id            VARCHAR(32)        NOT NULL COMMENT '用户id',
    activity_id        BIGINT             NOT NULL COMMENT '活动id',
    strategy_id        BIGINT             NOT NULL COMMENT '策略id',
    raffle_state       VARCHAR(32)        NOT NULL COMMENT '抽奖状态',
    raffle_time        DATETIME           NOT NULL COMMENT '抽奖时间',
    create_time        DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time        DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_user_id_activity_id (user_id, activity_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT ='抽奖订单表';

DROP TABLE IF EXISTS raffle_award_000;
CREATE TABLE raffle_award_000 (
    id          BIGINT UNSIGNED    NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    user_id     VARCHAR(32)        NOT NULL COMMENT '用户id',
    activity_id BIGINT             NOT NULL COMMENT '活动id',
    award_id    INT                NOT NULL COMMENT '奖品id',
    strategy_id BIGINT             NOT NULL COMMENT '策略id',
    order_id    VARCHAR(32)        NOT NULL COMMENT '订单id',
    award_name  VARCHAR(32)        NOT NULL COMMENT '奖品标题',
    award_time  DATETIME           NOT NULL COMMENT '中奖时间',
    award_state VARCHAR(32)        NOT NULL COMMENT '奖品发放状态',
    create_time DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT ='用户中奖记录表';

DROP TABLE IF EXISTS recharge_order_001;
CREATE TABLE IF NOT EXISTS recharge_order_001 LIKE recharge_order_000;
DROP TABLE IF EXISTS recharge_order_002;
CREATE TABLE IF NOT EXISTS recharge_order_002 LIKE recharge_order_000;
DROP TABLE IF EXISTS recharge_order_003;
CREATE TABLE IF NOT EXISTS recharge_order_003 LIKE recharge_order_000;

DROP TABLE IF EXISTS raffle_award_001;
CREATE TABLE IF NOT EXISTS raffle_award_001 LIKE raffle_award_000;
DROP TABLE IF EXISTS raffle_award_002;
CREATE TABLE IF NOT EXISTS raffle_award_002 LIKE raffle_award_000;
DROP TABLE IF EXISTS raffle_award_003;
CREATE TABLE IF NOT EXISTS raffle_award_003 LIKE raffle_award_000;

DROP TABLE IF EXISTS raffle_order_001;
CREATE TABLE IF NOT EXISTS raffle_order_001 LIKE raffle_order_000;
DROP TABLE IF EXISTS raffle_order_002;
CREATE TABLE IF NOT EXISTS raffle_order_002 LIKE raffle_order_000;
DROP TABLE IF EXISTS raffle_order_003;
CREATE TABLE IF NOT EXISTS raffle_order_003 LIKE raffle_order_000;

-- 分库 big_market_02
USE big_market_02;

DROP TABLE IF EXISTS activity_account;
CREATE TABLE activity_account LIKE big_market_01.activity_account;
DROP TABLE IF EXISTS activity_account_day;
CREATE TABLE activity_account_day LIKE big_market_01.activity_account_day;
DROP TABLE IF EXISTS activity_account_month;
CREATE TABLE activity_account_month LIKE big_market_01.activity_account_month;
DROP TABLE IF EXISTS task;
CREATE TABLE task LIKE big_market_01.task;

DROP TABLE IF EXISTS recharge_order_000;
CREATE TABLE recharge_order_000 LIKE big_market_01.recharge_order_000;
DROP TABLE IF EXISTS recharge_order_001;
CREATE TABLE recharge_order_001 LIKE big_market_01.recharge_order_000;
DROP TABLE IF EXISTS recharge_order_002;
CREATE TABLE recharge_order_002 LIKE big_market_01.recharge_order_000;
DROP TABLE IF EXISTS recharge_order_003;
CREATE TABLE recharge_order_003 LIKE big_market_01.recharge_order_000;

DROP TABLE IF EXISTS raffle_award_000;
CREATE TABLE IF NOT EXISTS raffle_award_000 LIKE big_market_01.raffle_award_000;
DROP TABLE IF EXISTS raffle_award_001;
CREATE TABLE IF NOT EXISTS raffle_award_001 LIKE big_market_01.raffle_award_000;
DROP TABLE IF EXISTS raffle_award_002;
CREATE TABLE IF NOT EXISTS raffle_award_002 LIKE big_market_01.raffle_award_000;
DROP TABLE IF EXISTS raffle_award_003;
CREATE TABLE IF NOT EXISTS raffle_award_003 LIKE big_market_01.raffle_award_000;

DROP TABLE IF EXISTS raffle_order_000;
CREATE TABLE IF NOT EXISTS raffle_order_000 LIKE big_market_01.raffle_order_000;
DROP TABLE IF EXISTS raffle_order_001;
CREATE TABLE IF NOT EXISTS raffle_order_001 LIKE big_market_01.raffle_order_000;
DROP TABLE IF EXISTS raffle_order_002;
CREATE TABLE IF NOT EXISTS raffle_order_002 LIKE big_market_01.raffle_order_000;
DROP TABLE IF EXISTS raffle_order_003;
CREATE TABLE IF NOT EXISTS raffle_order_003 LIKE big_market_01.raffle_order_000;

USE big_market;
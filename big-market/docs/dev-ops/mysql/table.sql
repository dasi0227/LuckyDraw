DROP DATABASE IF EXISTS big_market_table;
CREATE DATABASE IF NOT EXISTS big_market_table DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE big_market_table;

/* =======================================
任务表：存储需要发往 MQ 的信息
======================================= */
DROP TABLE IF EXISTS task;
CREATE TABLE task
(
    id          BIGINT UNSIGNED    NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    message_id  VARCHAR(64) UNIQUE NOT NULL COMMENT '消息id',
    user_id     VARCHAR(32)        NOT NULL COMMENT '用户id',
    topic       VARCHAR(32)        NOT NULL COMMENT '消息主题',
    message     VARCHAR(512)       NOT NULL COMMENT '消息主体',
    task_state  VARCHAR(32)        NOT NULL COMMENT '任务状态',
    create_time DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='任务表';

/* =======================================
用户表：存储登陆注册的用户
======================================= */
DROP TABLE IF EXISTS user;
CREATE TABLE user
(
    id          BIGINT UNSIGNED    NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    user_id     VARCHAR(32) UNIQUE NOT NULL COMMENT '用户id',
    password    VARCHAR(64)        NOT NULL COMMENT '用户密码',
    create_time DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='用户表';

/* =======================================
交易表：用户积分交易的信息
======================================= */
DROP TABLE IF EXISTS trade;
CREATE TABLE IF NOT EXISTS trade
(
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    trade_id    BIGINT UNIQUE NOT NULL COMMENT '交易id',
    activity_id BIGINT        NOT NULL COMMENT '活动id',
    trade_type  VARCHAR(32)   NOT NULL COMMENT '交易类型',
    trade_point VARCHAR(32)   NOT NULL COMMENT '交易积分',
    trade_value VARCHAR(32)   NOT NULL COMMENT '交易结果',
    trade_name  VARCHAR(32)   NOT NULL COMMENT '交易名称',
    trade_desc  VARCHAR(256)  NOT NULL COMMENT '交易描述',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uq_trade_id_activity_id (trade_id, activity_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='交易表';

/* =======================================
行为表：用户行为的信息
======================================= */
DROP TABLE IF EXISTS behavior;
CREATE TABLE IF NOT EXISTS behavior
(
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    activity_id   BIGINT       NOT NULL COMMENT '活动id',
    behavior_type VARCHAR(32)  NOT NULL COMMENT '行为类型',
    behavior_name VARCHAR(32)  NOT NULL COMMENT '行为名称',
    reward_type   VARCHAR(32)  NOT NULL COMMENT '奖励类型',
    reward_value  VARCHAR(32)  NOT NULL COMMENT '奖励值',
    reward_desc   VARCHAR(256) NOT NULL COMMENT '奖励描述',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='行为表';

/* =======================================
奖品表：最终发放到用户的奖品信息
======================================= */
DROP TABLE IF EXISTS award;
CREATE TABLE award
(
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    award_id    BIGINT UNIQUE NOT NULL COMMENT '奖品id',
    award_type  VARCHAR(32)   NOT NULL COMMENT '奖品类型',
    award_value VARCHAR(32)   NOT NULL COMMENT '奖品配置',
    award_name  VARCHAR(32)   NOT NULL COMMENT '奖品名称',
    award_desc  VARCHAR(256)  NOT NULL COMMENT '奖品描述',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='奖品表';

/* =======================================
策略表：与活动关联的策略信息
======================================= */
DROP TABLE IF EXISTS strategy;
CREATE TABLE strategy
(
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    strategy_id   BIGINT UNIQUE NOT NULL UNIQUE COMMENT '策略id',
    strategy_desc VARCHAR(256)  NOT NULL COMMENT '策略描述',
    rule_models   VARCHAR(256)  NULL COMMENT '规则列表',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='策略表';

/* =======================================
策略规则表：与策略关联的前置检查信息
======================================= */
DROP TABLE IF EXISTS strategy_rule;
CREATE TABLE strategy_rule
(
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    strategy_id BIGINT       NOT NULL COMMENT '抽奖策略id',
    rule_desc   VARCHAR(256) NOT NULL COMMENT '规则描述',
    rule_model  VARCHAR(32)  NOT NULL COMMENT '规则名',
    rule_value  VARCHAR(512) NOT NULL COMMENT '规则值',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uq_strategy_id_rule_model (strategy_id, rule_model)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='策略规则表';

/* =======================================
策略奖品表：与策略和奖品关联的后置检查信息
======================================= */
DROP TABLE IF EXISTS strategy_award;
CREATE TABLE strategy_award
(
    id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    strategy_id    BIGINT         NOT NULL COMMENT '抽奖策略id',
    award_id       BIGINT         NOT NULL COMMENT '抽奖奖品id',
    tree_id        VARCHAR(32)    NULL COMMENT '规则树id',
    award_title    VARCHAR(256)   NULL COMMENT '抽奖奖品标题',
    award_allocate INT            NOT NULL COMMENT '奖品库存总量',
    award_surplus  INT            NOT NULL COMMENT '奖品库存余量',
    award_rate     DECIMAL(10, 6) NOT NULL COMMENT '奖品中奖概率',
    award_index    INT            NOT NULL COMMENT '奖品排列序号',
    create_time    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uq_strategy_id_award_id (strategy_id, award_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='策略奖品表';

/* =======================================
规则树表：与策略奖品关联的规则信息
======================================= */
DROP TABLE IF EXISTS rule_tree;
CREATE TABLE rule_tree
(
    id          BIGINT UNSIGNED    NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    tree_id     VARCHAR(32) UNIQUE NOT NULL COMMENT '规则树id',
    tree_name   VARCHAR(32)        NOT NULL COMMENT '规则树名称',
    tree_desc   VARCHAR(256)       NOT NULL COMMENT '规则树描述',
    tree_root   VARCHAR(32)        NOT NULL COMMENT '根节点规则',
    create_time DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='规则树表';

/* =======================================
规则树节点表：与规则树关联的检查逻辑
======================================= */
DROP TABLE IF EXISTS rule_node;
CREATE TABLE rule_node
(
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    tree_id     VARCHAR(32)     NOT NULL COMMENT '规则树id',
    rule_desc   VARCHAR(256)    NOT NULL COMMENT '规则树节点描述',
    rule_model  VARCHAR(32)     NOT NULL COMMENT '规则树节点模型',
    rule_value  VARCHAR(512)    NULL     DEFAULT NULL COMMENT '规则树节点值',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uq_tree_id_rule_model (tree_id, rule_model)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='规则树节点表';

/* =======================================
规则树边表：与规则树和规则树节点关联的转移逻辑
======================================= */
DROP TABLE IF EXISTS rule_edge;
CREATE TABLE rule_edge
(
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    tree_id           VARCHAR(32)     NOT NULL COMMENT '规则树id',
    rule_node_from    VARCHAR(32)     NOT NULL COMMENT '规则边起点',
    rule_node_to      VARCHAR(32)     NOT NULL COMMENT '规则边终点',
    rule_check_type   VARCHAR(32)     NOT NULL COMMENT '规则检查类型',
    rule_check_result VARCHAR(32)     NOT NULL COMMENT '规则检查结果',
    create_time       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uq_tree_id_from_to (tree_id, rule_node_from, rule_node_to)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='规则树边表';

/* =======================================
活动表：用户参与抽奖的活动信息
======================================= */
DROP TABLE IF EXISTS activity;
CREATE TABLE activity
(
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
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='活动的元信息';


/* =======================================
权益库存表：权益的库存信息
======================================= */
DROP TABLE IF EXISTS activity_sku;
CREATE TABLE activity_sku
(
    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    sku_id         BIGINT UNIQUE   NOT NULL COMMENT '库存id',
    activity_id    BIGINT          NOT NULL COMMENT '活动id',
    count          INT             NOT NULL COMMENT '抽奖次数',
    stock_allocate INT             NOT NULL COMMENT '库存分配',
    stock_surplus  INT             NOT NULL COMMENT '库存剩余',
    create_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uq_sku_id_activity_id (sku_id, activity_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='权益库存表';

/* =======================================
活动账户表：活动获得的抽奖次数记录
======================================= */
DROP TABLE IF EXISTS activity_account;
CREATE TABLE activity_account
(
    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    user_id        VARCHAR(32)     NOT NULL COMMENT '用户id',
    activity_id    BIGINT          NOT NULL COMMENT '活动id',
    account_point  INT             NOT NULL COMMENT '用户积分',
    account_luck   INT             NOT NULL COMMENT '用户幸运值',
    month_limit    INT             NOT NULL COMMENT '月上限',
    day_limit      INT             NOT NULL COMMENT '天上限',
    total_allocate INT             NOT NULL COMMENT '总分配',
    total_surplus  INT             NOT NULL COMMENT '总抽奖次数',
    create_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uq_user_id_activity_id (user_id, activity_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='活动账户表';

/* =======================================
活动账户月表：活动每月获得的抽奖次数
======================================= */
DROP TABLE IF EXISTS activity_account_month;
CREATE TABLE activity_account_month
(
    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    user_id        VARCHAR(32)     NOT NULL COMMENT '用户id',
    activity_id    BIGINT          NOT NULL COMMENT '活动id',
    month_key      VARCHAR(32)     NOT NULL COMMENT 'yyyy-mm',
    month_limit    INT             NOT NULL COMMENT '月上限',
    month_allocate INT             NOT NULL COMMENT '月次数-分配',
    month_surplus  INT             NOT NULL COMMENT '月次数-剩余',
    create_time    datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uq_user_id_activity_id_month (user_id, activity_id, month_key)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='活动账户月表';

/* =======================================
活动账户天表：活动每天获得的抽奖次数
======================================= */
DROP TABLE IF EXISTS activity_account_day;
CREATE TABLE activity_account_day
(
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    user_id      VARCHAR(32)     NOT NULL COMMENT '用户id',
    activity_id  BIGINT          NOT NULL COMMENT '活动id',
    day_key      VARCHAR(32)     NOT NULL COMMENT 'yyyy-mm-dd',
    day_limit    INT             NOT NULL COMMENT '天上限',
    day_allocate INT             NOT NULL COMMENT '日次数-分配',
    day_surplus  INT             NOT NULL COMMENT '日次数-剩余',
    create_time  datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uq_user_id_activity_id_day (user_id, activity_id, day_key)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动账户天表';

/* =======================================
中奖结果表：中奖的记录
======================================= */
DROP TABLE IF EXISTS activity_award;
CREATE TABLE activity_award
(
    id          BIGINT UNSIGNED    NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    order_id    VARCHAR(64) UNIQUE NOT NULL COMMENT '订单id',
    user_id     VARCHAR(32)        NOT NULL COMMENT '用户id',
    activity_id BIGINT             NOT NULL COMMENT '活动id',
    award_id    BIGINT             NOT NULL COMMENT '奖品id',
    award_name  VARCHAR(32)        NOT NULL COMMENT '奖品标题',
    award_state VARCHAR(32)        NOT NULL COMMENT '奖品发放状态',
    award_time  DATETIME           NOT NULL COMMENT '中奖时间',
    create_time DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='中奖结果表';

/* =======================================
获奖结果表：获奖的记录
======================================= */
DROP TABLE IF EXISTS user_award;
CREATE TABLE user_award
(
    id             BIGINT UNSIGNED    NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    order_id       VARCHAR(64) UNIQUE NOT NULL COMMENT '订单id',
    user_id        VARCHAR(32)        NOT NULL COMMENT '用户id',
    award_id       VARCHAR(32)        NOT NULL COMMENT '奖品id',
    activity_id    VARCHAR(32)        NOT NULL COMMENT '活动id',
    award_source   VARCHAR(32)        NOT NULL COMMENT '奖品来源',
    award_name     VARCHAR(32)        NOT NULL COMMENT '奖品名称',
    award_desc     VARCHAR(256)       NOT NULL COMMENT '奖品描述',
    award_deadline DATETIME           NULL COMMENT '奖品期限',
    award_time     DATETIME           NOT NULL COMMENT '奖品时间',
    create_time    DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='获奖结果表';

/* =======================================
充值表：权益的充值记录
======================================= */
DROP TABLE IF EXISTS recharge_order;
CREATE TABLE recharge_order
(
    id             BIGINT UNSIGNED    NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    order_id       VARCHAR(64) UNIQUE NOT NULL COMMENT '充值订单id',
    biz_id         VARCHAR(64) UNIQUE NOT NULL COMMENT '业务幂等id',
    user_id        VARCHAR(32)        NOT NULL COMMENT '用户id',
    activity_id    BIGINT             NOT NULL COMMENT '活动id',
    sku_id         BIGINT             NOT NULL COMMENT '库存id',
    count          INT                NOT NULL COMMENT '本次下单获得的次数',
    recharge_state VARCHAR(32)        NOT NULL COMMENT '充值状态',
    recharge_time  DATETIME           NOT NULL COMMENT '充值时间',
    create_time    DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='充值抽奖次数订单';

/* =======================================
抽奖订单表：抽奖行为的记录
======================================= */
DROP TABLE IF EXISTS raffle_order;
CREATE TABLE raffle_order
(
    id           BIGINT UNSIGNED    NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    order_id     VARCHAR(64) UNIQUE NOT NULL COMMENT '订单id',
    user_id      VARCHAR(32)        NOT NULL COMMENT '用户id',
    activity_id  BIGINT             NOT NULL COMMENT '活动id',
    strategy_id  BIGINT             NOT NULL COMMENT '策略id',
    raffle_state VARCHAR(32)        NOT NULL COMMENT '抽奖状态',
    raffle_time  DATETIME           NOT NULL COMMENT '抽奖时间',
    create_time  DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='抽奖订单表';

/* =======================================
奖励订单表：用户执行行为获得的奖励
======================================= */
DROP TABLE IF EXISTS reward_order;
CREATE TABLE IF NOT EXISTS reward_order
(
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    order_id      VARCHAR(64) UNIQUE NOT NULL COMMENT '订单id',
    biz_id        VARCHAR(64) UNIQUE NOT NULL COMMENT '业务id',
    user_id       VARCHAR(32)        NOT NULL COMMENT '用户id',
    activity_id   BIGINT             NOT NULL COMMENT '活动id',
    behavior_type VARCHAR(32)        NOT NULL COMMENT '行为类型',
    reward_type   VARCHAR(32)        NOT NULL COMMENT '奖励类型',
    reward_value  VARCHAR(32)        NOT NULL COMMENT '奖励值',
    reward_state  VARCHAR(32)        NOT NULL COMMENT '奖励状态',
    reward_desc   VARCHAR(256)       NOT NULL COMMENT '奖励描述',
    reward_time   DATETIME           NOT NULL COMMENT '奖励时间',
    create_time   DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='奖励订单表';

/* =======================================
交易订单表：用户使用积分的订单
======================================= */
DROP TABLE IF EXISTS trade_order;
CREATE TABLE IF NOT EXISTS trade_order
(
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    order_id    VARCHAR(64) UNIQUE NOT NULL COMMENT '订单id',
    biz_id      VARCHAR(64) UNIQUE NOT NULL COMMENT '业务id',
    user_id     VARCHAR(32)        NOT NULL COMMENT '用户id',
    trade_id    BIGINT             NOT NULL COMMENT '交易id',
    activity_id BIGINT             NOT NULL COMMENT '活动id',
    trade_type  VARCHAR(32)        NOT NULL COMMENT '交易类型',
    trade_state VARCHAR(32)        NOT NULL COMMENT '交易状态',
    trade_time  DATETIME           NOT NULL COMMENT '交易时间',
    create_time DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='交易订单表';
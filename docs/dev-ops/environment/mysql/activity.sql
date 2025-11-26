-- 主库 big_market
USE big_market;

/* 活动表 */
DROP TABLE IF EXISTS activity;
CREATE TABLE `activity` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `activity_id` BIGINT NOT NULL,
    `activity_name` VARCHAR(64) NOT NULL,
    `activity_desc` VARCHAR(128) NOT NULL,
    `begin_time` DATETIME NOT NULL,
    `end_time` DATETIME NOT NULL,
    `activity_count_id` BIGINT NOT NULL,
    `strategy_id` BIGINT NOT NULL,
    `state` VARCHAR(32) NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uq_activity_id` (`activity_id`),
    KEY `idx_begin_date_time` (`begin_time`),
    KEY `idx_end_date_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖活动表';

/* 活动次数配置 */
DROP TABLE IF EXISTS activity_count;
CREATE TABLE `activity_count` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `activity_count_id` BIGINT NOT NULL,
    `total_count` INT NOT NULL,
    `day_count` INT NOT NULL,
    `month_count` INT NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uq_activity_count_id` (`activity_count_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖活动次数配置表';

/* 基本单位 */
DROP TABLE IF EXISTS activity_sku;
CREATE TABLE `activity_sku` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增ID',
    `sku` BIGINT NOT NULL COMMENT 'SKU编号（抽奖次数商品）',
    `activity_id` BIGINT NOT NULL COMMENT '活动ID',
    `activity_count_id` BIGINT NOT NULL COMMENT '活动次数编号',
    `stock_amount` INT NOT NULL COMMENT '库存总量',
    `stock_surplus` INT NOT NULL COMMENT '库存剩余',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uq_sku` (`sku`),
    KEY `idx_activity_id` (`activity_id`),
    KEY `idx_activity_count_id` (`activity_count_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖活动SKU表';

/* 活动账户 */
DROP TABLE IF EXISTS activity_account;
CREATE TABLE `activity_account` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id` VARCHAR(32) NOT NULL,
    `activity_id` BIGINT NOT NULL,
    `total_amount` INT NOT NULL,
    `total_surplus` INT NOT NULL,
    `day_amount` INT NOT NULL,
    `day_surplus` INT NOT NULL,
    `month_amount` INT NOT NULL,
    `month_surplus` INT NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uq_user_id_activity_id` (`user_id`,`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖活动账户表';

/* 订单 */
DROP TABLE IF EXISTS activity_order;
CREATE TABLE activity_order (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增ID',
    order_id VARCHAR(32) NOT NULL COMMENT '订单ID',
    biz_id VARCHAR(32) NOT NULL COMMENT '业务ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    sku BIGINT NOT NULL COMMENT '活动SKU编码',
    strategy_id BIGINT NOT NULL COMMENT '抽奖策略ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    activity_name VARCHAR(64) NOT NULL COMMENT '活动名称',
    total_count INT NOT NULL COMMENT '总次数（本次下单获得）',
    month_count INT NOT NULL COMMENT '月次数（本次下单获得）',
    day_count INT NOT NULL COMMENT '日次数（本次下单获得）',
    state VARCHAR(32) NOT NULL COMMENT '订单状态（not_used、used、expire）',
    order_time DATETIME NOT NULL COMMENT '下单时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uq_order_id (order_id),
    UNIQUE KEY uq_biz_id (biz_id),
    KEY idx_user_sku_status (user_id, sku, state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖活动订单表';

-- 分库 big_market_01 分表 activity_order_000~003
DROP DATABASE IF EXISTS big_market_01;
CREATE DATABASE big_market_01 DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE big_market_01;
CREATE TABLE activity_order_000 LIKE big_market.activity_order;
CREATE TABLE activity_order_001 LIKE big_market.activity_order;
CREATE TABLE activity_order_002 LIKE big_market.activity_order;
CREATE TABLE activity_order_003 LIKE big_market.activity_order;
CREATE TABLE activity_account LIKE big_market.activity_account;
-- 分库 big_market_01 分表 activity_order_000~003
DROP DATABASE IF EXISTS big_market_02;
CREATE DATABASE big_market_02 DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE big_market_02;
CREATE TABLE activity_order_000 LIKE big_market.activity_order;
CREATE TABLE activity_order_001 LIKE big_market.activity_order;
CREATE TABLE activity_order_002 LIKE big_market.activity_order;
CREATE TABLE activity_order_003 LIKE big_market.activity_order;
CREATE TABLE activity_account LIKE big_market.activity_account;

USE big_market;

INSERT INTO activity (activity_id, activity_name, activity_desc, begin_time, end_time, activity_count_id, strategy_id, state)
VALUES
    (1001, '测试活动', '测试活动描述', '2025-11-25 00:00:00', '2025-12-25 00:00:00', 1001, 100001, 'created');

INSERT INTO activity_count (activity_count_id, total_count, day_count, month_count)
VALUES
    (1001, 5, 1, 2);

INSERT INTO activity_sku (sku, activity_id, activity_count_id, stock_amount, stock_surplus)
VALUES
    (2001, 1001, 1001, 100, 100);
USE big_market;

DROP TABLE IF EXISTS behavior;
CREATE TABLE IF NOT EXISTS behavior (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    behavior_id     BIGINT UNIQUE NOT NULL COMMENT '行为id',
    behavior_desc   VARCHAR(256)  NOT NULL COMMENT '行为描述',
    behavior_type   VARCHAR(32)   NOT NULL COMMENT '行为类型',
    behavior_reward VARCHAR(32)   NOT NULL COMMENT '行为奖励',
    behavior_config VARCHAR(32)   NOT NULL COMMENT '行为奖励值',
    behavior_state  VARCHAR(32)   NOT NULL COMMENT '行为状态',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT ='行为表';
INSERT INTO behavior (behavior_id, behavior_desc, behavior_type, behavior_reward, behavior_config, behavior_state)
VALUES (6001, '签到，获得抽奖次数', '签到', 'sku', '3001', 'opened'),
       (6002, '点赞，获得 1 积分', '点赞', 'point', '1', 'opened');

USE big_market_01;

DROP TABLE IF EXISTS behavior_order_000;
CREATE TABLE IF NOT EXISTS behavior_order_000 (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '自增id',
    order_id        VARCHAR(32) UNIQUE NOT NULL COMMENT '订单id',
    biz_id          VARCHAR(32) UNIQUE NOT NULL COMMENT '业务id',
    user_id         VARCHAR(32)        NOT NULL COMMENT '用户id',
    behavior_id     BIGINT             NOT NULL COMMENT '行为id',
    behavior_type   VARCHAR(32)        NOT NULL COMMENT '行为类型',
    behavior_reward VARCHAR(32)        NOT NULL COMMENT '行为奖励',
    behavior_config VARCHAR(32)        NOT NULL COMMENT '行为奖励值',
    create_time     DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT ='行为订单表';

DROP TABLE IF EXISTS behavior_order_001;
CREATE TABLE IF NOT EXISTS behavior_order_001 LIKE behavior_order_000;
DROP TABLE IF EXISTS behavior_order_002;
CREATE TABLE IF NOT EXISTS behavior_order_002 LIKE behavior_order_000;
DROP TABLE IF EXISTS behavior_order_003;
CREATE TABLE IF NOT EXISTS behavior_order_003 LIKE behavior_order_000;

USE big_market_02;

DROP TABLE IF EXISTS behavior_order_001;
CREATE TABLE behavior_order_001 LIKE big_market_01.behavior_order_000;
DROP TABLE IF EXISTS behavior_order_002;
CREATE TABLE behavior_order_002 LIKE big_market_01.behavior_order_000;
DROP TABLE IF EXISTS behavior_order_003;
CREATE TABLE behavior_order_003 LIKE big_market_01.behavior_order_000;
DROP TABLE IF EXISTS behavior_order_004;
CREATE TABLE behavior_order_004 LIKE big_market_01.behavior_order_000;

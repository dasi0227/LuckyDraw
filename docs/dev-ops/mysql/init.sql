/* =============
主库 big_market
============= */
DROP DATABASE IF EXISTS big_market;
CREATE DATABASE IF NOT EXISTS big_market DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE big_market;

DROP TABLE IF EXISTS strategy;
CREATE TABLE strategy LIKE big_market_table.strategy;
INSERT INTO strategy (strategy_id, strategy_desc, rule_models)
VALUES (1001, '【测试策略1001】黑名单+权重', 'RULE_BLACKLIST,RULE_WEIGHT');

DROP TABLE IF EXISTS award;
CREATE TABLE award LIKE big_market_table.award;
INSERT INTO award (award_id, award_type, award_name, award_config, award_desc)
VALUES (2001, 'FIXED_USER_POINT', '【黑名单奖品2001】1积分', '10', '黑名单奖品'),
       (2002, 'FIXED_USER_POINT', '【兜底奖品2002】10积分', '10', '兜底奖品'),
       (2011, 'RANDOM_USER_POINT', '【积分奖品2011】随机积分', '1,50', '积分奖品'),
       (2012, 'RANDOM_USER_POINT', '【积分奖品2012】随机积分', '1,100', '积分奖品'),
       (2013, 'DISCOUNT_COUPON', '【普通奖品2013】5元优惠券', NULL, '普通奖品'),
       (2014, 'PHYSICAL_PRIZE', '【稀有奖品2014】IPhone17', NULL, '稀有奖品');

DROP TABLE IF EXISTS strategy_rule;
CREATE TABLE strategy_rule LIKE big_market_table.strategy_rule;
INSERT INTO strategy_rule (strategy_id, rule_model, rule_value, rule_desc)
VALUES (1001, 'RULE_BLACKLIST', '2001:dasi', '黑名单：dasi'),
       (1001, 'RULE_WEIGHT', '4000:2011,2012,2013 5000:2011,2014', '权重：4000 和 5000');

DROP TABLE IF EXISTS strategy_award;
CREATE TABLE strategy_award LIKE big_market_table.strategy_award;
INSERT INTO strategy_award (strategy_id, award_id, tree_id, award_title, award_allocate, award_surplus, award_rate, award_index)
VALUES (1001, 2011, 'TREE_STOCK', '【测试策略1001测试奖品2011】走TREE_STOCK', 60, 60, 0.6000, 1),
       (1001, 2012, 'TREE_STOCK', '【测试策略1001测试奖品2012】走TREE_STOCK', 30, 30, 0.3000, 2),
       (1001, 2013, 'TREE_STOCK', '【测试策略1001测试奖品2013】走TREE_LOCK', 7, 7, 0.0700, 3),
       (1001, 2014, 'TREE_LOCK', '【测试策略1001测试奖品2014】走TREE_LOCK', 3, 3, 0.0300, 4);

DROP TABLE IF EXISTS rule_tree;
CREATE TABLE rule_tree LIKE big_market_table.rule_tree;
INSERT INTO rule_tree (tree_id, tree_name, tree_desc, tree_root)
VALUES ('TREE_LOCK', '测试规则树TREE_LOCK', '先走 lock 再走 stock', 'RULE_LOCK'),
       ('TREE_STOCK', '测试规则树TREE_STOCK', '直接走 stock', 'RULE_STOCK');

DROP TABLE IF EXISTS rule_node;
CREATE TABLE rule_node LIKE big_market_table.rule_node;
INSERT INTO rule_node (tree_id, rule_model, rule_desc, rule_value)
VALUES ('TREE_LOCK', 'RULE_LOCK', '【RULE_LOCK】达到10次才解锁', '10'),
       ('TREE_LOCK', 'RULE_STOCK', '【RULE_STOCK】库存扣减1', NULL),
       ('TREE_LOCK', 'RULE_LUCK', '【RULE_LUCK】兜底奖品2002', '2002'),
       ('TREE_STOCK', 'RULE_STOCK', '【RULE_STOCK】库存扣减1', NULL),
       ('TREE_STOCK', 'RULE_LUCK', '【RULE_LUCK】兜底奖品2002', '2002');

DROP TABLE IF EXISTS rule_edge;
CREATE TABLE rule_edge LIKE big_market_table.rule_edge;
INSERT INTO rule_edge (tree_id, rule_node_from, rule_node_to, rule_check_type, rule_check_result)
VALUES ('TREE_LOCK', 'RULE_LOCK', 'RULE_STOCK', 'EQUAL', 'PERMIT'),
       ('TREE_LOCK', 'RULE_LOCK', 'RULE_LUCK', 'EQUAL', 'CAPTURE'),
       ('TREE_LOCK', 'RULE_STOCK', 'RULE_LUCK', 'EQUAL', 'CAPTURE'),
       ('TREE_STOCK', 'RULE_STOCK', 'RULE_LUCK', 'EQUAL', 'CAPTURE');

DROP TABLE IF EXISTS activity;
CREATE TABLE activity LIKE big_market_table.activity;
INSERT INTO activity (activity_id, strategy_id, activity_name, activity_desc, activity_state, activity_begin_time, activity_end_time)
VALUES (10001, 1001, '测试活动', '测试活动描述', 'UNDERWAY', '2025-11-25 00:00:00', '2025-12-25 00:00:00');

DROP TABLE IF EXISTS activity_sku;
CREATE TABLE activity_sku LIKE big_market_table.activity_sku;
INSERT INTO activity_sku (sku_id, activity_id, count, stock_allocate, stock_surplus)
VALUES (30001, 10001, 1, 100, 100),
       (30002, 10001, 3, 100, 100);

DROP TABLE IF EXISTS behavior;
CREATE TABLE behavior LIKE big_market_table.behavior;
INSERT INTO behavior
(activity_id, behavior_type, behavior_state, reward_type, reward_value, reward_desc)
VALUES (10001, 'SIGN', 'AVAILABLE', 'SKU', '30001', '签到：获得抽奖次数（2，1，1）'),
       (10001, 'SIGN', 'AVAILABLE', 'POINT', '2', '签到：获得 2 积分'),
       (10001, 'LIKE', 'AVAILABLE', 'POINT', '1', '点赞：获得 1 积分'),
       (10001, 'COMMENT', 'AVAILABLE', 'POINT', '5', '评论：获得 5 积分'),
       (10001, 'SHARE', 'AVAILABLE', 'SKU', '30002', '转发：获得抽奖次数（3，3，3）'),
       (10001, 'SHARE', 'AVAILABLE', 'POINT', '5', '转发：获得 5 积分');

/* =============
分库 big_market_01
============= */
DROP DATABASE IF EXISTS big_market_01;
CREATE DATABASE IF NOT EXISTS big_market_01 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE big_market_01;
-- 任务
DROP TABLE IF EXISTS task;
CREATE TABLE task LIKE big_market_table.task;
-- 账户
DROP TABLE IF EXISTS user_account;
CREATE TABLE user_account LIKE big_market_table.user_account;
DROP TABLE IF EXISTS activity_account;
CREATE TABLE activity_account LIKE big_market_table.activity_account;
DROP TABLE IF EXISTS activity_account_day;
CREATE TABLE activity_account_day LIKE big_market_table.activity_account_day;
DROP TABLE IF EXISTS activity_account_month;
CREATE TABLE activity_account_month LIKE big_market_table.activity_account_month;
-- 获奖
DROP TABLE IF EXISTS user_award_000;
CREATE TABLE IF NOT EXISTS user_award_000 LIKE big_market_table.user_award;
DROP TABLE IF EXISTS user_award_001;
CREATE TABLE IF NOT EXISTS user_award_001 LIKE big_market_table.user_award;
DROP TABLE IF EXISTS user_award_002;
CREATE TABLE IF NOT EXISTS user_award_002 LIKE big_market_table.user_award;
DROP TABLE IF EXISTS user_award_003;
CREATE TABLE IF NOT EXISTS user_award_003 LIKE big_market_table.user_award;
-- 中奖
DROP TABLE IF EXISTS activity_award_000;
CREATE TABLE IF NOT EXISTS activity_award_000 LIKE big_market_table.activity_award;
DROP TABLE IF EXISTS activity_award_001;
CREATE TABLE IF NOT EXISTS activity_award_001 LIKE big_market_table.activity_award;
DROP TABLE IF EXISTS activity_award_002;
CREATE TABLE IF NOT EXISTS activity_award_002 LIKE big_market_table.activity_award;
DROP TABLE IF EXISTS activity_award_003;
CREATE TABLE IF NOT EXISTS activity_award_003 LIKE big_market_table.activity_award;
-- 充值
DROP TABLE IF EXISTS recharge_order_000;
CREATE TABLE IF NOT EXISTS recharge_order_000 LIKE big_market_table.recharge_order;
DROP TABLE IF EXISTS recharge_order_001;
CREATE TABLE IF NOT EXISTS recharge_order_001 LIKE big_market_table.recharge_order;
DROP TABLE IF EXISTS recharge_order_002;
CREATE TABLE IF NOT EXISTS recharge_order_002 LIKE big_market_table.recharge_order;
DROP TABLE IF EXISTS recharge_order_003;
CREATE TABLE IF NOT EXISTS recharge_order_003 LIKE big_market_table.recharge_order;
-- 抽奖
DROP TABLE IF EXISTS raffle_order_000;
CREATE TABLE IF NOT EXISTS raffle_order_000 LIKE big_market_table.raffle_order;
DROP TABLE IF EXISTS raffle_order_001;
CREATE TABLE IF NOT EXISTS raffle_order_001 LIKE big_market_table.raffle_order;
DROP TABLE IF EXISTS raffle_order_002;
CREATE TABLE IF NOT EXISTS raffle_order_002 LIKE big_market_table.raffle_order;
DROP TABLE IF EXISTS raffle_order_003;
CREATE TABLE IF NOT EXISTS raffle_order_003 LIKE big_market_table.raffle_order;
-- 行为
DROP TABLE IF EXISTS reward_order_000;
CREATE TABLE IF NOT EXISTS reward_order_000 LIKE big_market_table.reward_order;
DROP TABLE IF EXISTS reward_order_001;
CREATE TABLE IF NOT EXISTS reward_order_001 LIKE big_market_table.reward_order;
DROP TABLE IF EXISTS reward_order_002;
CREATE TABLE IF NOT EXISTS reward_order_002 LIKE big_market_table.reward_order;
DROP TABLE IF EXISTS reward_order_003;
CREATE TABLE IF NOT EXISTS reward_order_003 LIKE big_market_table.reward_order;

/* =============
分库 big_market_02
============= */
DROP DATABASE IF EXISTS big_market_02;
CREATE DATABASE IF NOT EXISTS big_market_02 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE big_market_02;
-- 任务
DROP TABLE IF EXISTS task;
CREATE TABLE task LIKE big_market_table.task;
-- 账户
DROP TABLE IF EXISTS user_account;
CREATE TABLE user_account LIKE big_market_table.user_account;
DROP TABLE IF EXISTS activity_account;
CREATE TABLE activity_account LIKE big_market_table.activity_account;
DROP TABLE IF EXISTS activity_account_day;
CREATE TABLE activity_account_day LIKE big_market_table.activity_account_day;
DROP TABLE IF EXISTS activity_account_month;
CREATE TABLE activity_account_month LIKE big_market_table.activity_account_month;
-- 获奖
DROP TABLE IF EXISTS user_award_000;
CREATE TABLE IF NOT EXISTS user_award_000 LIKE big_market_table.user_award;
DROP TABLE IF EXISTS user_award_001;
CREATE TABLE IF NOT EXISTS user_award_001 LIKE big_market_table.user_award;
DROP TABLE IF EXISTS user_award_002;
CREATE TABLE IF NOT EXISTS user_award_002 LIKE big_market_table.user_award;
DROP TABLE IF EXISTS user_award_003;
CREATE TABLE IF NOT EXISTS user_award_003 LIKE big_market_table.user_award;
-- 中奖
DROP TABLE IF EXISTS activity_award_000;
CREATE TABLE IF NOT EXISTS activity_award_000 LIKE big_market_table.activity_award;
DROP TABLE IF EXISTS activity_award_001;
CREATE TABLE IF NOT EXISTS activity_award_001 LIKE big_market_table.activity_award;
DROP TABLE IF EXISTS activity_award_002;
CREATE TABLE IF NOT EXISTS activity_award_002 LIKE big_market_table.activity_award;
DROP TABLE IF EXISTS activity_award_003;
CREATE TABLE IF NOT EXISTS activity_award_003 LIKE big_market_table.activity_award;
-- 充值
DROP TABLE IF EXISTS recharge_order_000;
CREATE TABLE IF NOT EXISTS recharge_order_000 LIKE big_market_table.recharge_order;
DROP TABLE IF EXISTS recharge_order_001;
CREATE TABLE IF NOT EXISTS recharge_order_001 LIKE big_market_table.recharge_order;
DROP TABLE IF EXISTS recharge_order_002;
CREATE TABLE IF NOT EXISTS recharge_order_002 LIKE big_market_table.recharge_order;
DROP TABLE IF EXISTS recharge_order_003;
CREATE TABLE IF NOT EXISTS recharge_order_003 LIKE big_market_table.recharge_order;
-- 抽奖
DROP TABLE IF EXISTS raffle_order_000;
CREATE TABLE IF NOT EXISTS raffle_order_000 LIKE big_market_table.raffle_order;
DROP TABLE IF EXISTS raffle_order_001;
CREATE TABLE IF NOT EXISTS raffle_order_001 LIKE big_market_table.raffle_order;
DROP TABLE IF EXISTS raffle_order_002;
CREATE TABLE IF NOT EXISTS raffle_order_002 LIKE big_market_table.raffle_order;
DROP TABLE IF EXISTS raffle_order_003;
CREATE TABLE IF NOT EXISTS raffle_order_003 LIKE big_market_table.raffle_order;
-- 行为
DROP TABLE IF EXISTS reward_order_000;
CREATE TABLE IF NOT EXISTS reward_order_000 LIKE big_market_table.reward_order;
DROP TABLE IF EXISTS reward_order_001;
CREATE TABLE IF NOT EXISTS reward_order_001 LIKE big_market_table.reward_order;
DROP TABLE IF EXISTS reward_order_002;
CREATE TABLE IF NOT EXISTS reward_order_002 LIKE big_market_table.reward_order;
DROP TABLE IF EXISTS reward_order_003;
CREATE TABLE IF NOT EXISTS reward_order_003 LIKE big_market_table.reward_order;
/* =============
主库 big_market
============= */
DROP DATABASE IF EXISTS big_market;
CREATE DATABASE IF NOT EXISTS big_market DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE big_market;

DROP TABLE IF EXISTS strategy;
CREATE TABLE strategy LIKE big_market_table.strategy;
INSERT INTO strategy (strategy_id, strategy_desc, rule_models)
VALUES (1001, '【测试策略1001】黑名单+权重', 'rule_blacklist,rule_weight');

DROP TABLE IF EXISTS award;
CREATE TABLE award LIKE big_market_table.award;
INSERT INTO award (award_id, award_name, award_config, award_desc)
VALUES (2000, '【测试奖品2000】随机积分', '1,100', '负责兜底的随机积分'),
       (2001, '【测试奖品2001】随机积分', '1,50', '可以抽到的随机积分'),
       (2002, '【测试奖品2002】5元优惠券', NULL, '普通奖品'),
       (2003, '【测试奖品2003】IPhone17', NULL, '稀有奖品'),
       (2004, '【测试奖品2004】1积分', NULL, '黑名单奖品');

DROP TABLE IF EXISTS strategy_rule;
CREATE TABLE strategy_rule LIKE big_market_table.strategy_rule;
INSERT INTO strategy_rule (strategy_id, rule_model, rule_value, rule_desc)
VALUES (1001, 'rule_blacklist', '2004:dasi', '黑名单：dasi'),
       (1001, 'rule_weight', '4000:2001,2002 5000:2002,2003', '权重：4000 和 5000');

DROP TABLE IF EXISTS strategy_award;
CREATE TABLE strategy_award LIKE big_market_table.strategy_award;
INSERT INTO strategy_award (strategy_id, award_id, tree_id, award_title, award_allocate, award_surplus, award_rate,
                            award_index)
VALUES (1001, 2001, 'tree_stock', '【测试策略1001测试奖品2001】普通奖品，走tree_stock', 15, 15, 0.1500, 1),
       (1001, 2002, 'tree_stock', '【测试策略1001测试奖品2002】普通奖品，走tree_stock', 80, 80, 0.8000, 2),
       (1001, 2003, 'tree_lock', '【测试策略1001测试奖品2003】稀有奖品，走tree_stock', 5, 5, 0.0500, 3);

DROP TABLE IF EXISTS rule_tree;
CREATE TABLE rule_tree LIKE big_market_table.rule_tree;
INSERT INTO rule_tree (tree_id, tree_name, tree_desc, tree_root)
VALUES ('tree_lock', '测试规则树tree_lock', '先走 lock 再走 stock', 'rule_lock'),
       ('tree_stock', '测试规则树tree_stock', '直接走 stock', 'rule_stock');

DROP TABLE IF EXISTS rule_node;
CREATE TABLE rule_node LIKE big_market_table.rule_node;
INSERT INTO rule_node (tree_id, rule_model, rule_desc, rule_value)
VALUES ('tree_lock', 'rule_lock', '【rule_lock】达到3次才解锁', '3'),
       ('tree_lock', 'rule_stock', '【rule_stock】库存扣减1', NULL),
       ('tree_lock', 'rule_luck', '【rule_luck】幸运奖品2000', '2000'),
       ('tree_stock', 'rule_stock', '【rule_stock】库存扣减1', NULL),
       ('tree_stock', 'rule_luck', '【rule_luck】幸运奖品2000', '2000');

DROP TABLE IF EXISTS rule_edge;
CREATE TABLE rule_edge LIKE big_market_table.rule_edge;
INSERT INTO rule_edge (tree_id, rule_node_from, rule_node_to, rule_check_type, rule_check_result)
VALUES ('tree_lock', 'rule_lock', 'rule_stock', 'EQUAL', 'PERMIT'),
       ('tree_lock', 'rule_lock', 'rule_luck', 'EQUAL', 'CAPTURE'),
       ('tree_lock', 'rule_stock', 'rule_luck', 'EQUAL', 'CAPTURE'),
       ('tree_stock', 'rule_stock', 'rule_luck', 'EQUAL', 'CAPTURE');

DROP TABLE IF EXISTS activity;
CREATE TABLE activity LIKE big_market_table.activity;
INSERT INTO activity (activity_id, strategy_id, activity_name, activity_desc, activity_state, activity_begin_time, activity_end_time)
VALUES (10001, 1001, '测试活动', '测试活动描述', 'UNDERWAY', '2025-11-25 00:00:00', '2025-12-25 00:00:00');

DROP TABLE IF EXISTS recharge_quota;
CREATE TABLE recharge_quota LIKE big_market_table.recharge_quota;
INSERT INTO recharge_quota (quota_id, total_count, month_count, day_count)
VALUES (20001, 2, 1, 1),
       (20002, 3, 3, 3);

DROP TABLE IF EXISTS recharge_sku;
CREATE TABLE recharge_sku LIKE big_market_table.recharge_sku;
INSERT INTO recharge_sku (sku_id, activity_id, quota_id, stock_allocate, stock_surplus)
VALUES (30001, 10001, 20001, 100, 100),
       (30002, 10001, 20002, 100, 100);

DROP TABLE IF EXISTS behavior;
CREATE TABLE behavior LIKE big_market_table.behavior;
INSERT INTO behavior
(behavior_id, activity_id, behavior_type, behavior_state, reward_type, reward_value, reward_desc)
VALUES (100001, 10001, 'SIGN', 'AVAILABLE', 'SKU', '30001', '签到：获得抽奖次数（2，1，1）'),
       (100002, 10001, 'SIGN', 'AVAILABLE', 'POINT', '2', '签到：获得 2 积分'),
       (100003, 10001, 'LIKE', 'AVAILABLE', 'POINT', '1', '点赞：获得 1 积分'),
       (100004, 10001, 'COMMENT', 'AVAILABLE', 'POINT', '5', '评论：获得 5 积分'),
       (100005, 10001, 'SHARE', 'AVAILABLE', 'SKU', '30002', '转发：获得抽奖次数（3，3，3）'),
       (100006, 10001, 'SHARE', 'AVAILABLE', 'POINT', '5', '转发：获得 5 积分');

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
DROP TABLE IF EXISTS activity_account;
CREATE TABLE activity_account LIKE big_market_table.activity_account;
DROP TABLE IF EXISTS activity_account_day;
CREATE TABLE activity_account_day LIKE big_market_table.activity_account_day;
DROP TABLE IF EXISTS activity_account_month;
CREATE TABLE activity_account_month LIKE big_market_table.activity_account_month;
-- 中奖
DROP TABLE IF EXISTS raffle_award_000;
CREATE TABLE IF NOT EXISTS raffle_award_000 LIKE big_market_table.raffle_award;
DROP TABLE IF EXISTS raffle_award_001;
CREATE TABLE IF NOT EXISTS raffle_award_001 LIKE big_market_table.raffle_award;
DROP TABLE IF EXISTS raffle_award_002;
CREATE TABLE IF NOT EXISTS raffle_award_002 LIKE big_market_table.raffle_award;
DROP TABLE IF EXISTS raffle_award_003;
CREATE TABLE IF NOT EXISTS raffle_award_003 LIKE big_market_table.raffle_award;
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
DROP TABLE IF EXISTS activity_account;
CREATE TABLE activity_account LIKE big_market_table.activity_account;
DROP TABLE IF EXISTS activity_account_day;
CREATE TABLE activity_account_day LIKE big_market_table.activity_account_day;
DROP TABLE IF EXISTS activity_account_month;
CREATE TABLE activity_account_month LIKE big_market_table.activity_account_month;
-- 中奖
DROP TABLE IF EXISTS raffle_award_000;
CREATE TABLE IF NOT EXISTS raffle_award_000 LIKE big_market_table.raffle_award;
DROP TABLE IF EXISTS raffle_award_001;
CREATE TABLE IF NOT EXISTS raffle_award_001 LIKE big_market_table.raffle_award;
DROP TABLE IF EXISTS raffle_award_002;
CREATE TABLE IF NOT EXISTS raffle_award_002 LIKE big_market_table.raffle_award;
DROP TABLE IF EXISTS raffle_award_003;
CREATE TABLE IF NOT EXISTS raffle_award_003 LIKE big_market_table.raffle_award;
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
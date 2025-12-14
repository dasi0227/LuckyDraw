/* =============
主库 big_market
============= */
DROP DATABASE IF EXISTS big_market;
CREATE DATABASE IF NOT EXISTS big_market DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE big_market;

DROP TABLE IF EXISTS strategy;
CREATE TABLE strategy LIKE big_market_table.strategy;
INSERT INTO strategy (strategy_id, strategy_desc, rule_models)
VALUES (1001, '【测试策略1001】黑名单+幸运值', 'RULE_BLACKLIST,RULE_LUCK');

DROP TABLE IF EXISTS award;
CREATE TABLE award LIKE big_market_table.award;
INSERT INTO award (award_id, award_type, award_name, award_value, award_desc)
VALUES (2001, 'RANDOM_ACCOUNT_POINT', '随机积分 110', '1,10', '黑名单奖品，随机积分'),
       (2002, 'RANDOM_ACCOUNT_POINT', '随机积分 1-50', '1,50', '兜底奖品，固定积分'),
       (2003, 'FIXED_ACCOUNT_POINT', '固定积分 66', '66', '兜底奖品，固定积分'),
       (2004, 'FIXED_ACCOUNT_POINT', '固定积分 88', '88', '兜底奖品，固定积分'),

       (2011, 'RANDOM_ACCOUNT_POINT', '随机积分 10-50', '10,50', '积分奖品。随机'),
       (2012, 'RANDOM_ACCOUNT_POINT', '随机积分 50-100', '50,100', '积分奖品，随机'),
       (2013, 'FIXED_ACCOUNT_POINT', '固定积分 30', '30', '积分奖品，固定'),
       (2014, 'FIXED_ACCOUNT_POINT', '固定积分 80', '80', '积分奖品，固定'),

       (2015, 'DISCOUNT_COUPON', '5 元代金券', '259200', '普通奖品，3 天限时'),
       (2016, 'DISCOUNT_COUPON', '9.9 元瑞辛咖啡券', '432000', '普通奖品，5 天限时'),
       (2017, 'PHYSICAL_PRIZE', 'IPhone17', '172800', '稀有奖品，2 天限时'),
       (2018, 'PHYSICAL_PRIZE', '小米 SU7', '86400', '稀有奖品，1 天限时');

DROP TABLE IF EXISTS strategy_rule;
CREATE TABLE strategy_rule LIKE big_market_table.strategy_rule;
INSERT INTO strategy_rule (strategy_id, rule_model, rule_value, rule_desc)
VALUES (1001, 'RULE_BLACKLIST', '2001:dasi', '黑名单：dasi'),
       (1001, 'RULE_LUCK', '0:2011,2012,2013,2014,2015 100:2011,2012,2013,2014,2015,2016 200:2011,2012,2013,2014,2015,2016,2017 300:2011,2012,2013,2014,2015,2016,2017,2018', '幸运区间：0,100,200,300');

DROP TABLE IF EXISTS strategy_award;
CREATE TABLE strategy_award LIKE big_market_table.strategy_award;
INSERT INTO strategy_award (strategy_id, award_id, tree_id, award_title, award_allocate, award_surplus, award_rate,
                            award_index)
VALUES (1001, 2011, 'TREE_STOCK', '【测试策略1001测试奖品2011】走TREE_STOCK', 100, 0, 0.3500, 1),
       (1001, 2012, 'TREE_STOCK', '【测试策略1001测试奖品2012】走TREE_STOCK', 80, 80, 0.2500, 5),
       (1001, 2013, 'TREE_STOCK', '【测试策略1001测试奖品2013】走TREE_STOCK', 60, 60, 0.1500, 2),
       (1001, 2014, 'TREE_STOCK', '【测试策略1001测试奖品2014】走TREE_STOCK', 40, 40, 0.1000, 7),
       (1001, 2015, 'TREE_LOCK_1', '【测试策略1001测试奖品2015】走TREE_LOCK_1', 20, 20, 0.0700, 3),
       (1001, 2016, 'TREE_LOCK_1', '【测试策略1001测试奖品2016】走TREE_LOCK_1', 10, 10, 0.0500, 6),
       (1001, 2017, 'TREE_LOCK_2', '【测试策略1001测试奖品2017】走TREE_LOCK_2', 3, 3, 0.0200, 4),
       (1001, 2018, 'TREE_LOCK_2', '【测试策略1001测试奖品2018】走TREE_LOCK_2', 1, 1, 0.0100, 8);

DROP TABLE IF EXISTS rule_tree;
CREATE TABLE rule_tree LIKE big_market_table.rule_tree;
INSERT INTO rule_tree (tree_id, tree_name, tree_desc, tree_root)
VALUES ('TREE_LOCK_1', '测试规则树TREE_LOCK_1', '先走 lock 再走 stock', 'RULE_LOCK'),
       ('TREE_LOCK_2', '测试规则树TREE_LOCK_2', '先走 lock 再走 stock', 'RULE_LOCK'),
       ('TREE_STOCK', '测试规则树TREE_STOCK', '直接走 stock', 'RULE_STOCK');

DROP TABLE IF EXISTS rule_node;
CREATE TABLE rule_node LIKE big_market_table.rule_node;
INSERT INTO rule_node (tree_id, rule_model, rule_desc, rule_value)
VALUES ('TREE_LOCK_1', 'RULE_LOCK', '达到10次才解锁', '10'),
       ('TREE_LOCK_1', 'RULE_STOCK', '库存扣减1', NULL),
       ('TREE_LOCK_1', 'RULE_FALLBACK', '兜底奖品2003', '2003'),
       ('TREE_LOCK_2', 'RULE_LOCK', '达到10次才解锁', '20'),
       ('TREE_LOCK_2', 'RULE_STOCK', '库存扣减1', NULL),
       ('TREE_LOCK_2', 'RULE_FALLBACK', '兜底奖品2004', '2004'),
       ('TREE_STOCK', 'RULE_STOCK', '库存扣减1', NULL),
       ('TREE_STOCK', 'RULE_FALLBACK', '兜底奖品2002', '2002');

DROP TABLE IF EXISTS rule_edge;
CREATE TABLE rule_edge LIKE big_market_table.rule_edge;
INSERT INTO rule_edge (tree_id, rule_node_from, rule_node_to, rule_check_type, rule_check_result)
VALUES ('TREE_LOCK_1', 'RULE_LOCK', 'RULE_STOCK', 'EQUAL', 'PERMIT'),
       ('TREE_LOCK_1', 'RULE_LOCK', 'RULE_FALLBACK', 'EQUAL', 'CAPTURE'),
       ('TREE_LOCK_1', 'RULE_STOCK', 'RULE_FALLBACK', 'EQUAL', 'CAPTURE'),
       ('TREE_LOCK_2', 'RULE_LOCK', 'RULE_STOCK', 'EQUAL', 'PERMIT'),
       ('TREE_LOCK_2', 'RULE_LOCK', 'RULE_FALLBACK', 'EQUAL', 'CAPTURE'),
       ('TREE_LOCK_2', 'RULE_STOCK', 'RULE_FALLBACK', 'EQUAL', 'CAPTURE'),
       ('TREE_STOCK', 'RULE_STOCK', 'RULE_FALLBACK', 'EQUAL', 'CAPTURE');

DROP TABLE IF EXISTS activity;
CREATE TABLE activity LIKE big_market_table.activity;
INSERT INTO activity (activity_id, strategy_id, activity_name, activity_desc, activity_state, activity_begin_time,
                      activity_end_time)
VALUES (10001, 1001, '测试活动', '测试活动描述', 'UNDERWAY', '2025-11-25 00:00:00', '2025-12-25 00:00:00');

DROP TABLE IF EXISTS activity_sku;
CREATE TABLE activity_sku LIKE big_market_table.activity_sku;
INSERT INTO activity_sku (sku_id, activity_id, count, stock_allocate, stock_surplus)
VALUES (20001, 10001, 3, 100, 100),
       (20002, 10001, 50, 100, 100),
       (20003, 10001, 10, 100, 100),
       (20004, 10001, 1, 100, 100),
       (20005, 10001, 5, 100, 100);

DROP TABLE IF EXISTS trade;
CREATE TABLE trade LIKE big_market_table.trade;
INSERT INTO trade (trade_id, activity_id, trade_type, trade_point, trade_value, trade_name, trade_desc)
VALUES (30001, 10001, 'CONVERT_RAFFLE', '100', '1', '1 次抽奖机会', '1 次抽奖'),
       (30002, 10001, 'CONVERT_RAFFLE', '150', '2', '2 次抽奖机会', '2 次抽奖'),
       (30003, 10001, 'CONVERT_RAFFLE', '250', '4', '4 次抽奖机会', '4 次抽奖'),
       (30004, 10001, 'CONVERT_AWARD', '1000', '2013', '5 元优惠券', '5 元优惠券'),
       (30006, 10001, 'CONVERT_AWARD', '10000', '2015', '海贼王手办', '海贼王手办'),
       (30005, 10001, 'CONVERT_AWARD', '100000', '2014', 'iPhone17', 'iPhone17'),
       (30007, 10001, 'POINT_REWARD', '0', '10', '10 积分', '10 积分'),
       (30008, 10001, 'POINT_REWARD', '0', '20', '20 积分', '20 积分'),
       (30009, 10001, 'POINT_REWARD', '0', '50', '50 积分', '50 积分'),
       (30010, 10001, 'POINT_REWARD', '0', '100000000', '100000000 积分', '100000000 积分');

DROP TABLE IF EXISTS behavior;
CREATE TABLE behavior LIKE big_market_table.behavior;
INSERT INTO behavior (activity_id, behavior_type, behavior_name, reward_type, reward_value, reward_desc)
VALUES (10001, 'SIGN', '签到', 'SKU', '20001', '3 次抽奖'),
       (10001, 'SIGN', '签到', 'POINT', '30007', '10 积分'),
       (10001, 'SHARE', '转发', 'SKU', '20002', '50 次抽奖'),
       (10001, 'SHARE', '转发', 'POINT', '30008', '20 积分'),
       (10001, 'SURF', '看广告', 'SKU', '20003', '10 次抽奖'),
       (10001, 'SURF', '看广告', 'POINT', '30009', '50 积分'),
       (10001, 'COMMENT', '评论', 'SKU', '20004', '1 次抽奖'),
       (10001, 'LIKE', '点赞', 'POINT', '30010', '100000000 积分'),
       (10001, 'COLLECT', '收藏', 'SKU', '20005', '5 次抽奖');


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
-- 交易
DROP TABLE IF EXISTS trade_order_000;
CREATE TABLE IF NOT EXISTS trade_order_000 LIKE big_market_table.trade_order;
DROP TABLE IF EXISTS trade_order_001;
CREATE TABLE IF NOT EXISTS trade_order_001 LIKE big_market_table.trade_order;
DROP TABLE IF EXISTS trade_order_002;
CREATE TABLE IF NOT EXISTS trade_order_002 LIKE big_market_table.trade_order;
DROP TABLE IF EXISTS trade_order_003;
CREATE TABLE IF NOT EXISTS trade_order_003 LIKE big_market_table.trade_order;

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
-- 交易
DROP TABLE IF EXISTS trade_order_000;
CREATE TABLE IF NOT EXISTS trade_order_000 LIKE big_market_table.trade_order;
DROP TABLE IF EXISTS trade_order_001;
CREATE TABLE IF NOT EXISTS trade_order_001 LIKE big_market_table.trade_order;
DROP TABLE IF EXISTS trade_order_002;
CREATE TABLE IF NOT EXISTS trade_order_002 LIKE big_market_table.trade_order;
DROP TABLE IF EXISTS trade_order_003;
CREATE TABLE IF NOT EXISTS trade_order_003 LIKE big_market_table.trade_order;
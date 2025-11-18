

DROP DATABASE IF EXISTS `big_market`;

CREATE DATABASE IF NOT EXISTS `big_market`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE `big_market`;

CREATE TABLE `strategy` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `strategy_id` bigint(8) NOT NULL COMMENT '抽奖策略ID',
  `strategy_desc` varchar(128) NOT NULL COMMENT '抽奖策略描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_strategy_id` (`strategy_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖策略表：定义抽奖活动的策略ID和策略描述';

CREATE TABLE `strategy_award` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `strategy_id` bigint(8) NOT NULL COMMENT '抽奖策略ID',
  `award_id` int(8) NOT NULL COMMENT '抽奖奖品ID',
  `award_title` varchar(128) NOT NULL COMMENT '抽奖奖品标题',
  `award_subtitle` varchar(128) DEFAULT NULL COMMENT '抽奖奖品副标题',
  `award_count` int(8) NOT NULL DEFAULT '0' COMMENT '奖品库存总量',
  `award_count_surplus` int(8) NOT NULL DEFAULT '0' COMMENT '奖品库存余量',
  `award_rate` decimal(6,4) NOT NULL COMMENT '奖品中奖概率',
  `rule_models` varchar(256) DEFAULT NULL COMMENT '规则模型',
  `sort` int(2) NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_strategy_id_award_id` (`strategy_id`,`award_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖策略奖品池表：定义某个策略下可抽取的奖品、概率、库存及规则配置';

CREATE TABLE `strategy_rule` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `strategy_id` int(8) NOT NULL COMMENT '抽奖策略ID',
  `award_id` int(8) DEFAULT NULL COMMENT '抽奖奖品ID【规则类型为策略，则不需要奖品ID】',
  `rule_type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '规则类型；1-策略规则、2-奖品规则',
  `rule_model` varchar(16) NOT NULL COMMENT '规则模型；如随机、锁定、兜底等',
  `rule_value` varchar(64) NOT NULL COMMENT '规则比值（区间/次数/配置值）',
  `rule_desc` varchar(128) NOT NULL COMMENT '规则描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_strategy_id_award_id` (`strategy_id`,`award_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖策略规则表：定义抽奖策略及奖品规则逻辑';

CREATE TABLE `award` (
    `id`              bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `award_id`        int(8)           NOT NULL COMMENT '抽奖奖品ID（策略内流转使用）',
    `award_key`       varchar(32)      NOT NULL COMMENT '奖品对接标识（对应发奖策略）',
    `award_config`    varchar(32)      NOT NULL COMMENT '奖品配置值（数量/模型/积分范围等）',
    `award_desc`      varchar(128)     NOT NULL COMMENT '奖品内容描述',
    `create_time`     datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
KEY `idx_award_id` (`award_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='奖品定义表：配置具体奖品的发放方式、配置信息和描述';

INSERT INTO `strategy`
(`id`, `strategy_id`, `strategy_desc`, `create_time`, `update_time`)
VALUES
(1,100001,'抽奖策略','2023-12-09 09:37:19','2023-12-09 09:37:19');


INSERT INTO `strategy_award` (`id`, `strategy_id`, `award_id`, `award_title`, `award_subtitle`, `award_count`, `award_count_surplus`, `award_rate`, `rule_models`, `sort`, `create_time`, `update_time`)
VALUES
	(1,100001,101,'随机积分',NULL,80000,80000,80.0000,'rule_random,rule_luck_award',1,'2023-12-09 09:38:31','2023-12-09 10:58:06'),
	(2,100001,102,'5次使用',NULL,10000,10000,10.0000,'rule_luck_award',2,'2023-12-09 09:39:18','2023-12-09 10:34:23'),
	(3,100001,103,'10次使用',NULL,5000,5000,5.0000,'rule_luck_award',3,'2023-12-09 09:42:36','2023-12-09 10:34:24'),
	(4,100001,104,'20次使用',NULL,4000,4000,4.0000,'rule_luck_award',4,'2023-12-09 09:43:15','2023-12-09 10:34:25'),
	(5,100001,105,'增加gpt-4对话模型',NULL,600,600,0.6000,'rule_luck_award',5,'2023-12-09 09:43:47','2023-12-09 10:34:26'),
	(6,100001,106,'增加dall-e-2画图模型',NULL,200,200,0.2000,'rule_luck_award',6,'2023-12-09 09:44:20','2023-12-09 10:34:26'),
	(7,100001,107,'增加dall-e-3画图模型','抽奖1次后解锁',200,200,0.2000,'rule_lock,rule_luck_award',7,'2023-12-09 09:45:38','2023-12-09 10:30:59'),
	(8,100001,108,'增加100次使用','抽奖2次后解锁',199,199,0.1999,'rule_lock,rule_luck_award',8,'2023-12-09 09:46:02','2023-12-09 12:20:52'),
	(9,100001,109,'解锁全部模型','抽奖6次后解锁',1,1,0.0001,'rule_lock,rule_luck_award',9,'2023-12-09 09:46:39','2023-12-09 12:20:50');


INSERT INTO `strategy_rule` (`id`, `strategy_id`, `award_id`, `rule_type`, `rule_model`, `rule_value`, `rule_desc`, `create_time`, `update_time`)
VALUES
	(1,100001,101,2,'rule_random','1,1000','随机积分策略','2023-12-09 10:05:30','2023-12-09 12:55:52'),
	(2,100001,107,2,'rule_lock','1','抽奖1次后解锁','2023-12-09 10:16:41','2023-12-09 12:55:53'),
	(3,100001,108,2,'rule_lock','2','抽奖2次后解锁','2023-12-09 10:17:43','2023-12-09 12:55:54'),
	(4,100001,109,2,'rule_lock','6','抽奖6次后解锁','2023-12-09 10:17:43','2023-12-09 12:55:54'),
	(5,100001,107,2,'rule_luck_award','1,100','兜底奖品100以内随机积分','2023-12-09 10:30:12','2023-12-09 12:55:55'),
	(6,100001,108,2,'rule_luck_award','1,100','兜底奖品100以内随机积分','2023-12-09 10:30:43','2023-12-09 12:55:56'),
	(7,100001,101,2,'rule_luck_award','1,10','兜底奖品10以内随机积分','2023-12-09 10:30:43','2023-12-09 12:55:57'),
	(8,100001,102,2,'rule_luck_award','1,20','兜底奖品20以内随机积分','2023-12-09 10:30:43','2023-12-09 12:55:57'),
	(9,100001,103,2,'rule_luck_award','1,30','兜底奖品30以内随机积分','2023-12-09 10:30:43','2023-12-09 12:55:58'),
	(10,100001,104,2,'rule_luck_award','1,40','兜底奖品40以内随机积分','2023-12-09 10:30:43','2023-12-09 12:55:59'),
	(11,100001,105,2,'rule_luck_award','1,50','兜底奖品50以内随机积分','2023-12-09 10:30:43','2023-12-09 12:56:00'),
	(12,100001,106,2,'rule_luck_award','1,60','兜底奖品60以内随机积分','2023-12-09 10:30:43','2023-12-09 12:56:00'),
	(13,100001,NULL,1,'rule_weight','6000,102,103,104,105,106,107,108,109','消耗6000分，必中奖范围','2023-12-09 10:30:43','2023-12-09 12:58:21'),
	(14,100001,NULL,1,'rule_blacklist','1','黑名单抽奖，积分兜底','2023-12-09 12:59:45','2023-12-09 13:42:23');


INSERT INTO `award` (`id`, `award_id`, `award_key`, `award_config`, `award_desc`, `create_time`, `update_time`)
VALUES
    (1, 101, 'user_credit_random', '1100', '用户积分【优先透彻规则范围，如果没有则走配置】', '2023-12-09 11:07:06', '2023-12-09 11:21:31'),
    (2, 102, 'openai_use_count', '5', 'OpenAI增加使用次数', '2023-12-09 11:07:06', '2023-12-09 11:12:59'),
    (3, 103, 'openai_use_conut', '10', 'OpenAI增加使用次数', '2023-12-09 11:07:06', '2023-12-09 11:12:59'),
    (4, 104, 'openai_use_conut', '20', 'OpenAI增加使用次数', '2023-12-09 11:07:06', '2023-12-09 11:12:58'),
    (5, 105, 'openai_model', 'gpt-4', 'OpenAI增加模型', '2023-12-09 11:07:06', '2023-12-09 11:12:01'),
    (6, 106, 'openai_model', 'dall-e-2', 'OpenAI增加模型', '2023-12-09 11:07:06', '2023-12-09 11:12:08'),
    (7, 107, 'openai_model', 'dall-e-3', 'OpenAI增加模型', '2023-12-09 11:07:06', '2023-12-09 11:12:10'),
    (8, 108, 'openai_use_conut', '100', 'OpenAI增加使用次数', '2023-12-09 11:07:06', '2023-12-09 11:12:55'),
    (9, 109, 'openai_model', 'gpt-4,dall-e-2,dall-e-3', 'OpenAI增加模型', '2023-12-09 11:07:06', '2023-12-09 11:12:39');
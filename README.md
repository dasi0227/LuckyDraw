# 幸运抽奖组件



## 项目概述

**LuckyDraw** 是一个面向活动运营场景的抽奖系统，基于 Spring Boot 2.7 + DDD 构建，覆盖从**活动配置、抽奖策略、积分交易、行为奖励、库存发放、中奖记录到订单落库**的完整业务闭环。

- 系统将**抽奖、兑换、充值、奖励**等流程统一抽象为可配置、可扩展的领域模型，并通过**责任链、规则树、工厂模式和消息驱动机制**，将复杂业务拆分为清晰的用例和稳定的异步链路。

- 系统内置 **DCC 配置中心**，支持对**限流、熔断、降级、锁参数以及活动相关配置**进行在线热更新，无需重启即可生效，便于运营侧在活动期间快速调整策略、控制风险和响应流量变化。

- 系统结合 **Redis 原子库存、分布式锁、MQ 异步投递、任务补偿与 Nginx 负载均衡**，在高并发场景下保障核心流程具备较好的稳定性、幂等性和最终一致性。

> 技术栈：**Java 8, SpringBoot 2.7, MySQL 8.0, MyBatis, RabbitMQ, JWT, ZooKeeper, Curator, Redisson, Nginx**



## 核心设计


### DDD 架构
接口层（`api`/`trigger`）聚焦协议适配与鉴权、DTO/VO 转换；应用层（`app`）负责用例编排与事务边界；领域层（`domain`）沉淀活动、账户、策略、奖品、订单、任务等核心模型与规则；基础设施层（`infrastructure`）承接 MySQL/Redis/MQ/Nginx 等技术实现。通过 Repository + Entity/VO/Aggregate 统一表达领域语义，避免“控制器堆业务 + DAO 直连”的贫血模型，提升可测性与迭代空间。

### 责任链模式
抽奖前置校验拆为可插拔的 Handler 链路（活动状态/时间窗口、次数额度、黑名单、幸运值档位等），按顺序执行并支持短路；链路节点可按场景组合（抽奖链、兑换链、充值链），新增校验仅需注册 Handler，降低 if-else 耦合。


### 规则树模式

抽奖结果产出后，用规则树承载“库存不足、未解锁、次数上限、兜底奖品”等决策；节点组合表达复杂策略，可配置扩展且易于单测，把结果处理从流程代码剥离为可演进的决策结构。

### 策略工厂模式

发奖采用“统一入口 + 多实现路由”：注解标识奖品类型，构造注入自动注册 Handler，积分/实物/券码等发放逻辑解耦。统一入口负责幂等与上下文聚合，具体发放由对应 Handler 处理，满足开闭原则。

### MQ 削峰填谷

主链路先落库订单与任务（Task），再将发奖事件封装为 Event 投递 MQ，消费者异步执行发奖、入账、个人奖品仓库写入，削峰并降低延迟。同时引入**任务补偿机制**，定时轮询 Task 表对未分发/失败任务重试；结合订单状态机与消费幂等，保障网络抖动或失败场景下最终一致性。

### HSET 概率装配

策略装配阶段按最小概率单位扩展并打乱奖品概率，生成 index → awardId 的 Map 存入 Redis HSET；抽奖时随机 index 直接命中，避免实时遍历累加，降低尾延迟。支持按幸运值档位拆分装配，实现分档概率分布。

### BlockingQueue 防重防超卖

库存先用 Redis 原子 decr 预扣保证并发一致性，setNx(lockKey)（带过期）保护单次扣减唯一性；然后将入库操作放入 Redisson 延迟队列再入阻塞队列消费，错峰异步回刷 DB，减少瞬时写放大；库存归零触发“库存耗尽事件”投递 MQ，驱动数据库同步与缓存清理，形成“缓存扣减 → 事件落库/修正”的闭环。

### @DCCValue 热更新

自定义 @DCCValue + 监听器，将降级、限流、熔断等开关绑定运行时字段，配置变更在线生效无需重启，用于紧急止损、灰度调参与运维快速响应。

### AOP 限流/熔断

接口层 AOP 统一织入限流与熔断：限流基于 Redis 计数做分布式 QPS/单用户阈值，熔断按时间窗口统计失败进入 Open/Half-Open 状态；与 DCC 开关联动，提供可开关、可调参、可观测的稳定性治理。



## 页面展示

### 活动数据

![a5ec8c1b5fbbf2f0b536d98de39ae51c](./assets/a5ec8c1b5fbbf2f0b536d98de39ae51c.png)

### 抽奖九宫格

![bb12d1339bc6e7fdabdbaaae7b4d4e7a](./assets/bb12d1339bc6e7fdabdbaaae7b4d4e7a.png)

### 互动/兑换成功

![a83fcb1fb3d5a05ab79f9e1a958d838a](./assets/a83fcb1fb3d5a05ab79f9e1a958d838a.png)

### 抽奖结果

![03c38055c02a1ab8dcc4aaeeb38e8752](./assets/03c38055c02a1ab8dcc4aaeeb38e8752.png)

### 积分充值

![da86077e1293f03179acf422cc239c0b](./assets/da86077e1293f03179acf422cc239c0b.png)

### 登陆注册

![7b698ddf1a135ee7fa9c17aaf035813c](./assets/7b698ddf1a135ee7fa9c17aaf035813c.png)

### 配置中心

![2a1102d2d86922a1f620a178163d7c19](./assets/2a1102d2d86922a1f620a178163d7c19.png)



## 系统设计

### 活动装配

![60c45459cfd0a6eab73db8d619d54a5d](./assets/60c45459cfd0a6eab73db8d619d54a5d.png)

### 活动抽奖

![f35b2c6beee791e175a2afd65bc83c19](./assets/f35b2c6beee791e175a2afd65bc83c19.png)

### 活动校验责任链

![b3ee6f57a6b4903f60b09acc90b9ce06](./assets/b3ee6f57a6b4903f60b09acc90b9ce06.png)

### 抽奖规则树

![3f84213aa94534a5d905a24fecbff6dc](./assets/3f84213aa94534a5d905a24fecbff6dc.png)

### DCC 动态配置中心

![7f09954e23b2aa29b7a6b9ee7beb83ba](./assets/7f09954e23b2aa29b7a6b9ee7beb83ba.png)

### 熔断和限流 AOP

![759b71437cea587d9198496d6f7a52ed](./assets/759b71437cea587d9198496d6f7a52ed.png)

### 用户互动

![40fe81bc3c6ab4a0ff247bbc9bedb269](./assets/40fe81bc3c6ab4a0ff247bbc9bedb269.png)

### 用户交易

![faea391eceeac4026bf97b0415874d53](./assets/faea391eceeac4026bf97b0415874d53.png)

### 派发奖品和交易

![d4f5a784e71d879a0ee9b5a6fc76211a](./assets/d4f5a784e71d879a0ee9b5a6fc76211a.png)

![7294e6d8a5bc68a3549ee63d447b9047](./assets/7294e6d8a5bc68a3549ee63d447b9047.png)

![e9321f0ad551c7e5f04b174d2ca461ad](./assets/e9321f0ad551c7e5f04b174d2ca461ad.png)

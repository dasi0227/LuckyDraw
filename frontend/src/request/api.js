import request from './request.js';

const prefix = '/api/v1/big-market';

export default {
  // 登录
  login(data) {
    return request.post(`${prefix}/user/login`, data);
  },

  // 注册
  register(data) {
    return request.post(`${prefix}/user/register`, data);
  },

  // 查询当前活动的积分兑换信息
  queryActivityConvert(data) {
    return request.post(`${prefix}/query/convert`, data);
  },

  // 查询用户在当前活动的基本信息
  queryActivityAccount(data) {
    return request.post(`${prefix}/query/account`, data);
  },

  // 查询用户在当前活动的抽奖奖品列表
  queryActivityAward(data) {
    return request.post(`${prefix}/query/award`, data);
  },

  // 查询活动基本信息
  queryActivityInfo(data) {
    return request.post(`${prefix}/query/info`, data);
  },

  // 查询用户在当前活动的幸运值
  queryActivityLuck(data) {
    return request.post(`${prefix}/query/luck`, data);
  },

  // 查询用户在当前活动的互动任务
  queryActivityBehavior(data) {
    return request.post(`${prefix}/query/behavior`, data);
  },

  // 执行互动行为
  doBehavior(data) {
    return request.post(`${prefix}/behavior`, data);
  },

  // 增加幸运值
  addFortune(data) {
    return request.post(`${prefix}/fortune`, data);
  },

  // 执行积分兑换
  doConvert(data) {
    return request.post(`${prefix}/convert`, data);
  },

  // 执行抽奖
  doRaffle(data) {
    return request.post(`${prefix}/raffle`, data);
  },

  // 查询用户在当前活动的获奖信息
  queryUserAward(data) {
    return request.post(`${prefix}/query/user-award/raffle`, data);
  },

};

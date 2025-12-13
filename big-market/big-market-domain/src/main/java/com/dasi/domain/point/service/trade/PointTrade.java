package com.dasi.domain.point.service.trade;

import com.alibaba.fastjson.JSON;
import com.dasi.domain.common.IRedisLock;
import com.dasi.domain.common.IUniqueIdGenerator;
import com.dasi.domain.point.event.DispatchPointTradeOutcomeEvent;
import com.dasi.domain.point.event.DispatchPointTradeOutcomeEvent.DispatchTradeOutcomeMessage;
import com.dasi.domain.point.model.entity.ActivityAccountEntity;
import com.dasi.domain.point.model.entity.TaskEntity;
import com.dasi.domain.point.model.entity.TradeEntity;
import com.dasi.domain.point.model.entity.TradeOrderEntity;
import com.dasi.domain.point.model.io.TradeContext;
import com.dasi.domain.point.model.io.TradeResult;
import com.dasi.domain.point.model.type.TaskState;
import com.dasi.domain.point.model.type.TradeState;
import com.dasi.domain.point.repository.IPointRepository;
import com.dasi.types.constant.Delimiter;
import com.dasi.types.constant.ExceptionMessage;
import com.dasi.types.constant.RedisKey;
import com.dasi.types.event.BaseEvent;
import com.dasi.types.exception.AppException;
import com.dasi.types.exception.BusinessException;
import com.dasi.types.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Slf4j
@Service
public class PointTrade implements IPointTrade {

    @Resource
    private IPointRepository tradeRepository;

    @Resource
    private IUniqueIdGenerator uniqueIdGenerator;

    @Resource
    private IRedisLock redisLock;

    @Resource
    private DispatchPointTradeOutcomeEvent dispatchPointTradeOutcomeEvent;

    @Override
    public TradeResult doPointTrade(TradeContext tradeContext) {

        // 参数校验
        String userId = tradeContext.getUserId();
        Long tradeId = tradeContext.getTradeId();
        Long activityId = tradeContext.getActivityId();
        if (StringUtils.isBlank(userId)) throw new AppException("缺少参数 userId");
        if (tradeId == null) throw new AppException("缺少参数 tradeId");
        if (activityId == null) throw new AppException("缺少参数 activityId");

        String lockKey = RedisKey.LOCK_POINT_KEY + activityId + Delimiter.COLON + userId;
        boolean isLock = false;

        try {
            // 上锁
            isLock = redisLock.tryLock(lockKey);
            if (!isLock) {
                throw new BusinessException("当前系统繁忙，请稍后再试");
            }

            // 检查积分
            TradeEntity tradeEntity = tradeRepository.queryTradeByTradeId(tradeId);
            ActivityAccountEntity activityAccountEntity = tradeRepository.queryActivityAccount(userId, activityId);
            Integer accountPoint = activityAccountEntity.getAccountPoint();
            Integer tradePoint = tradeEntity.getTradePoint();
            if (accountPoint < tradePoint) {
                log.info("【交易】当前用户的积分不够：userId={}, activityId={}, accountPoint={}, tradePoint={}", userId, activityId, accountPoint, tradePoint);
                throw new BusinessException(ExceptionMessage.POINT_NOT_ENOUGH);
            } else {
                activityAccountEntity.setAccountPoint(tradePoint);
            }

            // 构建业务id
            String bizId = tradeContext.getBizId();
            String businessNo = tradeContext.getBusinessNo();
            if (StringUtils.isBlank(bizId) && StringUtils.isNotBlank(businessNo)) {
                bizId = businessNo + Delimiter.UNDERSCORE + userId + Delimiter.UNDERSCORE + tradeEntity.getTradeType() + Delimiter.UNDERSCORE + tradeEntity.getTradeValue() + Delimiter.UNDERSCORE + TimeUtil.thisMoment(true);
            }

            // 构建订单对象
            String orderId = uniqueIdGenerator.nextTradeOrderId();
            TradeOrderEntity tradeOrderEntity = TradeOrderEntity.builder()
                    .orderId(orderId)
                    .bizId(bizId)
                    .userId(userId)
                    .tradeId(tradeId)
                    .activityId(tradeEntity.getActivityId())
                    .tradeType(tradeEntity.getTradeType())
                    .tradeState(TradeState.CREATED)
                    .tradeTime(LocalDateTime.now())
                    .build();

            // 构建任务对象
            DispatchTradeOutcomeMessage dispatchTradeOutcomeMessage = DispatchTradeOutcomeMessage.builder()
                    .orderId(orderId)
                    .userId(userId)
                    .tradeId(tradeId)
                    .activityId(activityId)
                    .build();
            BaseEvent.EventMessage<DispatchTradeOutcomeMessage> eventMessage = dispatchPointTradeOutcomeEvent.buildEventMessage(dispatchTradeOutcomeMessage);
            TaskEntity taskEntity = TaskEntity.builder()
                    .userId(userId)
                    .messageId(eventMessage.getMessageId())
                    .topic(dispatchPointTradeOutcomeEvent.getTopic())
                    .message(JSON.toJSONString(eventMessage))
                    .taskState(TaskState.CREATED)
                    .build();

            tradeRepository.savePointTradeOrder(activityAccountEntity, taskEntity, tradeOrderEntity);
            return TradeResult.builder().tradeDesc(tradeEntity.getTradeDesc()).build();
        } finally {
            if (isLock) {
                redisLock.unlock(lockKey);
            }
        }
    }

}

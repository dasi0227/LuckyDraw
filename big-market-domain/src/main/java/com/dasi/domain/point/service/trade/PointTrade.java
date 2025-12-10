package com.dasi.domain.point.service.trade;

import com.alibaba.fastjson.JSON;
import com.dasi.domain.point.model.entity.TaskEntity;
import com.dasi.domain.point.model.type.TaskState;
import com.dasi.domain.common.IUniqueIdGenerator;
import com.dasi.domain.point.event.DispatchTradeOutcomeEvent;
import com.dasi.domain.point.event.DispatchTradeOutcomeEvent.DispatchTradeOutcomeMessage;
import com.dasi.domain.point.model.entity.TradeEntity;
import com.dasi.domain.point.model.entity.TradeOrderEntity;
import com.dasi.domain.point.model.io.TradeContext;
import com.dasi.domain.point.model.io.TradeResult;
import com.dasi.domain.point.model.type.TradeState;
import com.dasi.domain.point.repository.IPointRepository;
import com.dasi.types.constant.Delimiter;
import com.dasi.types.event.BaseEvent;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Slf4j
@Service
public class PointTrade implements IPointTrade {

    @Resource
    private IPointRepository pointRepository;

    @Resource
    private IUniqueIdGenerator uniqueIdGenerator;

    @Resource
    private DispatchTradeOutcomeEvent dispatchTradeOutcomeEvent;

    @Override
    public TradeResult doPointTrade(TradeContext tradeContext) {

        // 1. 参数校验
        String userId = tradeContext.getUserId();
        Long tradeId = tradeContext.getTradeId();
        String businessNo = tradeContext.getBusinessNo();
        if (StringUtils.isBlank(businessNo)) throw new AppException("缺少参数 businessNo");
        if (StringUtils.isBlank(userId)) throw new AppException("缺少参数 userId");
        if (tradeId == null) throw new AppException("缺少参数 tradeId");

        // 2. 检查积分
        TradeEntity tradeEntity = pointRepository.queryTradeByTradeId(tradeId);
        Integer userPoint = pointRepository.queryUserPointByUserId(userId);
        Integer tradePoint = Integer.valueOf(tradeEntity.getTradePoint());
        if (userPoint < tradePoint) {
            log.info("【兑换】当前用户的积分不够：userId={}, userPoint={}, tradePoint={}", userId, userPoint, tradePoint);
            throw new AppException("兑换失败：tradeId=" + tradeId);
        }

        // 3. 构建订单对象
        String bizId = businessNo + Delimiter.UNDERSCORE + userId + Delimiter.UNDERSCORE + tradeEntity.getTradeType() + Delimiter.UNDERSCORE + tradeEntity.getTradeValue() + Delimiter.UNDERSCORE + System.currentTimeMillis();
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

        // 4. 构建任务对象
        DispatchTradeOutcomeMessage dispatchTradeOutcomeMessage = DispatchTradeOutcomeMessage.builder()
                .orderId(orderId)
                .userId(userId)
                .tradeId(tradeId)
                .tradeType(tradeEntity.getTradeType())
                .build();
        BaseEvent.EventMessage<DispatchTradeOutcomeMessage> eventMessage = dispatchTradeOutcomeEvent.buildEventMessage(dispatchTradeOutcomeMessage);
        TaskEntity taskEntity = TaskEntity.builder()
                .userId(userId)
                .messageId(eventMessage.getMessageId())
                .topic(dispatchTradeOutcomeEvent.getTopic())
                .message(JSON.toJSONString(eventMessage))
                .taskState(TaskState.CREATED)
                .build();

        pointRepository.savePointConvertTradeOrder(taskEntity, tradeOrderEntity);
        return TradeResult.builder().tradeDesc(tradeEntity.getTradeDesc()).build();
    }

    @Override
    public void updateTradeOrderState(TradeOrderEntity tradeOrderEntity) {
        pointRepository.updateTradeOrderState(tradeOrderEntity);
    }

}

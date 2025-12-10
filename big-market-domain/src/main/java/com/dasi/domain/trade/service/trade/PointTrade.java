package com.dasi.domain.trade.service.trade;

import com.alibaba.fastjson.JSON;
import com.dasi.domain.trade.model.entity.TaskEntity;
import com.dasi.domain.trade.model.io.ConvertContext;
import com.dasi.domain.trade.model.type.TaskState;
import com.dasi.domain.common.IUniqueIdGenerator;
import com.dasi.domain.trade.event.DispatchTradeOutcomeEvent;
import com.dasi.domain.trade.event.DispatchTradeOutcomeEvent.DispatchTradeOutcomeMessage;
import com.dasi.domain.trade.model.entity.TradeEntity;
import com.dasi.domain.trade.model.entity.TradeOrderEntity;
import com.dasi.domain.trade.model.io.ConvertResult;
import com.dasi.domain.trade.model.type.TradeState;
import com.dasi.domain.trade.repository.ITradeRepository;
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
    private ITradeRepository tradeRepository;

    @Resource
    private IUniqueIdGenerator uniqueIdGenerator;

    @Resource
    private DispatchTradeOutcomeEvent dispatchTradeOutcomeEvent;

    @Override
    public ConvertResult doPointTrade(ConvertContext convertContext) {

        // 1. 参数校验
        String userId = convertContext.getUserId();
        Long tradeId = convertContext.getTradeId();
        String businessNo = convertContext.getBusinessNo();
        Long activityId = convertContext.getActivityId();
        if (StringUtils.isBlank(businessNo)) throw new AppException("缺少参数 businessNo");
        if (StringUtils.isBlank(userId)) throw new AppException("缺少参数 userId");
        if (tradeId == null) throw new AppException("缺少参数 tradeId");
        if (activityId == null) throw new AppException("缺少参数 activityId");

        // 2. 检查积分
        TradeEntity tradeEntity = tradeRepository.queryTradeByTradeId(tradeId);
        Integer userPoint = tradeRepository.queryActivityAccountPoint(userId, activityId);
        Integer tradePoint = tradeEntity.getTradePoint();
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
                .activityId(activityId)
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

        tradeRepository.savePointConvertTradeOrder(taskEntity, tradeOrderEntity);
        return ConvertResult.builder().tradeDesc(tradeEntity.getTradeDesc()).build();
    }

    @Override
    public void updateTradeOrderState(TradeOrderEntity tradeOrderEntity) {
        tradeRepository.updateTradeOrderState(tradeOrderEntity);
    }

}

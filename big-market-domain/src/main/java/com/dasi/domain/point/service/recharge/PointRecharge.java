package com.dasi.domain.point.service.recharge;

import com.dasi.domain.common.IUniqueIdGenerator;
import com.dasi.domain.point.model.entity.TradeEntity;
import com.dasi.domain.point.model.entity.TradeOrderEntity;
import com.dasi.domain.point.model.io.PointRechargeContext;
import com.dasi.domain.point.model.io.PointRechargeResult;
import com.dasi.domain.point.model.type.TradeState;
import com.dasi.domain.point.repository.IPointRepository;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Slf4j
@Service
public class PointRecharge implements IPointRecharge {

    @Resource
    private IPointRepository pointRepository;

    @Resource
    private IUniqueIdGenerator uniqueIdGenerator;

    @Override
    public PointRechargeResult doPointRecharge(PointRechargeContext pointRechargeContext) {

        // 1. 参数校验
        String userId = pointRechargeContext.getUserId();
        String bizId = pointRechargeContext.getBizId();
        Long tradeId = pointRechargeContext.getTradeId();
        if (StringUtils.isBlank(userId)) throw new AppException("缺少参数 userId");
        if (StringUtils.isBlank(bizId)) throw new AppException("缺少参数 bizId");
        if (tradeId == null) throw new AppException("缺少参数 tradeId");

        // 2. 查询活动的基础信息
        TradeEntity tradeEntity = pointRepository.queryTradeByTradeId(tradeId);

        // 3. 创建账户
        pointRepository.createUserAccountIfAbsent(userId);

        // 4. 构建订单
        TradeOrderEntity tradeOrderEntity = TradeOrderEntity.builder()
                .orderId(uniqueIdGenerator.nextTradeOrderId())
                .bizId(bizId)
                .userId(userId)
                .tradeId(tradeEntity.getTradeId())
                .activityId(tradeEntity.getActivityId())
                .tradeType(tradeEntity.getTradeType())
                .tradeState(TradeState.CREATED)
                .tradeTime(LocalDateTime.now())
                .build();
        pointRepository.savePointRechargeTradeOrder(tradeEntity, tradeOrderEntity);

        return PointRechargeResult.builder().point(Integer.valueOf(tradeEntity.getTradeValue())).build();
    }

}

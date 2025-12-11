package com.dasi.domain.trade.service.convert;

import com.dasi.domain.trade.model.entity.TradeEntity;
import com.dasi.domain.trade.model.io.ConvertContext;
import com.dasi.domain.trade.repository.ITradeRepository;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class PointConvert implements IPointConvert {

    @Resource
    private ITradeRepository tradeRepository;


    @Override
    public void doConvertAward(ConvertContext convertContext) {

        String userId = convertContext.getUserId();
        String orderId = convertContext.getOrderId();
        Long tradeId = convertContext.getTradeId();
        if (StringUtils.isBlank(userId)) throw new AppException("缺少参数 userId");
        if (tradeId == null) throw new AppException("缺少参数 tradeId");
        if (orderId == null) throw new AppException("缺少参数 orderId");

        TradeEntity tradeEntity = tradeRepository.queryTradeByTradeId(tradeId);
        Long activityId = tradeEntity.getActivityId();
        Integer accountPoint = tradeRepository.queryActivityAccountPoint(userId, activityId);
        Integer tradePoint = tradeEntity.getTradePoint();
        if (accountPoint < tradePoint) {
            log.info("【兑换】当前用户的积分不够：userId={}, accountPoint={}, tradePoint={}", userId, accountPoint, tradePoint);
            throw new AppException("兑换失败：tradeId=" + tradeId);
        }

        tradeRepository.doConvertAward(userId, orderId, tradeEntity);
    }

    @Override
    public void doConvertRaffle(ConvertContext convertContext) {

        String userId = convertContext.getUserId();
        String orderId = convertContext.getOrderId();
        Long tradeId = convertContext.getTradeId();
        if (StringUtils.isBlank(userId)) throw new AppException("缺少参数 userId");
        if (tradeId == null) throw new AppException("缺少参数 tradeId");
        if (orderId == null) throw new AppException("缺少参数 orderId");

        TradeEntity tradeEntity = tradeRepository.queryTradeByTradeId(tradeId);
        Long activityId = tradeEntity.getActivityId();
        Integer accountPoint = tradeRepository.queryActivityAccountPoint(userId, activityId);
        Integer tradePoint = tradeEntity.getTradePoint();
        if (accountPoint < tradePoint) {
            log.info("【兑换】当前用户的积分不够：userId={}, accountPoint={}, tradePoint={}", userId, accountPoint, tradePoint);
            throw new AppException("兑换失败：tradeId=" + tradeId);
        }

        tradeRepository.doConvertRaffle(userId, tradeEntity);
    }

}

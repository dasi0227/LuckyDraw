package com.dasi.domain.point.service.convert;

import com.dasi.domain.point.model.entity.TradeEntity;
import com.dasi.domain.point.model.io.ConvertContext;
import com.dasi.domain.point.repository.IPointRepository;
import com.dasi.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class PointConvert implements IPointConvert {

    @Resource
    private IPointRepository pointRepository;


    @Override
    public void doConvertAward(ConvertContext convertContext) {
        String userId = convertContext.getUserId();
        String orderId = convertContext.getOrderId();
        Long tradeId = convertContext.getTradeId();
        if (StringUtils.isBlank(userId)) throw new AppException("缺少参数 userId");
        if (tradeId == null) throw new AppException("缺少参数 tradeId");

        TradeEntity tradeEntity = pointRepository.queryTradeByTradeId(tradeId);
        Integer userPoint = pointRepository.queryUserPointByUserId(userId);
        Integer tradePoint = Integer.valueOf(tradeEntity.getTradePoint());
        if (userPoint < tradePoint) {
            log.info("【兑换】当前用户的积分不够：userId={}, userPoint={}, tradePoint={}", userId, userPoint, tradePoint);
            throw new AppException("兑换失败：tradeId=" + tradeId);
        }

        pointRepository.doConvertAward(userId, orderId, tradeEntity);
    }

    @Override
    public void doConvertRaffle(ConvertContext convertContext) {
        String userId = convertContext.getUserId();
        Long tradeId = convertContext.getTradeId();
        if (StringUtils.isBlank(userId)) throw new AppException("缺少参数 userId");
        if (tradeId == null) throw new AppException("缺少参数 tradeId");

        TradeEntity tradeEntity = pointRepository.queryTradeByTradeId(tradeId);
        Integer userPoint = pointRepository.queryUserPointByUserId(userId);
        Integer tradePoint = Integer.valueOf(tradeEntity.getTradePoint());
        if (userPoint < tradePoint) {
            log.info("【兑换】当前用户的积分不够：userId={}, userPoint={}, tradePoint={}", userId, userPoint, tradePoint);
            throw new AppException("兑换失败：tradeId=" + tradeId);
        }

        pointRepository.doConvertRaffle(userId, tradeEntity);
    }

}

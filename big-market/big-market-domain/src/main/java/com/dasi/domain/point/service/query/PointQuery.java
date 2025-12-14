package com.dasi.domain.point.service.query;

import com.dasi.domain.point.model.entity.TradeEntity;
import com.dasi.domain.point.model.io.QueryActivityConvertContext;
import com.dasi.domain.point.model.io.QueryActivityConvertResult;
import com.dasi.domain.point.model.io.QueryActivityRechargeContext;
import com.dasi.domain.point.model.io.QueryActivityRechargeResult;
import com.dasi.domain.point.repository.IPointRepository;
import com.dasi.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PointQuery implements IPointQuery {

    @Resource
    private IPointRepository tradeRepository;

    @Override
    public List<QueryActivityConvertResult> queryActivityConvertList(QueryActivityConvertContext queryActivityConvertContext) {

        Long activityId = queryActivityConvertContext.getActivityId();
        if (activityId == null) throw new AppException("缺少参数 activityId");

        List<TradeEntity> tradeEntityList = tradeRepository.queryActivityConvertList(activityId);

        return tradeEntityList.stream()
                .map(tradeEntity -> QueryActivityConvertResult.builder()
                        .tradeId(tradeEntity.getTradeId())
                        .tradePoint(tradeEntity.getTradePoint())
                        .tradeName(tradeEntity.getTradeName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<QueryActivityRechargeResult> queryActivityRechargeList(QueryActivityRechargeContext queryActivityRechargeContext) {
        Long activityId = queryActivityRechargeContext.getActivityId();
        if (activityId == null) throw new AppException("缺少参数 activityId");

        List<TradeEntity> tradeEntityList = tradeRepository.queryActivityRechargeList(activityId);

        return tradeEntityList.stream()
                .map(tradeEntity -> QueryActivityRechargeResult.builder()
                        .tradeId(tradeEntity.getTradeId())
                        .tradeType(tradeEntity.getTradeType())
                        .tradePoint(tradeEntity.getTradePoint())
                        .tradeMoney(tradeEntity.getTradeMoney())
                        .tradeValue(tradeEntity.getTradeValue())
                        .tradeName(tradeEntity.getTradeName())
                        .tradeDesc(tradeEntity.getTradeDesc())
                        .build())
                .collect(Collectors.toList());
    }

}

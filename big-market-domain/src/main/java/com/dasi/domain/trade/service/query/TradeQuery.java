package com.dasi.domain.trade.service.query;

import com.dasi.domain.trade.model.entity.TradeEntity;
import com.dasi.domain.trade.model.io.QueryActivityConvertContext;
import com.dasi.domain.trade.model.io.QueryActivityConvertResult;
import com.dasi.domain.trade.repository.ITradeRepository;
import com.dasi.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TradeQuery implements ITradeQuery {

    @Resource
    private ITradeRepository tradeRepository;

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

}

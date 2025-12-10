package com.dasi.domain.trade.service.query;

import com.dasi.domain.trade.model.entity.TradeEntity;
import com.dasi.domain.trade.model.io.QueryConvertContext;
import com.dasi.domain.trade.model.io.QueryConvertResult;
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
    public List<QueryConvertResult> queryConvertListByActivityId(QueryConvertContext queryConvertContext) {

        Long activityId = queryConvertContext.getActivityId();
        if (activityId == null) throw new AppException("缺少参数 activityId");

        List<TradeEntity> tradeEntityList = tradeRepository.queryConvertListByActivityId(activityId);

        return tradeEntityList.stream()
                .map(tradeEntity -> QueryConvertResult.builder()
                        .tradeId(tradeEntity.getTradeId())
                        .tradePoint(tradeEntity.getTradePoint())
                        .tradeName(tradeEntity.getTradeName())
                        .build())
                .collect(Collectors.toList());
    }

}

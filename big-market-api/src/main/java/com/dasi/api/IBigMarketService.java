package com.dasi.api;

import com.dasi.api.dto.*;
import com.dasi.types.model.Result;

import java.util.List;

@SuppressWarnings("unused")
public interface IBigMarketService {

    Result<List<QueryConvertResponse>> queryConvert(QueryConvertRequest queryConvertRequest);












    Result<QueryAccountResponse> queryActivityAccount(QueryAccountRequest queryAccountRequest);

    Result<Boolean> querySign(QuerySign querySign);

    Result<Void> assemble(Long activityId);

    Result<RaffleResponse> raffle(RaffleRequest raffleRequest);

    Result<BehaviorResponse> behaviorSign(BehaviorRequest behaviorRequest);

    Result<BehaviorResponse> behaviorLike(BehaviorRequest behaviorRequest);

    Result<BehaviorResponse> behaviorShare(BehaviorRequest behaviorRequest);

    Result<BehaviorResponse> behaviorComment(BehaviorRequest behaviorRequest);


    Result<List<ActivityAwardResponse>> queryActivityAward(ActivityAwardRequest activityAwardRequest);

    Result<StrategyRuleWeightResponse> queryStrategyRuleWeight(StrategyRuleWeightRequest strategyRuleWeightRequest);

    Result<ConvertResponse> convert(ConvertRequest convertRequest);

}

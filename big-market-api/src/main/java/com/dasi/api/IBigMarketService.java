package com.dasi.api;

import com.dasi.api.dto.*;
import com.dasi.types.model.Result;

import java.util.List;

@SuppressWarnings("unused")
public interface IBigMarketService {

    Result<QueryActivityAccountResponse> queryActivityAccount(QueryActivityAccountRequest queryActivityAccountRequest);

    Result<List<QueryActivityConvertResponse>> queryActivityConvert(QueryActivityConvertRequest queryActivityConvertRequest);

    Result<List<QueryUserAwardResponse>> queryUserAwardRaffle(QueryUserAwardRequest queryUserAwardRequest);

    Result<List<QueryActivityAwardResponse>> queryActivityAward(QueryActivityAwardRequest queryActivityAwardRequest);

    Result<List<QueryActivityBehaviorResponse>> queryActivityBehavior(QueryActivityBehaviorRequest queryActivityBehaviorRequest);

    Result<QueryActivityLuckResponse> queryActivityLuck(QueryActivityLuckRequest queryActivityLuckRequest);









    Result<Void> assemble(Long activityId);

    Result<RaffleResponse> raffle(RaffleRequest raffleRequest);

    Result<BehaviorResponse> behaviorSign(BehaviorRequest behaviorRequest);

    Result<BehaviorResponse> behaviorLike(BehaviorRequest behaviorRequest);

    Result<BehaviorResponse> behaviorShare(BehaviorRequest behaviorRequest);

    Result<BehaviorResponse> behaviorComment(BehaviorRequest behaviorRequest);


    Result<TradeResponse> trade(TradeRequest tradeRequest);

}

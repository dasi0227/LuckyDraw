package com.dasi.api;

import com.dasi.api.dto.*;
import com.dasi.types.model.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@SuppressWarnings("unused")
public interface ILuckyDrawService {

    Result<QueryActivityAccountResponse> queryActivityAccount(QueryActivityAccountRequest queryActivityAccountRequest);

    Result<List<QueryActivityConvertResponse>> queryActivityConvert(QueryActivityConvertRequest queryActivityConvertRequest);

    Result<List<QueryUserAwardResponse>> queryUserAwardRaffle(QueryUserAwardRequest queryUserAwardRequest);

    Result<List<QueryActivityAwardResponse>> queryActivityAward(QueryActivityAwardRequest queryActivityAwardRequest);

    Result<List<QueryActivityBehaviorResponse>> queryActivityBehavior(QueryActivityBehaviorRequest queryActivityBehaviorRequest);

    Result<QueryActivityLuckResponse> queryActivityLuck(QueryActivityLuckRequest queryActivityLuckRequest);

    Result<QueryActivityInfoResponse> queryActivityInfo(QueryActivityInfoRequest activityInfoRequest);

    Result<RaffleResponse> raffle(RaffleRequest raffleRequest);

    Result<BehaviorResponse> behavior(BehaviorRequest behaviorRequest);

    @PostMapping("/trade")
    Result<TradeResponse> recharge(@RequestBody TradeRequest tradeRequest);

    Result<TradeResponse> convert(TradeRequest tradeRequest);

    Result<FortuneResponse> fortune(FortuneRequest fortuneRequest);

    Result<List<QueryActivityResponse>> queryActivityList();

    Result<List<QueryActivityRechargeResponse>> queryActivityRecharge(QueryActivityRechargeRequest queryActivityRechargeRequest);

}

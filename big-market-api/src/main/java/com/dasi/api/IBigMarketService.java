package com.dasi.api;

import com.dasi.api.dto.*;
import com.dasi.types.model.Result;

import java.util.List;

@SuppressWarnings("unused")
public interface IBigMarketService {

    Result<Boolean> isSign(IsSignRequestDTO isSignRequestDTO);

    Result<Void> assemble(Long activityId);

    Result<RaffleResponseDTO> raffle(RaffleRequestDTO raffleRequestDTO);

    Result<BehaviorResponseDTO> behaviorSign(BehaviorRequestDTO behaviorRequestDTO);

    Result<BehaviorResponseDTO> behaviorLike(BehaviorRequestDTO behaviorRequestDTO);

    Result<BehaviorResponseDTO> behaviorShare(BehaviorRequestDTO behaviorRequestDTO);

    Result<BehaviorResponseDTO> behaviorComment(BehaviorRequestDTO behaviorRequestDTO);

    Result<ActivityAccountResponseDTO> queryActivityAccount(ActivityAccountRequestDTO activityAccountRequestDTO);

    Result<List<ActivityAwardResponseDTO>> queryActivityAward(ActivityAwardRequestDTO activityAwardRequestDTO);

}

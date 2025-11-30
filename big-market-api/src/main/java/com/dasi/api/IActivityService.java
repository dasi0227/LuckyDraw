package com.dasi.api;

import com.dasi.api.dto.*;
import com.dasi.types.model.Result;

import java.util.List;

@SuppressWarnings("unused")
public interface IActivityService {

    Result<Boolean> assemble(Long activityId);

    Result<RechargeResponseDTO> recharge(RechargeRequestDTO rechargeRequestDTO);

    Result<RaffleResponseDTO> raffle(RaffleRequestDTO raffleRequestDTO);

    Result<List<AwardListResponseDTO>> award(AwardListRequestDTO awardListRequestDTO);

}

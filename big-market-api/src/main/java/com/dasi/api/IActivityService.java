package com.dasi.api;

import com.dasi.api.dto.RaffleRequestDTO;
import com.dasi.api.dto.RaffleResponseDTO;
import com.dasi.api.dto.RechargeRequestDTO;
import com.dasi.api.dto.RechargeResponseDTO;
import com.dasi.types.model.Result;

@SuppressWarnings("unused")
public interface IActivityService {

    Result<Boolean> assemble(Long activityId);

    Result<RechargeResponseDTO> recharge(RechargeRequestDTO rechargeRequestDTO);

    Result<RaffleResponseDTO> raffle(RaffleRequestDTO raffleRequestDTO);

}

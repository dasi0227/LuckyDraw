package com.dasi.api;

import com.dasi.api.dto.RaffleAwardListRequestDTO;
import com.dasi.api.dto.RaffleAwardListResponseDTO;
import com.dasi.api.dto.RaffleRequestDTO;
import com.dasi.api.dto.RaffleResponseDTO;
import com.dasi.types.model.Result;

import java.util.List;

public interface IRaffleService {

    /** 策略装配 */
    Result<Boolean> strategyAssemble(Long strategyId);

    /** 获取奖品列表 */
    Result<List<RaffleAwardListResponseDTO>> queryRaffleAwardList(RaffleAwardListRequestDTO raffleAwardListRequestDTO);

    /** 抽奖接口 */
    Result<RaffleResponseDTO> raffle(RaffleRequestDTO raffleRequestDTO);

}

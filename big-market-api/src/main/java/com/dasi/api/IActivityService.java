package com.dasi.api;

import com.dasi.api.dto.*;
import com.dasi.types.model.Result;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@SuppressWarnings("unused")
public interface IActivityService {

    Result<Boolean> assemble(Long activityId);

    Result<RaffleResponseDTO> raffle(RaffleRequestDTO raffleRequestDTO);

    Result<List<AwardListResponseDTO>> award(AwardListRequestDTO awardListRequestDTO);

    Result<BehaviorSignResponseDTO> behavior(@RequestBody BehaviorSignRequestDTO behaviorSignRequestDTO);

}

package com.dasi.trigger.http;

import com.dasi.api.IRaffleService;
import com.dasi.api.dto.RaffleAwardListRequestDTO;
import com.dasi.api.dto.RaffleAwardListResponseDTO;
import com.dasi.api.dto.RaffleRequestDTO;
import com.dasi.api.dto.RaffleResponseDTO;
import com.dasi.domain.strategy.model.dto.RaffleContext;
import com.dasi.domain.strategy.model.dto.RaffleResult;
import com.dasi.domain.strategy.model.entity.StrategyAwardEntity;
import com.dasi.domain.strategy.service.assemble.IAssemble;
import com.dasi.domain.strategy.service.raffle.IRaffle;
import com.dasi.types.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/${app.config.api-version}/raffle")
public class IRaffleController implements IRaffleService {

    @Resource
    private IAssemble assemble;

    @Resource
    private IRaffle raffle;

    @GetMapping("/assemble")
    @Override
    public Result<Boolean> strategyAssemble(Long strategyId) {
        boolean result = assemble.assembleStrategy(strategyId);
        return Result.success(result);
    }

    @PostMapping("/award")
    @Override
    public Result<List<RaffleAwardListResponseDTO>> queryRaffleAwardList(@RequestBody RaffleAwardListRequestDTO raffleAwardListRequestDTO) {
        List<StrategyAwardEntity> strategyAwardEntities = raffle.queryRaffleStrategyAwardList(raffleAwardListRequestDTO.getStrategyId());
        List<RaffleAwardListResponseDTO> result = strategyAwardEntities.stream()
                .map(e -> RaffleAwardListResponseDTO.builder()
                        .awardId(e.getAwardId())
                        .awardTitle(e.getAwardTitle())
                        .build())
                .collect(Collectors.toList());
        return Result.success(result);
    }

    @PostMapping("/raffle")
    @Override
    public Result<RaffleResponseDTO> raffle(@RequestBody RaffleRequestDTO raffleRequestDTO) {
        RaffleContext raffleContext = RaffleContext.builder()
                .strategyId(raffleRequestDTO.getStrategyId())
                .userId("dasi")
                .build();
        log.info("【执行抽奖】raffleContext = {}", raffleContext);
        RaffleResult raffleResult = raffle.doRaffle(raffleContext);
        log.info("【执行抽奖】raffleResult = {}", raffleResult);
        RaffleResponseDTO raffleResponseDTO = RaffleResponseDTO.builder()
                .awardId(raffleResult.getAwardId())
                .build();
        return Result.success(raffleResponseDTO);
    }

}

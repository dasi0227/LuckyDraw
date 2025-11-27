package com.dasi.trigger.http;

import com.dasi.api.IRaffleService;
import com.dasi.api.dto.RaffleAwardListRequestDTO;
import com.dasi.api.dto.RaffleAwardListResponseDTO;
import com.dasi.api.dto.RaffleRequestDTO;
import com.dasi.api.dto.RaffleResponseDTO;
import com.dasi.domain.strategy.model.dto.LotteryContext;
import com.dasi.domain.strategy.model.dto.LotteryResult;
import com.dasi.domain.strategy.model.entity.StrategyAwardEntity;
import com.dasi.domain.strategy.service.assemble.IStrategyAssemble;
import com.dasi.domain.strategy.service.lottery.IStrategyLottery;
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
    private IStrategyAssemble assemble;

    @Resource
    private IStrategyLottery raffle;

    @GetMapping("/assemble")
    @Override
    public Result<Boolean> strategyAssemble(Long strategyId) {
        boolean result = assemble.assembleStrategy(strategyId);
        return Result.success(result);
    }

    @PostMapping("/award")
    @Override
    public Result<List<RaffleAwardListResponseDTO>> queryRaffleAwardList(@RequestBody RaffleAwardListRequestDTO raffleAwardListRequestDTO) {
        List<StrategyAwardEntity> strategyAwardEntities = raffle.queryStrategyAwardList(raffleAwardListRequestDTO.getStrategyId());
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
        LotteryContext lotteryContext = LotteryContext.builder()
                .strategyId(raffleRequestDTO.getStrategyId())
                .userId("dasi")
                .build();
        log.info("【执行抽奖】lotteryContext = {}", lotteryContext);
        LotteryResult lotteryResult = raffle.doStrategyLottery(lotteryContext);
        log.info("【执行抽奖】lotteryResult = {}", lotteryResult);
        RaffleResponseDTO raffleResponseDTO = RaffleResponseDTO.builder()
                .awardId(lotteryResult.getAwardId())
                .build();
        return Result.success(raffleResponseDTO);
    }

}

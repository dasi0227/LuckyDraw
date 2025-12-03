package com.dasi.infrastructure.common;

import com.dasi.domain.common.IUniqueIdGenerator;
import com.dasi.infrastructure.persistent.redis.IRedisService;
import com.dasi.types.constant.Delimiter;
import com.dasi.types.constant.RedisKey;
import com.dasi.types.util.TimeUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;

@Service
public class UniqueIdGenerator implements IUniqueIdGenerator {

    @Resource
    private IRedisService redisService;

    public String nextRechargeOrderId() {
        return buildOrderId(RedisKey.RECHARGE_ORDER_ID_KEY);
    }

    public String nextRaffleOrderId() {
        return buildOrderId(RedisKey.RAFFLE_ORDER_ID_KEY);
    }

    public String nextRewardOrderId() {
        return buildOrderId(RedisKey.REWARD_ORDER_ID_KEY);
    }

    public String nextMessageId() {
        return UUID.randomUUID().toString();
    }

    private String buildOrderId(String type) {
        String date = TimeUtil.thisDay(false);
        String cacheKey = RedisKey.PREFIX + type + Delimiter.COLON + date;
        long seq = redisService.incr(cacheKey);
        return type.substring(0, 3)
                + date
                + "-"
                + String.format("%06d", seq)
                + "-"
                + TimeUtil.currentTimeMillis()
                + "-"
                + RandomStringUtils.randomAlphabetic(6);
    }

}

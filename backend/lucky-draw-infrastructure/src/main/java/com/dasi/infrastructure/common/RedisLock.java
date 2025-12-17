package com.dasi.infrastructure.common;

import com.dasi.domain.common.IRedisLock;
import com.dasi.infrastructure.persistent.redis.IRedisService;
import com.dasi.types.annotation.DCCValue;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class RedisLock implements IRedisLock {

    @DCCValue("redisLockMaxRetry:3")
    private String redisLockMaxRetry;

    @DCCValue("redisLockWaitTime:50")
    private String redisLockWaitTime;

    @DCCValue("redisLockTTL:3000")
    private String redisLockTTL;

    @Resource
    private IRedisService redisService;

    @Override
    public boolean tryLock(String key) {
        RLock lock = redisService.getLock(key);

        int retry = Integer.parseInt(redisLockMaxRetry);
        long wait = Long.parseLong(redisLockWaitTime);
        long ttl = Long.parseLong(redisLockTTL);

        for (int i = 1; i <= retry; i++) {
            try {
                if (lock.tryLock(wait, ttl, TimeUnit.MILLISECONDS)) {
                    return true;
                }

                long backoff = 50L * i + (long) (Math.random() * 20);
                TimeUnit.MILLISECONDS.sleep(backoff);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    @Override
    public void unlock(String key) {
        RLock lock = redisService.getLock(key);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

}

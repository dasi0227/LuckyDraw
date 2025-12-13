package com.dasi.infrastructure.common;

import com.dasi.domain.common.IRedisLock;
import com.dasi.infrastructure.persistent.redis.IRedisService;
import com.dasi.types.constant.DefaultValue;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class RedisLock implements IRedisLock {

    @Resource
    private IRedisService redisService;

    @Override
    public boolean tryLock(String key) {
        RLock lock = redisService.getLock(key);

        for (int i = 1; i <= DefaultValue.MAX_RETRY; i++) {
            try {
                if (lock.tryLock(DefaultValue.LOCK_WAIT, DefaultValue.LOCK_TTL, TimeUnit.MILLISECONDS)) {
                    return true;
                }

                long jitter = (long) (Math.random() * 20);
                long backoff = DefaultValue.BASE_BACKOFF * i + jitter;
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

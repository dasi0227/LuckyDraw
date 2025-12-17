package com.dasi.domain.common;

public interface IRedisLock {

    boolean tryLock(String key);

    void unlock(String key);

}

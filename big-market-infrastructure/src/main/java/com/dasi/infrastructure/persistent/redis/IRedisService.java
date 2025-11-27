package com.dasi.infrastructure.persistent.redis;

import org.redisson.api.*;

import java.time.Duration;

@SuppressWarnings("unused")
public interface IRedisService {

    /** 设置指定 key 的值（不过期） */
    <T> void setValue(String key, T value);

    /** 设置指定 key 的值（含过期时间，毫秒） */
    <T> void setValue(String key, T value, long expired);

    /** 获取指定 key 的值 */
    <T> T getValue(String key);

    /** 删除指定 key */
    void remove(String key);

    /** 判断 key 是否存在 */
    boolean isExists(String key);

    /** 自增 */
    long incr(String key);

    /** 自增 delta */
    long incrBy(String key, long delta);

    /** 自减 */
    long decr(String key);

    /** 自减 delta */
    long decrBy(String key, long delta);

    /** 获取普通队列 */
    <T> RQueue<T> getQueue(String key);

    /** 获取阻塞队列 */
    <T> RBlockingQueue<T> getBlockingQueue(String key);

    /** 获取延迟队列 */
    <T> RDelayedQueue<T> getDelayedQueue(RBlockingQueue<T> rBlockingQueue);

    /** 添加到 Set */
    void addToSet(String key, String value);

    /** 判断 Set 是否含某元素 */
    boolean isSetMember(String key, String value);

    /** 添加到 List */
    void addToList(String key, String value);

    /** 获取 List 指定索引的值 */
    String getFromList(String key, int index);

    /** 向 Hash(Map) 添加 field-value */
    void addToMap(String key, String field, String value);

    /** 获取 Redis Map（Hash） */
    RMap<String, String> getMap(String key);

    /** 获取 Hash(Map) 中 field 对应值 */
    String getFromMap(String key, String field);

    /** 添加到 SortedSet */
    void addToSortedSet(String key, String value);

    /** 可重入锁 */
    RLock getLock(String key);

    /** 公平锁 */
    RLock getFairLock(String key);

    /** 读写锁 */
    RReadWriteLock getReadWriteLock(String key);

    /** 信号量 */
    RSemaphore getSemaphore(String key);

    /** 可过期信号量 */
    RPermitExpirableSemaphore getPermitExpirableSemaphore(String key);

    /** 闭锁 */
    RCountDownLatch getCountDownLatch(String key);

    /** 布隆过滤器 */
    <T> RBloomFilter<T> getBloomFilter(String key);

    /** 获取原子 Long 类型数据 */
    Long getAtomicLong(String key);

    /** 放入原子 Long 类型数据 */
    void setAtomicLong(String key, Long num);

    /** 设置值如果不存在 */
    Boolean setNx(String key);

    /** 设置值如果不存在 */
    Boolean setNx(String key, Duration expire);

    /** 清空键 */
    long deleteByPattern(String pattern);

}
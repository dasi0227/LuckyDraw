package com.dasi.domain.common;

public interface IUniqueIdGenerator {

    String nextRechargeOrderId();
    String nextRaffleOrderId();
    String nextRewardOrderId();
    String nextMessageId();

}

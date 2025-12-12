package com.dasi.domain.strategy.service.chain.impl;

import com.dasi.domain.strategy.service.chain.IStrategyChain;

public abstract class AbstractStrategyChain implements IStrategyChain, Cloneable {

    private IStrategyChain next;

    @Override
    public IStrategyChain next() {
        return next;
    }

    @Override
    public IStrategyChain appendNext(IStrategyChain next) {
        this.next = next;
        return next;
    }

    @Override
    public IStrategyChain clone() {
        try {
            AbstractStrategyChain copy = (AbstractStrategyChain) super.clone();
            if (this.next != null) {
                copy.next = this.next.clone();
            }
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }


}

package com.dasi.domain.activity.service.chain.impl;

import com.dasi.domain.activity.service.chain.IActivityChain;

public abstract class AbstractActivityChain implements IActivityChain, Cloneable {

    private IActivityChain next;

    @Override
    public IActivityChain next() {
        return this.next;
    }

    @Override
    public IActivityChain appendNext(IActivityChain next) {
        this.next = next;
        return next;
    }

    @Override
    public IActivityChain clone() {
        try {
            AbstractActivityChain copy = (AbstractActivityChain) super.clone();
            if (this.next != null) {
                copy.next = this.next.clone();
            }
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone IActionChain failed", e);
        }
    }
}

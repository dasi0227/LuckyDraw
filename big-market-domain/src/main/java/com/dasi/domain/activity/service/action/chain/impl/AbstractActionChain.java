package com.dasi.domain.activity.service.action.chain.impl;

import com.dasi.domain.activity.service.action.chain.IActionChain;

public abstract class AbstractActionChain implements IActionChain {

    private IActionChain next;

    @Override
    public IActionChain next() {
        return this.next;
    }

    @Override
    public IActionChain appendNext(IActionChain next) {
        this.next = next;
        return next;
    }

    @Override
    public IActionChain clone() {
        try {
            AbstractActionChain copy = (AbstractActionChain) super.clone();
            copy.next = null;
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}

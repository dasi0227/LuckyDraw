package com.dasi.domain.activity.service.action.chain.impl;

import com.dasi.domain.activity.service.action.chain.IActionChain;

public abstract class AbstractActionChain implements IActionChain, Cloneable {

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
            if (this.next != null) {
                copy.next = this.next.clone();
            }
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone IActionChain failed", e);
        }
    }
}

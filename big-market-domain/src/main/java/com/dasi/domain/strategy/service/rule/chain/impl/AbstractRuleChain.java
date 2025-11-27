package com.dasi.domain.strategy.service.rule.chain.impl;

import com.dasi.domain.strategy.service.rule.chain.IRuleChain;

public abstract class AbstractRuleChain implements IRuleChain, Cloneable {

    private IRuleChain next;

    @Override
    public IRuleChain next() {
        return next;
    }

    @Override
    public IRuleChain appendNext(IRuleChain next) {
        this.next = next;
        return next;
    }

    @Override
    public IRuleChain clone() {
        try {
            AbstractRuleChain copy = (AbstractRuleChain) super.clone();
            if (this.next != null) {
                copy.next = this.next.clone();
            }
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }


}

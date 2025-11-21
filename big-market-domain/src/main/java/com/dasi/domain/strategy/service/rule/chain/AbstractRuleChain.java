package com.dasi.domain.strategy.service.rule.chain;


public abstract class AbstractRuleChain implements IRuleChain {

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

    protected abstract String myRuleModel();
}

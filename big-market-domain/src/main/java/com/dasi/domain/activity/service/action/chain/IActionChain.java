package com.dasi.domain.activity.service.action.chain;

import com.dasi.domain.activity.model.aggregate.ActionChainCheckAggregate;

public interface IActionChain {

    Boolean action(ActionChainCheckAggregate actionChainCheckAggregate);

    IActionChain next();

    IActionChain appendNext(IActionChain next);

    IActionChain clone();

}

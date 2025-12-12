package com.dasi.domain.activity.service.chain;

import com.dasi.domain.activity.model.aggregate.ActionChainCheckAggregate;

public interface IActivityChain {

    Boolean action(ActionChainCheckAggregate actionChainCheckAggregate);

    IActivityChain next();

    IActivityChain appendNext(IActivityChain next);

    IActivityChain clone();

}

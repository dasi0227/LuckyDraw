package com.dasi.domain.activity.service.chain.impl;

import com.dasi.domain.activity.model.aggregate.ActionChainCheckAggregate;
import com.dasi.domain.activity.model.type.ActionModel;
import org.springframework.stereotype.Component;

@Component(ActionModel.DEFAULT)
public class DefaultChain extends AbstractActivityChain {

    @Override
    public Boolean action(ActionChainCheckAggregate actionChainCheckAggregate) {
        return true;
    }

}

package com.dasi.domain.activity.service.action.chain.impl;

import com.dasi.domain.activity.model.aggregate.ActionChainCheckAggregate;
import com.dasi.domain.activity.model.type.ActionModel;
import org.springframework.stereotype.Component;

@Component(ActionModel.DEFAULT)
public class ActionDefaultChain extends AbstractActionChain {

    @Override
    public Boolean action(ActionChainCheckAggregate actionChainCheckAggregate) {
        return true;
    }

}

package com.dasi.domain.activity.service.action.chain.impl;

import com.dasi.domain.activity.model.dto.ActionChainCheck;
import com.dasi.domain.activity.model.type.ActionModel;
import org.springframework.stereotype.Component;

@Component(ActionModel.DEFAULT)
public class ActionDefaultChain extends AbstractActionChain {

    @Override
    public Boolean action(ActionChainCheck actionChainCheck) {
        return true;
    }

}

package com.dasi.domain.activity.service.action.chain;

import com.dasi.domain.activity.model.dto.ActionChainCheck;

public interface IActionChain {

    Boolean action(ActionChainCheck actionChainCheck);

    IActionChain next();

    IActionChain appendNext(IActionChain next);

    IActionChain clone();

}

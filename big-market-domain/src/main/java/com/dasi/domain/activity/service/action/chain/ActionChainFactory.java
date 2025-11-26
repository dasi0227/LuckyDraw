package com.dasi.domain.activity.service.action.chain;

import com.dasi.domain.activity.model.type.ActionModel;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ActionChainFactory {

    private final IActionChain actionChain;

    public IActionChain getFirstActionChain() {
        return actionChain;
    }

    public ActionChainFactory(Map<String, IActionChain> actionChainMap) {

        actionChain = actionChainMap.get(ActionModel.ACTION_BASIC_INFO);
        actionChain.appendNext(actionChainMap.get(ActionModel.ACTION_SKU_STOCK));

    }

}

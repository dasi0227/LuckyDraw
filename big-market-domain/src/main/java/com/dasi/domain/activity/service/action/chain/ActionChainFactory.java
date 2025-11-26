package com.dasi.domain.activity.service.action.chain;

import com.dasi.domain.activity.model.action.ActionModel;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ActionChainFactory {

    private final IActionChain actionChain;

    public IActionChain getActionChain() {
        return actionChain;
    }

    public ActionChainFactory(Map<String, IActionChain> actionChainMap) {

        actionChain = actionChainMap.get(ActionModel.ACTION_BASE);
        actionChain.appendNext(actionChainMap.get(ActionModel.ACTION_SKU_STOCK));

    }

}

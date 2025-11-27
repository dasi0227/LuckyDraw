package com.dasi.domain.activity.service.action.chain;

import com.dasi.domain.activity.model.type.ActionModel;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ActionChainFactory {

    private final Map<String, IActionChain> actionChainMap = new ConcurrentHashMap<>();

    public ActionChainFactory(Map<String, IActionChain> actionChainMap) {
        this.actionChainMap.putAll(actionChainMap);
    }

    public IActionChain getRaffleActionChain() {
        IActionChain head = actionChainMap.get(ActionModel.ACTION_BASIC).clone();
        head.appendNext(actionChainMap.get(ActionModel.ACTION_DEFAULT).clone());
        return head;
    }

    public IActionChain getRechargeActionChain() {
        IActionChain head = actionChainMap.get(ActionModel.ACTION_BASIC).clone();
        head.appendNext(actionChainMap.get(ActionModel.ACTION_STOCK).clone())
            .appendNext(actionChainMap.get(ActionModel.ACTION_DEFAULT).clone());
        return head;
    }

}

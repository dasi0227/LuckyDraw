package com.dasi.domain.activity.service.action.chain;

import com.dasi.domain.activity.model.type.ActionModel;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ActionChainFactory {

    private final IActionChain raffleActionChainPrototype;

    private final IActionChain rechargeActionChainPrototype;

    public ActionChainFactory(Map<String, IActionChain> actionChainMap) {
        IActionChain raffleHead = actionChainMap.get(ActionModel.ACTION_BASIC).clone();
        raffleHead.appendNext(actionChainMap.get(ActionModel.ACTION_DEFAULT).clone());
        this.raffleActionChainPrototype = raffleHead;

        IActionChain rechargeHead = actionChainMap.get(ActionModel.ACTION_BASIC).clone();
        rechargeHead.appendNext(actionChainMap.get(ActionModel.ACTION_STOCK).clone())
                    .appendNext(actionChainMap.get(ActionModel.ACTION_DEFAULT).clone());
        this.rechargeActionChainPrototype = rechargeHead;
    }

    public IActionChain getRaffleActionChain() {
        return this.raffleActionChainPrototype.clone();
    }

    public IActionChain getRechargeActionChain() {
        return this.rechargeActionChainPrototype.clone();
    }

}

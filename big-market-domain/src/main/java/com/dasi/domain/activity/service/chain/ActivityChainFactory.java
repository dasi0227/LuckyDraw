package com.dasi.domain.activity.service.chain;

import com.dasi.domain.activity.model.type.ActionModel;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ActivityChainFactory {

    private final IActivityChain raffleActionChainPrototype;

    private final IActivityChain rechargeActionChainPrototype;

    public ActivityChainFactory(Map<String, IActivityChain> actionChainMap) {
        IActivityChain raffleHead = actionChainMap.get(ActionModel.ACTIVITY_INFO).clone();
        raffleHead.appendNext(actionChainMap.get(ActionModel.ACCOUNT_INFO).clone())
                  .appendNext(actionChainMap.get(ActionModel.DEFAULT).clone());
        this.raffleActionChainPrototype = raffleHead;

        IActivityChain rechargeHead = actionChainMap.get(ActionModel.ACTIVITY_INFO).clone();
        rechargeHead.appendNext(actionChainMap.get(ActionModel.SKU_STOCK).clone())
                    .appendNext(actionChainMap.get(ActionModel.DEFAULT).clone());
        this.rechargeActionChainPrototype = rechargeHead;
    }

    public IActivityChain getRaffleActionChain() {
        return this.raffleActionChainPrototype.clone();
    }

    public IActivityChain getRechargeActionChain() {
        return this.rechargeActionChainPrototype.clone();
    }

}

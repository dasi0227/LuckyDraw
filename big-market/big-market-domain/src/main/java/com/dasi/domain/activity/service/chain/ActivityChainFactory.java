package com.dasi.domain.activity.service.chain;

import com.dasi.domain.activity.annotation.ActionModelConfig;
import com.dasi.domain.activity.model.type.ActionModel;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ActivityChainFactory {

    private final IActivityChain raffleActionChainPrototype;

    private final IActivityChain rechargeActionChainPrototype;

    private final Map<String, IActivityChain> activityChainMap = new ConcurrentHashMap<>();

    public ActivityChainFactory(List<IActivityChain> activityChainList) {
        activityChainList.forEach(activityChain -> {
            ActionModelConfig actionModelConfig = AnnotationUtils.findAnnotation(activityChain.getClass(), ActionModelConfig.class);
            if (actionModelConfig != null) {
                this.activityChainMap.put(actionModelConfig.actionModel().name(), activityChain);
            }
        });

        IActivityChain raffleHead = activityChainMap.get(ActionModel.ACTIVITY_INFO_1.name()).clone();
        raffleHead.appendNext(activityChainMap.get(ActionModel.ACCOUNT_INFO.name()).clone())
                  .appendNext(activityChainMap.get(ActionModel.DEFAULT.name()).clone());
        this.raffleActionChainPrototype = raffleHead;

        IActivityChain rechargeHead = activityChainMap.get(ActionModel.ACTIVITY_INFO_2.name()).clone();
        rechargeHead.appendNext(activityChainMap.get(ActionModel.SKU_STOCK.name()).clone())
                    .appendNext(activityChainMap.get(ActionModel.DEFAULT.name()).clone());
        this.rechargeActionChainPrototype = rechargeHead;
    }

    public IActivityChain getRaffleActionChain() {
        return this.raffleActionChainPrototype.clone();
    }

    public IActivityChain getRechargeActionChain() {
        return this.rechargeActionChainPrototype.clone();
    }

}

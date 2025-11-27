package com.dasi.domain.activity.service.raffle.impl;

import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.repository.IActivityRepository;
import com.dasi.domain.activity.service.action.chain.ActionChainFactory;
import com.dasi.domain.activity.service.action.chain.IActionChain;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DefaultActivityRaffle extends AbstractActivityRaffle {

    @Resource
    private ActionChainFactory actionChainFactory;

    public DefaultActivityRaffle(IActivityRepository activityRepository) {
        super(activityRepository);
    }

    @Override
    protected Boolean checkRaffleValid(ActivityEntity activityEntity) {
        IActionChain actionChain = actionChainFactory.getRaffleActionChain();
        return actionChain.action(null, activityEntity, null);
    }

}

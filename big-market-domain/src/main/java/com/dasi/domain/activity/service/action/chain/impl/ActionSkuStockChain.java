package com.dasi.domain.activity.service.action.chain.impl;

import com.dasi.domain.activity.model.action.ActionModel;
import com.dasi.domain.activity.model.entity.ActivityCountEntity;
import com.dasi.domain.activity.model.entity.ActivityEntity;
import com.dasi.domain.activity.model.entity.ActivitySkuEntity;
import com.dasi.domain.activity.service.action.chain.AbstractActionChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component(ActionModel.ACTION_SKU_STOCK)
public class ActionSkuStockChain extends AbstractActionChain {
    @Override
    public boolean action(ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity) {
        log.info("【活动责任链 - action_sku_stock】校验。。。");
        if (next() != null) {
            return next().action(activitySkuEntity, activityEntity, activityCountEntity);
        } else {
            log.info("【活动责任链】默认返回 true");
            return true;
        }
    }
}

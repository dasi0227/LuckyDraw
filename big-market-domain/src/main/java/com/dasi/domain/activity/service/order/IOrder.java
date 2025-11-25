package com.dasi.domain.activity.service.order;

import com.dasi.domain.activity.model.entity.ActivityOrderEntity;
import com.dasi.domain.activity.model.entity.ActivityShoppingCartEntity;

public interface IOrder {

    ActivityOrderEntity createActivityOrder(ActivityShoppingCartEntity activityShoppingCartEntity);

}

package com.dasi.domain.activity.service.order;

import com.dasi.domain.activity.repository.IActivityRepository;
import org.springframework.stereotype.Service;

@Service
public class DefaultOrder extends AbstractOrder {

    public DefaultOrder(IActivityRepository activityRepository) {
        super(activityRepository);
    }

}

package com.dasi.domain.award.service.deliver;

import com.dasi.domain.award.model.io.DeliverContext;
import com.dasi.domain.award.model.io.DeliverResult;

public interface IAwardDeliver {

    DeliverResult doAwardDeliver(DeliverContext deliverContext);

}

package com.dasi.domain.behavior.service.action;

import com.dasi.domain.behavior.model.io.BehaviorContext;
import com.dasi.domain.behavior.model.io.BehaviorResult;

public interface IUserAction {

    BehaviorResult doUserAction(BehaviorContext behaviorContext);

}

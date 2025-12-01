package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.Behavior;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IBehaviorDao {

    Behavior queryBehaviorByBehaviorId(Long behaviorId);

}

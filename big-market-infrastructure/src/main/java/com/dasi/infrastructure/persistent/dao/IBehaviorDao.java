package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.Behavior;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IBehaviorDao {

    Behavior queryBehaviorByBehaviorId(Long behaviorId);

    List<Behavior> queryBehaviorListByBehaviorIds(List<Long> behaviorIds);

}

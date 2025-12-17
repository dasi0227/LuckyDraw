package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.Award;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IAwardDao {

    Award queryAwardByAwardId(Long awardId);

}

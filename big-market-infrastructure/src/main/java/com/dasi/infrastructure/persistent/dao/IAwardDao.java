package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.Award;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IAwardDao {
    List<Award> queryAwardList();

    Award queryAwardByAwardId(Long awardId);
}

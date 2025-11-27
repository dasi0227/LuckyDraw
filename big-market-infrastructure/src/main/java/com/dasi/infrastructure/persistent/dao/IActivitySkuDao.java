package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.ActivitySku;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IActivitySkuDao {

    ActivitySku queryActivitySkuBySku(Long skuId);

    void updateActivitySkuStock(Long skuId);

    void clearActivitySkuStock(Long skuId);
}

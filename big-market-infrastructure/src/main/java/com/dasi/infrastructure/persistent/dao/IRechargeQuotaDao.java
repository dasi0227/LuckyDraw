package com.dasi.infrastructure.persistent.dao;

import com.dasi.infrastructure.persistent.po.RechargeQuota;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IRechargeQuotaDao {

    RechargeQuota queryRechargeQuotaByQuotaId(Long quotaId);

}

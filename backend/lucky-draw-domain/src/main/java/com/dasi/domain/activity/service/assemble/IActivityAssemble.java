package com.dasi.domain.activity.service.assemble;

public interface IActivityAssemble {

    boolean assembleRechargeSkuStockByActivityId(Long activityId);

    boolean assembleRechargeSkuStockBySkuId(Long skuId);

}

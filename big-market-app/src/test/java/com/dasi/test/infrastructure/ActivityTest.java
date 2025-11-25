package com.dasi.test.infrastructure;

import com.dasi.infrastructure.persistent.dao.IActivityDao;
import com.dasi.infrastructure.persistent.dao.IActivityOrderDao;
import com.dasi.infrastructure.persistent.po.Activity;
import com.dasi.infrastructure.persistent.po.ActivityOrder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ActivityTest {

    @Resource
    private IActivityDao activityDao;

    @Resource
    private IActivityOrderDao activityOrderDao;

    @Test
    public void test() {
        Activity activity = activityDao.queryActivityByActivityId(1001L);
        log.info("查询结果：{}", activity);

        for (int i = 0; i < 10; i++) {
            ActivityOrder activityOrder = new ActivityOrder();
            activityOrder.setUserId("user" + i);
            activityOrder.setStrategyId(activity.getStrategyId());
            activityOrder.setActivityId(1001L);
            activityOrder.setActivityName(activity.getActivityName());
            activityOrder.setOrderId(RandomStringUtils.randomNumeric(12));
            activityOrder.setOrderTime(LocalDateTime.now());
            activityOrder.setState("unused");
            activityOrderDao.insert(activityOrder);
        }

        List<ActivityOrder> orderList;

        orderList = activityOrderDao.queryActivityOrderListByUserId("user1");
        log.info("user1 的订单列表：{}", orderList);

        orderList = activityOrderDao.queryActivityOrderListByUserId("user2");
        log.info("user2 的订单列表：{}", orderList);

        orderList = activityOrderDao.queryActivityOrderListByUserId("user3");
        log.info("user3 的订单列表：{}", orderList);
    }

}

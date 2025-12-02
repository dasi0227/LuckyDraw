package com.dasi.test.domain;

import com.dasi.domain.behavior.model.io.BehaviorContext;
import com.dasi.domain.behavior.model.io.BehaviorResult;
import com.dasi.domain.behavior.service.action.IBehaviorReact;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class BehaviorTest {

    @Resource
    private IBehaviorReact behaviorReact;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Test
    public void testBehavior() {
        BehaviorContext behaviorContext = BehaviorContext.builder()
                .userId("dasi")
                .behaviorIds(Arrays.asList(6001L, 6002L))
                .businessNo(LocalDate.now().format(dateTimeFormatter))
                .build();
        BehaviorResult behaviorResult = behaviorReact.doBehaviorReact(behaviorContext);
        log.info("订单 IDs：{}", behaviorResult.getOrderIds());
    }

}

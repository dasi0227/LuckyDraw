package com.dasi.domain.activity.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivitySnapshot {

    private String activityName;

    private String activityDesc;

    private LocalDateTime activityBeginTime;

    private LocalDateTime activityEndTime;

    private Integer activityAccountCount;

    private Integer activityAwardCount;

    private Integer activityRaffleCount;

}

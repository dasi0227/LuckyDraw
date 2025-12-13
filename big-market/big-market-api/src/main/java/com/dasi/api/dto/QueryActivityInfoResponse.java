package com.dasi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryActivityInfoResponse {

    private String activityName;

    private String activityDesc;

    private LocalDateTime activityBeginTime;

    private LocalDateTime activityEndTime;

    private Integer activityAccountCount;

    private Integer activityAwardCount;

    private Integer activityRaffleCount;

}

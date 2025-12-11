package com.dasi.domain.award.model.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryUserAwardResult {

    private Long awardId;

    private String awardName;

    private LocalDateTime awardTime;

}

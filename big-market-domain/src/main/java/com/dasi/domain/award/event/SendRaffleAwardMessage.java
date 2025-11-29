package com.dasi.domain.award.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendRaffleAwardMessage {

    private String userId;

    private Integer awardId;

}

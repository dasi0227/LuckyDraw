package com.dasi.domain.award.service.query.impl;

import com.dasi.domain.award.repository.IAwardRepository;
import com.dasi.domain.award.service.query.IAwardQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DefaultAwardQuery implements IAwardQuery {

    @Resource
    private IAwardRepository awardRepository;

}

package com.dasi.domain.point.service.query;

import com.dasi.domain.point.model.io.QueryActivityConvertContext;
import com.dasi.domain.point.model.io.QueryActivityConvertResult;

import java.util.List;

public interface IPointQuery {


    List<QueryActivityConvertResult> queryActivityConvertList(QueryActivityConvertContext queryActivityConvertContext);

}

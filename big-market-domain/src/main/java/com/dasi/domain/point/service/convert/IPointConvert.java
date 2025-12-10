package com.dasi.domain.point.service.convert;

import com.dasi.domain.point.model.io.ConvertContext;

public interface IPointConvert {

    void doConvertAward(ConvertContext convertContext);

    void doConvertRaffle(ConvertContext convertContext);

}

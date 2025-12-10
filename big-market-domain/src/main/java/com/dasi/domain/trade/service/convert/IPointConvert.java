package com.dasi.domain.trade.service.convert;

import com.dasi.domain.trade.model.io.ConvertContext;

public interface IPointConvert {

    void doConvertAward(ConvertContext convertContext);

    void doConvertRaffle(ConvertContext convertContext);

}

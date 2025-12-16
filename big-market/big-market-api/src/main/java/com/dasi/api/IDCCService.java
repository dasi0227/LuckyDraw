package com.dasi.api;

import com.dasi.types.model.Result;

public interface IDCCService {

    Result<Void> toggle(String key);

    Result<Void> set(String key, String value);

    Result<String> get(String key);

}

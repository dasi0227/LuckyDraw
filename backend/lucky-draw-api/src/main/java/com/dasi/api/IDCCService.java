package com.dasi.api;

import com.dasi.types.model.Result;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

public interface IDCCService {

    Result<Void> toggle(String key);

    Result<Void> set(String key, String value);

    Result<String> get(String key);

    @GetMapping("/getAll")
    Result<Map<String, String>> getAll();
}

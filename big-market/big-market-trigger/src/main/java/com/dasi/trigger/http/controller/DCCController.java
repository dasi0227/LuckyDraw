package com.dasi.trigger.http.controller;

import com.dasi.api.IDCCService;
import com.dasi.properties.ZooKeeperConfigProperties;
import com.dasi.types.constant.DefaultValue;
import com.dasi.types.constant.Delimiter;
import com.dasi.types.constant.ExceptionMessage;
import com.dasi.types.exception.BusinessException;
import com.dasi.types.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/api/${app.config.api-version}/big-market/dcc")
public class DCCController implements IDCCService {

    @Resource
    private CuratorFramework curatorFramework;

    @Resource
    private ZooKeeperConfigProperties zooKeeperConfigProperties;

    @PostMapping("/set")
    @Override
    public Result<Void> set(@RequestParam String key, @RequestParam String value) {

        try {
            String path = zooKeeperConfigProperties.getConfigPath() + Delimiter.SLASH + key;
            if (curatorFramework.checkExists().forPath(path) == null) {
                throw new BusinessException(ExceptionMessage.CONFIG_KEY_NOT_EXISTS);
            }

            Stat stat = curatorFramework.setData().forPath(path, value.getBytes(StandardCharsets.UTF_8));

            log.info("【配置】DCC 配置节点值：key={}, value={}, version={}, time={}", path, value, stat.getVersion(), stat.getCtime());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return Result.success();
    }

    @PostMapping("/toggle")
    @Override
    public Result<Void> toggle(@RequestParam String key) {

        try {
            String path = zooKeeperConfigProperties.getConfigPath() + Delimiter.SLASH + key;
            if (curatorFramework.checkExists().forPath(path) == null) {
                throw new BusinessException(ExceptionMessage.CONFIG_KEY_NOT_EXISTS);
            }

            Stat prev = new Stat();
            String data = new String(curatorFramework.getData().storingStatIn(prev).forPath(path), StandardCharsets.UTF_8);
            String value = data.equals(DefaultValue.TOGGLE_ON) ? DefaultValue.TOGGLE_OFF : DefaultValue.TOGGLE_ON;
            Stat stat = curatorFramework.setData().withVersion(prev.getVersion()).forPath(path, value.getBytes(StandardCharsets.UTF_8));

            log.info("【配置】DCC 配置节点值：key={}, value={}, version={}, time={}", path, value, stat.getVersion(), stat.getCtime());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return Result.success();
    }

    @PostMapping("/get")
    @Override
    public Result<String> get(@RequestParam String key) {

        try {
            String path = zooKeeperConfigProperties.getConfigPath() + Delimiter.SLASH + key;
            if (curatorFramework.checkExists().forPath(path) == null) {
                throw new BusinessException(ExceptionMessage.CONFIG_KEY_NOT_EXISTS);
            }

            byte[] data = curatorFramework.getData().forPath(path);
            String value = new String(data, StandardCharsets.UTF_8);
            return Result.success(value);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

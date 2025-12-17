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
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/${app.config.api-version}/lucky-draw/dcc")
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

            log.info("【配置】DCC 配置节点值：key={}, version={}, time={}", path, stat.getVersion(), stat.getCtime());
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

            log.info("【配置】DCC 配置节点值：key={}, version={}, time={}", path, stat.getVersion(), stat.getCtime());
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

            log.info("【配置】DCC 获取节点值：key={}, value={}", path, value);
            return Result.success(value);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/getAll")
    @Override
    public Result<Map<String, String>> getAll() {

        try {
            String root = zooKeeperConfigProperties.getConfigPath();
            if (curatorFramework.checkExists().forPath(root) == null) {
                return Result.success();
            }

            List<String> children = curatorFramework.getChildren().forPath(root);
            Map<String, String> map = new LinkedHashMap<>();

            for (String child : children) {
                String path = root + Delimiter.SLASH + child;
                byte[] data = curatorFramework.getData().forPath(path);
                map.put(child, new String(data, StandardCharsets.UTF_8));
            }

            log.info("【配置】DCC 获取全部配置：map={}", map);
            return Result.success(map);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

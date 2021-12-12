package com.ssk.binlog.distributed;


import com.ssk.binlog.BinlogException;
import com.ssk.binlog.config.BinlogConfig;
import com.ssk.binlog.config.RedisConfig;
import com.ssk.binlog.config.SyncConfig;
import com.ssk.binlog.factory.IClientFactory;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class RedisDistributedHandler implements IDistributedHandler {

    private final Logger log = LoggerFactory.getLogger(RedisDistributedHandler.class);

    //redis配置，支持集群模式
    RedisConfig redisConfig;

    public RedisDistributedHandler(RedisConfig redisConfig) {
        this.redisConfig = redisConfig;
    }

    @Override
    public void start(BinlogConfig binlogPortalConfig) throws BinlogException {
        if (redisConfig == null) {
            throw new BinlogException("redis config can not be null");
        }
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig.setAddress("redis://" + redisConfig.getHost() + ":" + redisConfig.getPort());
        if (!StringUtils.isBlank(redisConfig.getAuth())) {
            singleServerConfig.setPassword(redisConfig.getAuth());
        }
        config.setLockWatchdogTimeout(10000L);
        RedissonClient redisson = Redisson.create(config);

        //新建工厂对象
        IClientFactory binaryLogClientFactory = binlogPortalConfig.getClientFactory();
        binaryLogClientFactory.setPositionHandler(binlogPortalConfig.getPositionHandler());
        binaryLogClientFactory.setLifeCycleFactory(binlogPortalConfig.getLifeCycleFactory());

        //定时创建客户端,抢到锁的就创建
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                binlogPortalConfig.getSyncConfigMap().forEach((key, syncConfig) -> {
                    String lockStr = Md5Crypt.md5Crypt(syncConfig.toString().getBytes(), null, "");
                    RLock lock = redisson.getLock(lockStr);
                    try {
                        if (lock.tryLock()) {
                            binaryLogClientFactory.getClient(syncConfig).connect();
                        }
                    } catch (BinlogException | IOException e) {
                        log.error(e.getMessage(), e);
                    } finally {
                        lock.unlock();
                    }
                });
            }
        }, 0, 1000);
    }

    public Boolean canBuild(SyncConfig syncConfig) {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        config.setLockWatchdogTimeout(10000L);
        RedissonClient redisson = Redisson.create(config);
        RLock lock = redisson.getLock("myLock");
        lock.lock();

        return null;
    }

    public static void main(String[] args) {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        config.setLockWatchdogTimeout(10000L);
        RedissonClient redisson = Redisson.create(config);
        RLock lock = redisson.getLock("myLock");
        lock.lock();
    }
}

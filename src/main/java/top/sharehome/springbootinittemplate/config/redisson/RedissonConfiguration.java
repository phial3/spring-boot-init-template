package top.sharehome.springbootinittemplate.config.redisson;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.ReadMode;
import org.redisson.config.SubscriptionMode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.ConditionalOnRepositoryType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * Redisson配置
 *
 * @author AntonyCheng
 */
@Configuration
@EnableConfigurationProperties(RedissonProperties.class)
@AllArgsConstructor
@Slf4j
@ConditionalOnProperty(value = {
        "redisson.singleServerConfig.enableSingle",
        "redisson.clusterServersConfig.enableCluster"
}, havingValue = "true")
public class RedissonConfiguration {

    private final RedissonProperties redissonProperties;

    private final ObjectMapper objectMapper;

    @Bean
    @ConditionalOnProperty(prefix = "redisson.singleServerConfig", name = "enableSingle", havingValue = "true")
    public RedissonClient singleClient() {
        Config config = new Config();
        config.setThreads(redissonProperties.getThreads())
                .setNettyThreads(redissonProperties.getNettyThreads())
                .setCodec(new JsonJacksonCodec(objectMapper));
        RedissonProperties.SingleServerConfig singleServerConfig = redissonProperties.getSingleServerConfig();
        if (ObjectUtil.isNotNull(singleServerConfig)) {
            // 使用单机模式
            config.useSingleServer()
                    .setAddress(singleServerConfig.getAddress())
                    .setDatabase(singleServerConfig.getDatabase())
                    .setPassword(singleServerConfig.getPassword())
                    .setTimeout(singleServerConfig.getTimeout())
                    .setIdleConnectionTimeout(singleServerConfig.getIdleConnectionTimeout())
                    .setSubscriptionConnectionPoolSize(singleServerConfig.getSubscriptionConnectionPoolSize())
                    .setConnectionMinimumIdleSize(singleServerConfig.getConnectionMinimumIdleSize())
                    .setConnectionPoolSize(singleServerConfig.getConnectionPoolSize());
        }
        RedissonClient redissonClient = Redisson.create(config);
        log.info("redisson配置成功，服务器地址：{}", singleServerConfig.getAddress());
        return redissonClient;
    }

    @Bean(autowireCandidate = false)
    @ConditionalOnProperty(prefix = "redisson.clusterServersConfig", name = "enableCluster", havingValue = "true")
    public RedissonClient clusterClient() {
        Config config = new Config();
        config.setThreads(redissonProperties.getThreads())
                .setNettyThreads(redissonProperties.getNettyThreads())
                .setCodec(new JsonJacksonCodec(objectMapper));
        RedissonProperties.ClusterServersConfig clusterServersConfig = redissonProperties.getClusterServersConfig();
        if (ObjectUtil.isNotNull(clusterServersConfig)) {
            // 使用集群模式
            config.useClusterServers()
                    .setPassword(clusterServersConfig.getPassword())
                    .setMasterConnectionMinimumIdleSize(clusterServersConfig.getMasterConnectionMinimumIdleSize())
                    .setMasterConnectionPoolSize(clusterServersConfig.getMasterConnectionPoolSize())
                    .setSlaveConnectionMinimumIdleSize(clusterServersConfig.getSlaveConnectionMinimumIdleSize())
                    .setSlaveConnectionPoolSize(clusterServersConfig.getSlaveConnectionPoolSize())
                    .setIdleConnectionTimeout(clusterServersConfig.getIdleConnectionTimeout())
                    .setTimeout(clusterServersConfig.getTimeout())
                    .setSubscriptionConnectionPoolSize(clusterServersConfig.getSubscriptionConnectionPoolSize())
                    .setReadMode(ReadMode.SLAVE)
                    .setSubscriptionMode(SubscriptionMode.MASTER)
                    .setNodeAddresses(clusterServersConfig.getNodeAddresses());
        }
        RedissonClient redissonClient = Redisson.create(config);
        log.info("redisson配置成功，服务器集群地址：{}", clusterServersConfig.getNodeAddresses());
        return redissonClient;
    }
}
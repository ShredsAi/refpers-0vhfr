package ai.shreds.infrastructure.config;

import ai.shreds.shared.value_objects.SharedValueDuration;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@Getter
public class InfrastructureRedisConfig {

    @Value("${spring.redis.host:localhost}")
    private String host;

    @Value("${spring.redis.port:6379}")
    private Integer port;

    @Value("${spring.redis.password:}")
    private String password;

    @Value("${spring.redis.database:0}")
    private Integer database;

    @Value("${spring.redis.timeout.connection:2000}")
    private Long connectionTimeoutMillis;

    @Value("${spring.redis.timeout.command:1000}")
    private Long commandTimeoutMillis;

    @Value("${spring.redis.pool-size:10}")
    private Integer poolSize;

    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(poolSize);
        poolConfig.setMaxIdle(poolSize);
        poolConfig.setMinIdle(2);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        return poolConfig;
    }

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        if (!password.isEmpty()) {
            config.setPassword(password);
        }
        config.setDatabase(database);
        
        JedisConnectionFactory factory = new JedisConnectionFactory(config);
        factory.setPoolConfig(jedisPoolConfig());
        return factory;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        StringRedisSerializer serializer = new StringRedisSerializer();
        template.setKeySerializer(serializer);
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(serializer);
        template.setHashValueSerializer(serializer);
        template.setEnableTransactionSupport(true);
        return template;
    }
    
    @Bean
    public StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setEnableTransactionSupport(true);
        return template;
    }

    public SharedValueDuration getConnectionTimeout() {
        return SharedValueDuration.fromMillis(connectionTimeoutMillis);
    }

    public SharedValueDuration getCommandTimeout() {
        return SharedValueDuration.fromMillis(commandTimeoutMillis);
    }
}
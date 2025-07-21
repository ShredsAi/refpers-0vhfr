package ai.shreds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableRedisRepositories
@EnableTransactionManagement
@EnableAsync
@EnableAspectJAutoProxy
public class AuthenticationShredApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthenticationShredApplication.class, args);
    }
}

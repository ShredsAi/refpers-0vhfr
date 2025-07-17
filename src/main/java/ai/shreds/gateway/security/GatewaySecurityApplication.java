package ai.shreds.gateway.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;

@SpringBootApplication
@EnableWebFluxSecurity
@ComponentScan(basePackages = "ai.shreds")
public class GatewaySecurityApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewaySecurityApplication.class, args);
    }
}

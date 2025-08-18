package ai.shreds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaRepositories
@EnableKafka
@EnableTransactionManagement
public class FinancialPaymentShredApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancialPaymentShredApplication.class, args);
    }
}

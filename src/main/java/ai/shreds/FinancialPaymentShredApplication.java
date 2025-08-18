package ai.shreds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableKafka
@EnableTransactionManagement
@EnableCaching
@EnableAsync
public class FinancialPaymentShredApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancialPaymentShredApplication.class, args);
    }
}

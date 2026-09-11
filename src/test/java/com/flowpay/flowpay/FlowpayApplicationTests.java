package com.flowpay.flowpay;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:flowpay_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "flowpay.webhook.secret=test-webhook-secret",
        "ollama.base-url=http://localhost:11434",
        "ollama.model=llama3"
})
class FlowpayApplicationTests {

    @Test
    void contextLoads() {
    }
}
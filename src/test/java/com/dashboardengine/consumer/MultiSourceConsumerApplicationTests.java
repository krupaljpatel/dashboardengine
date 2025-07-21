package com.dashboardengine.consumer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "app.leadership.enabled=false"
})
class MultiSourceConsumerApplicationTests {

    @Test
    void contextLoads() {
        // Test that Spring context loads successfully
    }
}
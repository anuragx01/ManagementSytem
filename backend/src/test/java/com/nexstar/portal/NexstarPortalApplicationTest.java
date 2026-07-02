package com.nexstar.portal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class NexstarPortalApplicationTest {

    @Test
    void contextLoads() {
        // Just passes if Spring context starts without errors
    }
}

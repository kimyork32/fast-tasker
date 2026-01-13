package com.fasttasker.notification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@SpringBootTest
class NotificationServiceApplicationTests {

    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;

	@Test
	void contextLoads() {
	}

}

package com.fasttasker.notification;

import com.fasttasker.notification.application.service.NotificationService;
import com.fasttasker.notification.domain.INotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
@ActiveProfiles("test")
class NotificationServiceApplicationTests {

    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @MockBean
    private INotificationRepository notificationRepository;

    @MockBean
    private NotificationService notificationService;

	@Test
	void contextLoads() {
	}

}

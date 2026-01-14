package com.fasttasker.fast_tasker;

import com.fasttasker.fast_tasker.application.service.AccountService;
import com.fasttasker.fast_tasker.application.service.ConversationService;
import com.fasttasker.fast_tasker.application.service.TaskService;
import com.fasttasker.fast_tasker.application.service.TaskerService;
import com.fasttasker.fast_tasker.domain.account.IAccountRepository;
import com.fasttasker.fast_tasker.domain.conversation.IConversationRepository;
import com.fasttasker.fast_tasker.domain.task.ITaskRepository;
import com.fasttasker.fast_tasker.domain.tasker.ITaskerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
@ActiveProfiles("test")
class FastTaskerApplicationTests {

    @MockBean
    private TaskerService taskerService;

    @MockBean
    private TaskService taskService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private ConversationService conversationService;

    @MockBean
    private IAccountRepository accountRepository;

    @MockBean
    private ITaskerRepository taskerRepository;

    @MockBean
    private ITaskRepository taskRepository;

    @MockBean
    private IConversationRepository conversationRepository;

	@Test
	void contextLoads() {
	}

}

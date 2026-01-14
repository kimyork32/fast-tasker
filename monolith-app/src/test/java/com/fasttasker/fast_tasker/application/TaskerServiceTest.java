package com.fasttasker.fast_tasker.application;

import com.fasttasker.fast_tasker.application.dto.tasker.LocationRequest;
import com.fasttasker.fast_tasker.application.dto.tasker.ProfileRequest;
import com.fasttasker.fast_tasker.application.dto.tasker.TaskerRequest;
import com.fasttasker.fast_tasker.application.dto.tasker.TaskerResponse;
import com.fasttasker.fast_tasker.application.dto.tasker.ProfileResponse;
import com.fasttasker.fast_tasker.application.mapper.TaskerMapper;
import com.fasttasker.fast_tasker.application.service.ConversationService;
import com.fasttasker.fast_tasker.application.service.TaskerService;
import com.fasttasker.fast_tasker.domain.task.ITaskRepository;
import com.fasttasker.fast_tasker.domain.tasker.ITaskerRepository;
import com.fasttasker.fast_tasker.domain.tasker.Location;
import com.fasttasker.fast_tasker.domain.tasker.Profile;
import com.fasttasker.fast_tasker.domain.tasker.Tasker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for TaskerService
 */
@ExtendWith(MockitoExtension.class)
class TaskerServiceTest {

    @Mock
    private ITaskerRepository taskerRepository;

    @Mock
    private ITaskRepository taskRepository;

    @Mock
    private TaskerMapper taskerMapper;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ConversationService conversationService;

    @InjectMocks
    private TaskerService taskerService;

    private UUID testAccountId;
    private Tasker tasker;

    @BeforeEach
    void setUp() {

        testAccountId = UUID.randomUUID();
        var defaultLocation = new Location(
                0,
                0,
                "",
                "0"
        );

        var defaultProfile = new Profile(
                "Default",
                "User",
                "",
                defaultLocation,
                ""
        );

        tasker = new Tasker(
                UUID.randomUUID(),
                testAccountId,
                defaultProfile
        );
    }

    @Test
    void shouldRegisterTaskerSuccess() {
        // 1. GIVEN
        // input DTO

        var locationRequest = new LocationRequest(
                -13.412453,
                -12.158023,
                "address street",
                "4141414"
        );

        var profileRequest = new ProfileRequest(
                "homer",
                "simpson",
                "https://aws-service-s3.com/photo/141454",
                "about me",
                locationRequest
        );

        var taskerRequest = new TaskerRequest(
                testAccountId.toString(),
                profileRequest
        );

        // Mocking Repository behavior
        when(taskerRepository.findByAccountId(testAccountId)).thenReturn(tasker);

        // Mocking Mapper behavior
        // 1. toProfileEntity
        var newLocation = new Location(locationRequest.latitude(), locationRequest.longitude(), locationRequest.address(), locationRequest.zip());
        var newProfile = new Profile(profileRequest.firstName(), profileRequest.lastName(), profileRequest.photo(), newLocation, profileRequest.about());
        when(taskerMapper.toProfileEntity(any())).thenReturn(newProfile);

        // 2. toResponse
        TaskerResponse mockResponse = mock(TaskerResponse.class);
        ProfileResponse mockProfileResponse = mock(ProfileResponse.class);
        when(taskerMapper.toResponse(any())).thenReturn(mockResponse);
        when(mockResponse.profile()).thenReturn(mockProfileResponse);
        when(mockProfileResponse.about()).thenReturn(profileRequest.about());
        when(mockProfileResponse.photo()).thenReturn(profileRequest.photo());

        // 2.  WHEN
        TaskerResponse response = taskerService.registerTasker(taskerRequest);

        // 2. THEN
        // verify the DTO request
        assertThat(response).isNotNull();
        assertThat(response.profile().about()).isEqualTo(profileRequest.about());
        assertThat(response.profile().photo()).isEqualTo(profileRequest.photo());

        // verify that save was called
        verify(taskerRepository).save(tasker);

        // verify that the object state was updated
        assertThat(tasker.getProfile().getAbout()).isEqualTo(profileRequest.about());
    }
}
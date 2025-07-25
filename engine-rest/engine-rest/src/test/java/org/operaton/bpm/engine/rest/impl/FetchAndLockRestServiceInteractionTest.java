/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.operaton.bpm.engine.rest.impl;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.operaton.bpm.engine.ExternalTaskService;
import org.operaton.bpm.engine.IdentityService;
import org.operaton.bpm.engine.ProcessEngineException;
import org.operaton.bpm.engine.externaltask.ExternalTaskQueryTopicBuilder;
import org.operaton.bpm.engine.externaltask.FetchAndLockBuilder;
import org.operaton.bpm.engine.externaltask.LockedExternalTask;
import org.operaton.bpm.engine.identity.Group;
import org.operaton.bpm.engine.identity.Tenant;
import org.operaton.bpm.engine.impl.identity.Authentication;
import org.operaton.bpm.engine.rest.AbstractRestServiceTest;
import org.operaton.bpm.engine.rest.dto.externaltask.FetchExternalTasksDto.FetchExternalTaskTopicDto;
import org.operaton.bpm.engine.rest.dto.externaltask.FetchExternalTasksExtendedDto;
import org.operaton.bpm.engine.rest.exception.InvalidRequestException;
import org.operaton.bpm.engine.rest.helper.MockProvider;
import org.operaton.bpm.engine.rest.util.container.TestContainerExtension;

import io.restassured.http.ContentType;
import jakarta.servlet.ServletContextEvent;
import jakarta.ws.rs.core.Response.Status;

/**
 * @author Tassilo Weidner
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class FetchAndLockRestServiceInteractionTest extends AbstractRestServiceTest {

  @RegisterExtension
  public static TestContainerExtension rule = new TestContainerExtension();

  private static final String FETCH_EXTERNAL_TASK_URL =  "/rest-test/external-task/fetchAndLock";

  @Mock
  private ExternalTaskService externalTaskService;

  @Mock
  private ExternalTaskQueryTopicBuilder fetchTopicBuilder;

  @Mock
  private FetchAndLockBuilder fetchAndLockBuilder;

  @Mock
  private IdentityService identityServiceMock;

  private LockedExternalTask lockedExternalTaskMock;

  private List<String> groupIds;
  private List<String> tenantIds;

  @BeforeEach
  void setUpRuntimeData() {
    when(processEngine.getExternalTaskService()).thenReturn(externalTaskService);

    lockedExternalTaskMock = MockProvider.createMockLockedExternalTask();

    when(externalTaskService.fetchAndLock(anyInt(), any(String.class), any(Boolean.class)))
      .thenReturn(fetchTopicBuilder);

    when(externalTaskService.fetchAndLock()).thenReturn(fetchAndLockBuilder);

    // fetch and lock builder
    when(fetchAndLockBuilder.workerId(anyString())).thenReturn(fetchAndLockBuilder);
    when(fetchAndLockBuilder.maxTasks(anyInt())).thenReturn(fetchAndLockBuilder);
    when(fetchAndLockBuilder.usePriority(anyBoolean())).thenReturn(fetchAndLockBuilder);
    when(fetchAndLockBuilder.orderByCreateTime()).thenReturn(fetchAndLockBuilder);
    when(fetchAndLockBuilder.asc()).thenReturn(fetchAndLockBuilder);
    when(fetchAndLockBuilder.desc()).thenReturn(fetchAndLockBuilder);

    when(fetchAndLockBuilder.subscribe()).thenReturn(fetchTopicBuilder);

    when(fetchTopicBuilder.topic(anyString(), anyLong())).thenReturn(fetchTopicBuilder);
    when(fetchTopicBuilder.variables(anyList())).thenReturn(fetchTopicBuilder);
    when(fetchTopicBuilder.enableCustomObjectDeserialization()).thenReturn(fetchTopicBuilder);
    when(fetchTopicBuilder.processDefinitionVersionTag(anyString())).thenReturn(fetchTopicBuilder);

    // for authentication
    when(processEngine.getIdentityService()).thenReturn(identityServiceMock);

    List<Group> groupMocks = MockProvider.createMockGroups();
    groupIds = groupMocks.stream().map(Group::getId).toList();

    List<Tenant> tenantMocks = Collections.singletonList(MockProvider.createMockTenant());
    tenantIds = tenantMocks.stream().map(Tenant::getId).toList();

    new FetchAndLockContextListener().contextInitialized(mock(ServletContextEvent.class, RETURNS_DEEP_STUBS));
  }

  @Test
  void shouldFetchAndLock() {
    when(fetchTopicBuilder.execute()).thenReturn(new ArrayList<>(Collections.singleton(lockedExternalTaskMock)));
    FetchExternalTasksExtendedDto fetchExternalTasksDto = createDto(null, true, true, false);

    given()
      .contentType(ContentType.JSON)
      .body(fetchExternalTasksDto)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("[0].id", equalTo(MockProvider.EXTERNAL_TASK_ID))
      .body("[0].topicName", equalTo(MockProvider.EXTERNAL_TASK_TOPIC_NAME))
      .body("[0].workerId", equalTo(MockProvider.EXTERNAL_TASK_WORKER_ID))
      .body("[0].lockExpirationTime", equalTo(MockProvider.EXTERNAL_TASK_LOCK_EXPIRATION_TIME))
      .body("[0].processInstanceId", equalTo(MockProvider.EXAMPLE_PROCESS_INSTANCE_ID))
      .body("[0].executionId", equalTo(MockProvider.EXAMPLE_EXECUTION_ID))
      .body("[0].activityId", equalTo(MockProvider.EXAMPLE_ACTIVITY_ID))
      .body("[0].activityInstanceId", equalTo(MockProvider.EXAMPLE_ACTIVITY_INSTANCE_ID))
      .body("[0].processDefinitionId", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_ID))
      .body("[0].processDefinitionKey", equalTo(MockProvider.EXAMPLE_PROCESS_DEFINITION_KEY))
      .body("[0].tenantId", equalTo(MockProvider.EXAMPLE_TENANT_ID))
      .body("[0].retries", equalTo(MockProvider.EXTERNAL_TASK_RETRIES))
      .body("[0].errorMessage", equalTo(MockProvider.EXTERNAL_TASK_ERROR_MESSAGE))
      .body("[0].errorMessage", equalTo(MockProvider.EXTERNAL_TASK_ERROR_MESSAGE))
      .body("[0].priority", equalTo(MockProvider.EXTERNAL_TASK_PRIORITY))
      .body("[0].variables." + MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME, notNullValue())
      .body("[0].variables." + MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME + ".value", equalTo(MockProvider.EXAMPLE_PRIMITIVE_VARIABLE_VALUE.getValue()))
      .body("[0].variables." + MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME + ".type", equalTo("String"))
    .when().post(FETCH_EXTERNAL_TASK_URL);

    InOrder inOrder = inOrder(fetchAndLockBuilder, fetchTopicBuilder, externalTaskService);

    inOrder.verify(externalTaskService, times(1)).fetchAndLock();

    inOrder.verify(fetchAndLockBuilder).workerId("aWorkerId");
    inOrder.verify(fetchAndLockBuilder).maxTasks(5);
    inOrder.verify(fetchAndLockBuilder).usePriority(true);

    inOrder.verify(fetchAndLockBuilder).subscribe();

    inOrder.verify(fetchTopicBuilder).topic("aTopicName", 12354L);
    inOrder.verify(fetchTopicBuilder).variables(Collections.singletonList(MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME));
    inOrder.verify(fetchTopicBuilder).execute();

    verifyNoMoreInteractions(fetchAndLockBuilder, externalTaskService);
  }

  @Test
  void shouldFetchWithoutVariables() {
    when(fetchTopicBuilder.execute()).thenReturn(new ArrayList<>(Collections.singleton(lockedExternalTaskMock)));
    FetchExternalTasksExtendedDto fetchExternalTasksDto = createDto(null);

    given()
      .contentType(ContentType.JSON)
      .body(fetchExternalTasksDto)
    .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
      .body("[0].id", equalTo(MockProvider.EXTERNAL_TASK_ID))
    .when()
      .post(FETCH_EXTERNAL_TASK_URL);

    InOrder inOrder = inOrder(fetchAndLockBuilder, fetchTopicBuilder, externalTaskService);

    inOrder.verify(externalTaskService, times(1)).fetchAndLock();

    inOrder.verify(fetchAndLockBuilder).workerId("aWorkerId");
    inOrder.verify(fetchAndLockBuilder).maxTasks(5);
    inOrder.verify(fetchAndLockBuilder).usePriority(false);
    inOrder.verify(fetchAndLockBuilder).subscribe();

    inOrder.verify(fetchTopicBuilder).topic("aTopicName", 12354L);
    inOrder.verify(fetchTopicBuilder).execute();

    verifyNoMoreInteractions(fetchAndLockBuilder, externalTaskService);
  }

  @Test
  void shouldFetchWithCustomObjectDeserializationEnabled() {
    when(fetchTopicBuilder.execute())
      .thenReturn(new ArrayList<>(Collections.singleton(lockedExternalTaskMock)));
    FetchExternalTasksExtendedDto fetchExternalTasksDto = createDto(null, false, true, true);

    given()
      .contentType(ContentType.JSON)
      .body(fetchExternalTasksDto)
    .then()
      .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(FETCH_EXTERNAL_TASK_URL);

    InOrder inOrder = inOrder(fetchAndLockBuilder, fetchTopicBuilder, externalTaskService);

    inOrder.verify(externalTaskService).fetchAndLock();

    inOrder.verify(fetchAndLockBuilder).workerId("aWorkerId");
    inOrder.verify(fetchAndLockBuilder).maxTasks(5);
    inOrder.verify(fetchAndLockBuilder).usePriority(false);

    inOrder.verify(fetchAndLockBuilder).subscribe();

    inOrder.verify(fetchTopicBuilder).topic("aTopicName", 12354L);
    inOrder.verify(fetchTopicBuilder).variables(Collections.singletonList(MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME));
    inOrder.verify(fetchTopicBuilder).enableCustomObjectDeserialization();
    inOrder.verify(fetchTopicBuilder).execute();

    verifyNoMoreInteractions(fetchAndLockBuilder, fetchTopicBuilder, externalTaskService);
  }

  @Test
  void shouldThrowInvalidRequestExceptionOnMaxTimeoutExceeded() {
    FetchExternalTasksExtendedDto fetchExternalTasksDto = createDto(FetchAndLockHandlerImpl.MAX_REQUEST_TIMEOUT + 1);

    given()
      .contentType(ContentType.JSON)
      .body(fetchExternalTasksDto)
    .then()
      .expect()
        .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
        .body("message", containsString("The asynchronous response timeout cannot be set to a value greater than "))
        .statusCode(Status.BAD_REQUEST.getStatusCode())
    .when()
      .post(FETCH_EXTERNAL_TASK_URL);
  }

  @Test
  void shouldThrowProcessEngineExceptionDuringTimeout() {
    FetchExternalTasksExtendedDto fetchExternalTasksDto = createDto(500L);

    when(fetchTopicBuilder.execute())
      .thenReturn(Collections.<LockedExternalTask>emptyList())
      .thenReturn(Collections.<LockedExternalTask>emptyList())
      .thenThrow(new ProcessEngineException("anExceptionMessage"));

    given()
      .contentType(ContentType.JSON)
      .body(fetchExternalTasksDto)
    .then()
      .expect()
        .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
        .body("message", equalTo("anExceptionMessage"))
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
    .when()
      .post(FETCH_EXTERNAL_TASK_URL);

    verify(fetchTopicBuilder, atLeastOnce()).execute();
  }

  @Test
  void shouldThrowProcessEngineExceptionNotDuringTimeout() {
    FetchExternalTasksExtendedDto fetchExternalTasksDto = createDto(500L);

    when(fetchTopicBuilder.execute())
      .thenThrow(new ProcessEngineException("anExceptionMessage"));

    given()
      .contentType(ContentType.JSON)
      .body(fetchExternalTasksDto)
    .then()
      .expect()
        .body("type", equalTo(ProcessEngineException.class.getSimpleName()))
        .body("message", equalTo("anExceptionMessage"))
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
    .when()
      .post(FETCH_EXTERNAL_TASK_URL);

    verify(fetchTopicBuilder, times(1)).execute();
  }

  @Test
  void shouldResponseImmediatelyDueToAvailableTasks() {
    when(fetchTopicBuilder.execute())
      .thenReturn(new ArrayList<>(Collections.singleton(lockedExternalTaskMock)));

    FetchExternalTasksExtendedDto fetchExternalTasksDto = createDto(500L);

    given()
      .contentType(ContentType.JSON)
      .body(fetchExternalTasksDto)
    .then()
      .expect()
        .body("size()", is(1))
        .statusCode(Status.OK.getStatusCode())
    .when()
      .post(FETCH_EXTERNAL_TASK_URL);
  }

  @Test
  void shouldSetAuthenticationProperly() {
    when(identityServiceMock.getCurrentAuthentication())
      .thenReturn(new Authentication(MockProvider.EXAMPLE_USER_ID, groupIds, tenantIds));

    FetchExternalTasksExtendedDto fetchExternalTasksDto = createDto(500L);

    given()
      .contentType(ContentType.JSON)
      .body(fetchExternalTasksDto)
    .when()
      .post(FETCH_EXTERNAL_TASK_URL);

    ArgumentCaptor<Authentication> argumentCaptor = ArgumentCaptor.forClass(Authentication.class);
    verify(identityServiceMock, atLeastOnce()).setAuthentication(argumentCaptor.capture());

    assertThat(argumentCaptor.getValue().getUserId()).isEqualTo(MockProvider.EXAMPLE_USER_ID);
    assertThat(argumentCaptor.getValue().getGroupIds()).isEqualTo(groupIds);
    assertThat(argumentCaptor.getValue().getTenantIds()).isEqualTo(tenantIds);
  }

  @Test
  void shouldReturnInternalServerErrorResponseJsonWithTypeAndMessage() {
    FetchExternalTasksExtendedDto fetchExternalTasksDto = createDto(500L);

    when(fetchTopicBuilder.execute())
      .thenThrow(new IllegalArgumentException("anExceptionMessage"));

    given()
      .contentType(ContentType.JSON)
      .body(fetchExternalTasksDto)
    .then()
      .expect()
        .body("type", equalTo(IllegalArgumentException.class.getSimpleName()))
        .body("message", equalTo("anExceptionMessage"))
        .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
    .when()
      .post(FETCH_EXTERNAL_TASK_URL);

    verify(fetchTopicBuilder, times(1)).execute();
  }

  @Test
  void shouldFetchAndLockByProcessDefinitionVersionTag() {
    when(fetchTopicBuilder.execute())
    .thenReturn(new ArrayList<LockedExternalTask>(Collections.singleton(lockedExternalTaskMock)));

    FetchExternalTasksExtendedDto fetchExternalTasksDto = createDto(500L);
    for (FetchExternalTaskTopicDto topic : fetchExternalTasksDto.getTopics()) {
      topic.setProcessDefinitionVersionTag("version");
    }

  given()
    .contentType(ContentType.JSON)
    .body(fetchExternalTasksDto)
  .then()
    .expect()
    .statusCode(Status.OK.getStatusCode())
  .when()
    .post(FETCH_EXTERNAL_TASK_URL);

    verify(fetchTopicBuilder).processDefinitionVersionTag("version");
  }

  // helper /////////////////////////

  private FetchExternalTasksExtendedDto createDto(Long responseTimeout) {
    return createDto(responseTimeout, false, false, false);
  }

  private FetchExternalTasksExtendedDto createDto(Long responseTimeout, boolean usePriority, boolean withVariables, boolean withDeserialization) {
    FetchExternalTasksExtendedDto fetchExternalTasksDto = new FetchExternalTasksExtendedDto();
    if (responseTimeout != null) {
      fetchExternalTasksDto.setAsyncResponseTimeout(responseTimeout);
    }
    fetchExternalTasksDto.setMaxTasks(5);
    fetchExternalTasksDto.setWorkerId("aWorkerId");
    fetchExternalTasksDto.setUsePriority(usePriority);
    FetchExternalTasksExtendedDto.FetchExternalTaskTopicDto topicDto = new FetchExternalTasksExtendedDto.FetchExternalTaskTopicDto();
    fetchExternalTasksDto.setTopics(Collections.singletonList(topicDto));
    topicDto.setTopicName("aTopicName");
    topicDto.setLockDuration(12354L);
    if (withVariables) {
      topicDto.setVariables(Collections.singletonList(MockProvider.EXAMPLE_VARIABLE_INSTANCE_NAME));
    }
    topicDto.setDeserializeValues(withDeserialization);
    fetchExternalTasksDto.setTopics(Collections.singletonList(topicDto));
    return fetchExternalTasksDto;
  }

}

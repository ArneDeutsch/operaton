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
package org.operaton.bpm.engine.rest.history;

import static io.restassured.RestAssured.expect;
import static io.restassured.RestAssured.given;
import static io.restassured.path.json.JsonPath.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

import org.operaton.bpm.engine.history.HistoricCaseInstance;
import org.operaton.bpm.engine.history.HistoricCaseInstanceQuery;
import org.operaton.bpm.engine.impl.calendar.DateTimeUtil;
import org.operaton.bpm.engine.rest.AbstractRestServiceTest;
import org.operaton.bpm.engine.rest.exception.InvalidRequestException;
import org.operaton.bpm.engine.rest.helper.MockProvider;
import org.operaton.bpm.engine.rest.helper.variable.EqualsPrimitiveValue;
import org.operaton.bpm.engine.rest.util.OrderingBuilder;
import org.operaton.bpm.engine.rest.util.container.TestContainerExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class HistoricCaseInstanceRestServiceQueryTest extends AbstractRestServiceTest {

  @RegisterExtension
  public static TestContainerExtension rule = new TestContainerExtension();

  protected static final String HISTORIC_CASE_INSTANCE_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/history/case-instance";
  protected static final String HISTORIC_CASE_INSTANCE_COUNT_RESOURCE_URL = HISTORIC_CASE_INSTANCE_RESOURCE_URL + "/count";

  protected HistoricCaseInstanceQuery mockedQuery;

  @BeforeEach
  void setUpRuntimeData() {
    mockedQuery = setUpMockHistoricCaseInstanceQuery(MockProvider.createMockHistoricCaseInstances());
  }

  protected HistoricCaseInstanceQuery setUpMockHistoricCaseInstanceQuery(List<HistoricCaseInstance> mockedHistoricCaseInstances) {
    HistoricCaseInstanceQuery mockedHistoricCaseInstanceQuery = mock(HistoricCaseInstanceQuery.class);
    when(mockedHistoricCaseInstanceQuery.list()).thenReturn(mockedHistoricCaseInstances);
    when(mockedHistoricCaseInstanceQuery.count()).thenReturn((long) mockedHistoricCaseInstances.size());

    when(processEngine.getHistoryService().createHistoricCaseInstanceQuery()).thenReturn(mockedHistoricCaseInstanceQuery);

    return mockedHistoricCaseInstanceQuery;
  }

  @Test
  void testEmptyQuery() {
    String queryKey = "";
    given()
      .queryParam("caseDefinitionKey", queryKey)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);
  }

  @Test
  void testNoParametersQuery() {
    expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  void testNoParametersQueryAsPost() {
    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).list();
    verifyNoMoreInteractions(mockedQuery);
  }

  @Test
  void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("definitionId", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  @Test
  void testSortByParameterOnly() {
    given()
      .queryParam("sortBy", "definitionId")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Only a single sorting parameter specified. sortBy and sortOrder required"))
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);
  }

  @Test
  void testSortOrderParameterOnly() {
    given()
      .queryParam("sortOrder", "asc")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Only a single sorting parameter specified. sortBy and sortOrder required"))
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);
  }

  @Test
  void testSortingParameters() {
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("instanceId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseInstanceId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("instanceId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseInstanceId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("definitionId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseDefinitionId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("definitionId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseDefinitionId();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("businessKey", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseInstanceBusinessKey();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("businessKey", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseInstanceBusinessKey();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("createTime", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseInstanceCreateTime();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("createTime", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseInstanceCreateTime();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("closeTime", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseInstanceCloseTime();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("closeTime", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseInstanceCloseTime();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("duration", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseInstanceDuration();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("duration", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByCaseInstanceDuration();
    inOrder.verify(mockedQuery).desc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("tenantId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTenantId();
    inOrder.verify(mockedQuery).asc();

    inOrder = Mockito.inOrder(mockedQuery);
    executeAndVerifySorting("tenantId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByTenantId();
    inOrder.verify(mockedQuery).desc();
  }

  @Test
  void testSecondarySortingAsPost() {
    InOrder inOrder = Mockito.inOrder(mockedQuery);
    Map<String, Object> json = new HashMap<>();
    json.put("sorting", OrderingBuilder.create()
      .orderBy("businessKey").desc()
      .orderBy("closeTime").asc()
      .getJson());
    given().contentType(POST_JSON_CONTENT_TYPE).body(json)
      .header("accept", MediaType.APPLICATION_JSON)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    inOrder.verify(mockedQuery).orderByCaseInstanceBusinessKey();
    inOrder.verify(mockedQuery).desc();
    inOrder.verify(mockedQuery).orderByCaseInstanceCloseTime();
    inOrder.verify(mockedQuery).asc();
  }

  @Test
  void testSuccessfulPagination() {
    int firstResult = 0;
    int maxResults = 10;

    given()
      .queryParam("firstResult", firstResult)
      .queryParam("maxResults", maxResults)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).listPage(firstResult, maxResults);
  }

  @Test
  void testMissingFirstResultParameter() {
    int maxResults = 10;

    given()
      .queryParam("maxResults", maxResults)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).listPage(0, maxResults);
  }

  @Test
  void testMissingMaxResultsParameter() {
    int firstResult = 10;

    given()
      .queryParam("firstResult", firstResult)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).listPage(firstResult, Integer.MAX_VALUE);
  }

  @Test
  void testQueryCount() {
    expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .get(HISTORIC_CASE_INSTANCE_COUNT_RESOURCE_URL);

    verify(mockedQuery).count();
  }

  @Test
  void testQueryCountForPost() {
    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(EMPTY_JSON_OBJECT)
    .then().expect()
      .body("count", equalTo(1))
    .when()
      .post(HISTORIC_CASE_INSTANCE_COUNT_RESOURCE_URL);

    verify(mockedQuery).count();
  }

  @Test
  void testSimpleHistoricCaseQuery() {
    String caseInstanceId = MockProvider.EXAMPLE_CASE_INSTANCE_ID;

    Response response = given()
        .queryParam("caseInstanceId", caseInstanceId)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).caseInstanceId(caseInstanceId);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<Map<String, Object>> instances = from(content).getList("");
    Assertions.assertEquals(1, instances.size());
    assertThat(instances.get(0)).isNotNull();

    String returnedCaseInstanceId = from(content).getString("[0].id");
    String returnedCaseInstanceBusinessKey = from(content).getString("[0].businessKey");
    String returnedCaseDefinitionId = from(content).getString("[0].caseDefinitionId");
    String returnedCaseDefinitionKey = from(content).getString("[0].caseDefinitionKey");
    String returnedCaseDefinitionName = from(content).getString("[0].caseDefinitionName");
    String returnedCreateTime = from(content).getString("[0].createTime");
    String returnedCloseTime = from(content).getString("[0].closeTime");
    long returnedDurationInMillis = from(content).getLong("[0].durationInMillis");
    String returnedCreateUserId = from(content).getString("[0].createUserId");
    String returnedSuperCaseInstanceId = from(content).getString("[0].superCaseInstanceId");
    String returnedSuperProcessInstanceId = from(content).getString("[0].superProcessInstanceId");
    String returnedTenantId = from(content).getString("[0].tenantId");
    boolean active = from(content).getBoolean("[0].active");
    boolean completed = from(content).getBoolean("[0].completed");
    boolean terminated = from(content).getBoolean("[0].terminated");
    boolean closed = from(content).getBoolean("[0].closed");

    Assertions.assertEquals(MockProvider.EXAMPLE_CASE_INSTANCE_ID, returnedCaseInstanceId);
    Assertions.assertEquals(MockProvider.EXAMPLE_CASE_INSTANCE_BUSINESS_KEY, returnedCaseInstanceBusinessKey);
    Assertions.assertEquals(MockProvider.EXAMPLE_CASE_DEFINITION_ID, returnedCaseDefinitionId);
    Assertions.assertEquals(MockProvider.EXAMPLE_CASE_DEFINITION_KEY, returnedCaseDefinitionKey);
    Assertions.assertEquals(MockProvider.EXAMPLE_CASE_DEFINITION_NAME, returnedCaseDefinitionName);
    Assertions.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CREATE_TIME, returnedCreateTime);
    Assertions.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CLOSE_TIME, returnedCloseTime);
    Assertions.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_DURATION_MILLIS, returnedDurationInMillis);
    Assertions.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CREATE_USER_ID, returnedCreateUserId);
    Assertions.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_SUPER_CASE_INSTANCE_ID, returnedSuperCaseInstanceId);
    Assertions.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_SUPER_PROCESS_INSTANCE_ID, returnedSuperProcessInstanceId);
    Assertions.assertEquals(MockProvider.EXAMPLE_TENANT_ID, returnedTenantId);
    Assertions.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_IS_ACTIVE, active);
    Assertions.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_IS_COMPLETED, completed);
    Assertions.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_IS_TERMINATED, terminated);
    Assertions.assertEquals(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_IS_CLOSED, closed);
  }

  @Test
  void testAdditionalParametersExcludingCases() {
    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();

    given()
      .queryParams(stringQueryParameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verifyStringParameterQueryInvocations();
  }

  @Test
  void testAdditionalParametersExcludingCasesAsPost() {
    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(stringQueryParameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verifyStringParameterQueryInvocations();
  }

  @Test
  void testHistoricBeforeAndAfterCreateTimeQuery() {
    given()
      .queryParam("createdBefore", MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CREATED_BEFORE)
      .queryParam("createdAfter", MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CREATED_AFTER)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verifyCreateParameterQueryInvocations();
  }

  @Test
  void testHistoricBeforeAndAfterCreateTimeQueryAsPost() {
    Map<String, Date> parameters = getCompleteCreateDateQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verifyCreateParameterQueryInvocations();
  }

  @Test
  void testHistoricBeforeAndAfterCreateTimeAsStringQueryAsPost() {
    Map<String, String> parameters = getCompleteCreateDateAsStringQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verifyStringCreateParameterQueryInvocations();
  }

  @Test
  void testHistoricAfterAndBeforeCloseTimeQuery() {
    given()
      .queryParam("closedAfter", MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CLOSED_AFTER)
      .queryParam("closedBefore", MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CLOSED_BEFORE)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verifyClosedParameterQueryInvocations();
  }

  @Test
  void testHistoricAfterAndBeforeCloseTimeQueryAsPost() {
    Map<String, Date> parameters = getCompleteClosedDateQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verifyClosedParameterQueryInvocations();
  }

  @Test
  void testHistoricAfterAndBeforeCloseTimeAsStringQueryAsPost() {
    Map<String, String> parameters = getCompleteClosedDateAsStringQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verifyStringClosedParameterQueryInvocations();
  }

  @Test
  void testCaseActiveClosed() {
    given()
      .queryParam("active", true)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).active();
    inOrder.verify(mockedQuery).list();
  }

  @Test
  void testCaseQueryActiveAsPost() {
    Map<String, Boolean> body = new HashMap<>();
    body.put("active", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(body)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).active();
    inOrder.verify(mockedQuery).list();
  }

  @Test
  void testCaseQueryCompleted() {
    given()
      .queryParam("completed", true)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).completed();
    inOrder.verify(mockedQuery).list();
  }

  @Test
  void testCaseQueryCompletedAsPost() {
    Map<String, Boolean> body = new HashMap<>();
    body.put("completed", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(body)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).completed();
    inOrder.verify(mockedQuery).list();
  }

  @Test
  void testCaseQueryTerminated() {
    given()
      .queryParam("terminated", true)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).terminated();
    inOrder.verify(mockedQuery).list();
  }

  @Test
  void testCaseQueryTerminatedAsPost() {
    Map<String, Boolean> body = new HashMap<>();
    body.put("terminated", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(body)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).terminated();
    inOrder.verify(mockedQuery).list();
  }

  @Test
  void testCaseQueryClosed() {
    given()
      .queryParam("closed", true)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).closed();
    inOrder.verify(mockedQuery).list();
  }

  @Test
  void testCaseQueryClosedAsPost() {
    Map<String, Boolean> body = new HashMap<>();
    body.put("closed", true);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(body)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).closed();
    inOrder.verify(mockedQuery).list();
  }

  @Test
  void testCaseQueryNotClosed() {
    List<HistoricCaseInstance> mockedHistoricCaseInstances = MockProvider.createMockRunningHistoricCaseInstances();
    HistoricCaseInstanceQuery mockedHistoricCaseInstanceQuery = mock(HistoricCaseInstanceQuery.class);
    when(mockedHistoricCaseInstanceQuery.list()).thenReturn(mockedHistoricCaseInstances);
    when(processEngine.getHistoryService().createHistoricCaseInstanceQuery()).thenReturn(mockedHistoricCaseInstanceQuery);

    Response response = given()
        .queryParam("notClosed", true)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedHistoricCaseInstanceQuery);
    inOrder.verify(mockedHistoricCaseInstanceQuery).notClosed();
    inOrder.verify(mockedHistoricCaseInstanceQuery).list();

    String content = response.asString();
    List<Map<String, Object>> instances = from(content).getList("");
    Assertions.assertEquals(1, instances.size());
    assertThat(instances.get(0)).isNotNull();

    String returnedCaseInstanceId = from(content).getString("[0].id");
    String returnedCloseTime = from(content).getString("[0].closeTime");

    Assertions.assertEquals(MockProvider.EXAMPLE_CASE_INSTANCE_ID, returnedCaseInstanceId);
    Assertions.assertEquals(null, returnedCloseTime);
  }

  @Test
  void testCaseQueryNotClosedAsPost() {
    List<HistoricCaseInstance> mockedHistoricCaseInstances = MockProvider.createMockRunningHistoricCaseInstances();
    HistoricCaseInstanceQuery mockedHistoricCaseInstanceQuery = mock(HistoricCaseInstanceQuery.class);
    when(mockedHistoricCaseInstanceQuery.list()).thenReturn(mockedHistoricCaseInstances);
    when(processEngine.getHistoryService().createHistoricCaseInstanceQuery()).thenReturn(mockedHistoricCaseInstanceQuery);

    Map<String, Boolean> body = new HashMap<>();
    body.put("notClosed", true);

    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(body)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    InOrder inOrder = inOrder(mockedHistoricCaseInstanceQuery);
    inOrder.verify(mockedHistoricCaseInstanceQuery).notClosed();
    inOrder.verify(mockedHistoricCaseInstanceQuery).list();

    String content = response.asString();
    List<Map<String, Object>> instances = from(content).getList("");
    Assertions.assertEquals(1, instances.size());
    assertThat(instances.get(0)).isNotNull();

    String returnedCaseInstanceId = from(content).getString("[0].id");
    String returnedCloseTime = from(content).getString("[0].closeTime");

    Assertions.assertEquals(MockProvider.EXAMPLE_CASE_INSTANCE_ID, returnedCaseInstanceId);
    Assertions.assertEquals(null, returnedCloseTime);
  }

  @Test
  void testQueryByCaseInstanceIds() {
    given()
      .queryParam("caseInstanceIds", "firstCaseInstanceId,secondCaseInstanceId")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verifyCaseInstanceIdSetInvocation();
  }

  @Test
  void testQueryByCaseInstanceIdsAsPost() {
    Map<String, Set<String>> parameters = getCompleteCaseInstanceIdSetQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verifyCaseInstanceIdSetInvocation();
  }

  @Test
  void testQueryByCaseDefinitionKeyNotIn() {
    given()
      .queryParam("caseDefinitionKeyNotIn", "firstCaseInstanceKey,secondCaseInstanceKey")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verifyCaseDefinitionKeyNotInListInvocation();
  }

  @Test
  void testQueryByCaseDefinitionKeyNotInAsPost() {
    Map<String, List<String>> parameters = getCompleteCaseDefinitionKeyNotInListQueryParameters();

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(parameters)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verifyCaseDefinitionKeyNotInListInvocation();
  }

  @Test
  void testVariableValueEquals() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_eq_" + variableValue;
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);
    verify(mockedQuery).variableValueEquals(variableName, variableValue);
  }

  @Test
  void testVariableValueGreaterThan() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_gt_" + variableValue;
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);
    verify(mockedQuery).variableValueGreaterThan(variableName, variableValue);
  }

  @Test
  void testVariableValueGreaterThanEquals() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_gteq_" + variableValue;
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);
    verify(mockedQuery).variableValueGreaterThanOrEqual(variableName, variableValue);
  }

  @Test
  void testVariableValueLessThan() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_lt_" + variableValue;
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);
    verify(mockedQuery).variableValueLessThan(variableName, variableValue);
  }

  @Test
  void testVariableValueLessThanEquals() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_lteq_" + variableValue;
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);
    verify(mockedQuery).variableValueLessThanOrEqual(variableName, variableValue);
  }

  @Test
  void testVariableValueLike() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_like_" + variableValue;
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);
    verify(mockedQuery).variableValueLike(variableName, variableValue);
  }

  @Test
  void testVariableValueNotEquals() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_neq_" + variableValue;
    given().queryParam("variables", queryValue)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);
    verify(mockedQuery).variableValueNotEquals(variableName, variableValue);
  }

  @Test
  void testVariableValuesEqualsIgnoreCase() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_eq_" + variableValue;
    given().queryParam("variables", queryValue).queryParam("variableValuesIgnoreCase", true)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);
    verify(mockedQuery).variableValueEquals(variableName, variableValue);
    verify(mockedQuery).matchVariableValuesIgnoreCase();
  }

  @Test
  void testVariableValuesNotEqualsIgnoreCase() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_neq_" + variableValue;
    given().queryParam("variables", queryValue).queryParam("variableValuesIgnoreCase", true)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);
    verify(mockedQuery).variableValueNotEquals(variableName, variableValue);
    verify(mockedQuery).matchVariableValuesIgnoreCase();
  }

  @Test
  void testVariableValuesLikeIgnoreCase() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_like_" + variableValue;
    given().queryParam("variables", queryValue).queryParam("variableValuesIgnoreCase", true)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);
    verify(mockedQuery).variableValueLike(variableName, variableValue);
    verify(mockedQuery).matchVariableValuesIgnoreCase();
  }


  @Test
  void testVariableNamesEqualsIgnoreCase() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_eq_" + variableValue;
    given().queryParam("variables", queryValue).queryParam("variableNamesIgnoreCase", true)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);
    verify(mockedQuery).variableValueEquals(variableName, variableValue);
    verify(mockedQuery).matchVariableNamesIgnoreCase();
  }

  @Test
  void testVariableNamesNotEqualsIgnoreCase() {
    String variableName = "varName";
    String variableValue = "varValue";
    String queryValue = variableName + "_neq_" + variableValue;
    given().queryParam("variables", queryValue).queryParam("variableNamesIgnoreCase", true)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);
    verify(mockedQuery).variableValueNotEquals(variableName, variableValue);
    verify(mockedQuery).matchVariableNamesIgnoreCase();
  }

  @Test
  void testVariableValueEqualsAsPost() {
    Map<String, Object> variableJson = new HashMap<>();
    variableJson.put("name", "varName");
    variableJson.put("value", "varValue");
    variableJson.put("operator", "eq");

    List<Map<String, Object>> variables = new ArrayList<>();
    variables.add(variableJson);

    Map<String, Object> json = new HashMap<>();
    json.put("variables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueEquals("varName", "varValue");
  }

  @Test
  void testVariableValueGreaterThanAsPost() {
    Map<String, Object> variableJson = new HashMap<>();
    variableJson.put("name", "varName");
    variableJson.put("value", "varValue");
    variableJson.put("operator", "gt");

    List<Map<String, Object>> variables = new ArrayList<>();
    variables.add(variableJson);

    Map<String, Object> json = new HashMap<>();
    json.put("variables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueGreaterThan("varName", "varValue");
  }

  @Test
  void testVariableValueGreaterThanEqualsAsPost() {
    Map<String, Object> variableJson = new HashMap<>();
    variableJson.put("name", "varName");
    variableJson.put("value", "varValue");
    variableJson.put("operator", "gteq");

    List<Map<String, Object>> variables = new ArrayList<>();
    variables.add(variableJson);

    Map<String, Object> json = new HashMap<>();
    json.put("variables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueGreaterThanOrEqual("varName", "varValue");
  }

  @Test
  void testVariableValueLessThanAsPost() {
    Map<String, Object> variableJson = new HashMap<>();
    variableJson.put("name", "varName");
    variableJson.put("value", "varValue");
    variableJson.put("operator", "lt");

    List<Map<String, Object>> variables = new ArrayList<>();
    variables.add(variableJson);

    Map<String, Object> json = new HashMap<>();
    json.put("variables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueLessThan("varName", "varValue");
  }

  @Test
  void testVariableValueLessThanEqualsAsPost() {
    Map<String, Object> variableJson = new HashMap<>();
    variableJson.put("name", "varName");
    variableJson.put("value", "varValue");
    variableJson.put("operator", "lteq");

    List<Map<String, Object>> variables = new ArrayList<>();
    variables.add(variableJson);

    Map<String, Object> json = new HashMap<>();
    json.put("variables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueLessThanOrEqual("varName", "varValue");
  }

  @Test
  void testVariableValueLikeAsPost() {
    Map<String, Object> variableJson = new HashMap<>();
    variableJson.put("name", "varName");
    variableJson.put("value", "varValue");
    variableJson.put("operator", "like");

    List<Map<String, Object>> variables = new ArrayList<>();
    variables.add(variableJson);

    Map<String, Object> json = new HashMap<>();
    json.put("variables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueLike("varName", "varValue");
  }

  @Test
  void testVariableValueNotEqualsAsPost() {
    Map<String, Object> variableJson = new HashMap<>();
    variableJson.put("name", "varName");
    variableJson.put("value", "varValue");
    variableJson.put("operator", "neq");

    List<Map<String, Object>> variables = new ArrayList<>();
    variables.add(variableJson);

    Map<String, Object> json = new HashMap<>();
    json.put("variables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueNotEquals("varName", "varValue");
  }

  @Test
  void testVariableValuesEqualsIgnoreCaseAsPost() {
    Map<String, Object> variableJson = new HashMap<>();
    variableJson.put("name", "varName");
    variableJson.put("value", "varValue");
    variableJson.put("operator", "eq");

    List<Map<String, Object>> variables = new ArrayList<>();
    variables.add(variableJson);

    Map<String, Object> json = new HashMap<>();
    json.put("variables", variables);
    json.put("variableValuesIgnoreCase", true);

    given()
    .contentType(POST_JSON_CONTENT_TYPE)
    .body(json)
    .then()
    .expect()
    .statusCode(Status.OK.getStatusCode())
    .when()
    .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).matchVariableValuesIgnoreCase();
    verify(mockedQuery).variableValueEquals("varName", "varValue");
  }

  @Test
  void testVariableValuesNotEqualsIgnoreCaseAsPost() {
    Map<String, Object> variableJson = new HashMap<>();
    variableJson.put("name", "varName");
    variableJson.put("value", "varValue");
    variableJson.put("operator", "neq");

    List<Map<String, Object>> variables = new ArrayList<>();
    variables.add(variableJson);

    Map<String, Object> json = new HashMap<>();
    json.put("variables", variables);
    json.put("variableValuesIgnoreCase", true);

    given()
    .contentType(POST_JSON_CONTENT_TYPE)
    .body(json)
    .then()
    .expect()
    .statusCode(Status.OK.getStatusCode())
    .when()
    .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).matchVariableValuesIgnoreCase();
    verify(mockedQuery).variableValueNotEquals("varName", "varValue");
  }

  @Test
  void testVariableValuesLikeIgnoreCaseAsPost() {
    Map<String, Object> variableJson = new HashMap<>();
    variableJson.put("name", "varName");
    variableJson.put("value", "varValue");
    variableJson.put("operator", "like");

    List<Map<String, Object>> variables = new ArrayList<>();
    variables.add(variableJson);

    Map<String, Object> json = new HashMap<>();
    json.put("variables", variables);
    json.put("variableValuesIgnoreCase", true);

    given()
    .contentType(POST_JSON_CONTENT_TYPE)
    .body(json)
    .then()
    .expect()
    .statusCode(Status.OK.getStatusCode())
    .when()
    .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).matchVariableValuesIgnoreCase();
    verify(mockedQuery).variableValueLike("varName", "varValue");
  }


  @Test
  void testVariableNamesEqualsIgnoreCaseAsPost() {
    Map<String, Object> variableJson = new HashMap<>();
    variableJson.put("name", "varName");
    variableJson.put("value", "varValue");
    variableJson.put("operator", "eq");

    List<Map<String, Object>> variables = new ArrayList<>();
    variables.add(variableJson);

    Map<String, Object> json = new HashMap<>();
    json.put("variables", variables);
    json.put("variableNamesIgnoreCase", true);

    given()
    .contentType(POST_JSON_CONTENT_TYPE)
    .body(json)
    .then()
    .expect()
    .statusCode(Status.OK.getStatusCode())
    .when()
    .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).matchVariableNamesIgnoreCase();
    verify(mockedQuery).variableValueEquals("varName", "varValue");
  }

  @Test
  void testVariableNamesNotEqualsIgnoreCaseAsPost() {
    Map<String, Object> variableJson = new HashMap<>();
    variableJson.put("name", "varName");
    variableJson.put("value", "varValue");
    variableJson.put("operator", "neq");

    List<Map<String, Object>> variables = new ArrayList<>();
    variables.add(variableJson);

    Map<String, Object> json = new HashMap<>();
    json.put("variables", variables);
    json.put("variableNamesIgnoreCase", true);

    given()
    .contentType(POST_JSON_CONTENT_TYPE)
    .body(json)
    .then()
    .expect()
    .statusCode(Status.OK.getStatusCode())
    .when()
    .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).matchVariableNamesIgnoreCase();
    verify(mockedQuery).variableValueNotEquals("varName", "varValue");
  }

  @Test
  void testMultipleVariableParameters() {
    String variableName1 = "varName";
    String variableValue1 = "varValue";
    String variableParameter1 = variableName1 + "_eq_" + variableValue1;

    String variableName2 = "anotherVarName";
    String variableValue2 = "anotherVarValue";
    String variableParameter2 = variableName2 + "_neq_" + variableValue2;

    String queryValue = variableParameter1 + "," + variableParameter2;

    given()
      .queryParam("variables", queryValue)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueEquals(variableName1, variableValue1);
    verify(mockedQuery).variableValueNotEquals(variableName2, variableValue2);
  }

  @Test
  void testMultipleVariableParametersAsPost() {
    String variableName = "varName";
    String variableValue = "varValue";
    String anotherVariableName = "anotherVarName";
    Integer anotherVariableValue = 30;

    Map<String, Object> variableJson = new HashMap<>();
    variableJson.put("name", variableName);
    variableJson.put("operator", "eq");
    variableJson.put("value", variableValue);

    Map<String, Object> anotherVariableJson = new HashMap<>();
    anotherVariableJson.put("name", anotherVariableName);
    anotherVariableJson.put("operator", "neq");
    anotherVariableJson.put("value", anotherVariableValue);

    List<Map<String, Object>> variables = new ArrayList<>();
    variables.add(variableJson);
    variables.add(anotherVariableJson);

    Map<String, Object> json = new HashMap<>();
    json.put("variables", variables);

    given()
      .contentType(POST_JSON_CONTENT_TYPE)
      .body(json)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).variableValueEquals(variableName, variableValue);
    verify(mockedQuery).variableValueNotEquals(eq(anotherVariableName), argThat(EqualsPrimitiveValue.numberValue(anotherVariableValue)));
  }

  @Test
  void testTenantIdListParameter() {
    mockedQuery = setUpMockHistoricCaseInstanceQuery(createMockHistoricCaseInstancesTwoTenants());

    Response response = given()
      .queryParam("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID_LIST)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID, MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    verify(mockedQuery).list();

    String content = response.asString();
    List<Map<String, Object>> historicCaseInstances = from(content).getList("");
    assertThat(historicCaseInstances).hasSize(2);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    String returnedTenantId2 = from(content).getString("[1].tenantId");

    assertThat(returnedTenantId1).isEqualTo(MockProvider.EXAMPLE_TENANT_ID);
    assertThat(returnedTenantId2).isEqualTo(MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
  }

  @Test
  void testTenantIdListPostParameter() {
    mockedQuery = setUpMockHistoricCaseInstanceQuery(createMockHistoricCaseInstancesTwoTenants());

    Map<String, Object> queryParameters = new HashMap<>();
    queryParameters.put("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID_LIST.split(","));

    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(queryParameters)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID, MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    verify(mockedQuery).list();

    String content = response.asString();
    List<Map<String, Object>> historicCaseInstances = from(content).getList("");
    assertThat(historicCaseInstances).hasSize(2);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    String returnedTenantId2 = from(content).getString("[1].tenantId");

    assertThat(returnedTenantId1).isEqualTo(MockProvider.EXAMPLE_TENANT_ID);
    assertThat(returnedTenantId2).isEqualTo(MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
  }

  @Test
  void testWithoutTenantIdParameter() {
    mockedQuery = setUpMockHistoricCaseInstanceQuery(Arrays.asList(MockProvider.createMockHistoricCaseInstance(null)));

    Response response = given()
      .queryParam("withoutTenantId", true)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).withoutTenantId();
    verify(mockedQuery).list();

    String content = response.asString();
    List<Map<String, Object>> definitions = from(content).getList("");
    assertThat(definitions).hasSize(1);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    assertThat(returnedTenantId1).isNull();
  }

  @Test
  void testWithoutTenantIdPostParameter() {
    mockedQuery = setUpMockHistoricCaseInstanceQuery(Arrays.asList(MockProvider.createMockHistoricCaseInstance(null)));

    Map<String, Object> queryParameters = new HashMap<>();
    queryParameters.put("withoutTenantId", true);

    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(queryParameters)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).withoutTenantId();
    verify(mockedQuery).list();

    String content = response.asString();
    List<Map<String, Object>> definitions = from(content).getList("");
    assertThat(definitions).hasSize(1);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    assertThat(returnedTenantId1).isNull();
  }

  private List<HistoricCaseInstance> createMockHistoricCaseInstancesTwoTenants() {
    return Arrays.asList(
        MockProvider.createMockHistoricCaseInstance(MockProvider.EXAMPLE_TENANT_ID),
        MockProvider.createMockHistoricCaseInstance(MockProvider.ANOTHER_EXAMPLE_TENANT_ID));
  }

  @Test
  void testCaseActivityIdListParameter() {

    Response response = given()
      .queryParam("caseActivityIdIn", MockProvider.EXAMPLE_CASE_ACTIVITY_ID_LIST)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).caseActivityIdIn(MockProvider.EXAMPLE_CASE_ACTIVITY_ID, MockProvider.ANOTHER_EXAMPLE_CASE_ACTIVITY_ID);
    verify(mockedQuery).list();

    String content = response.asString();
    List<Map<String, Object>> historicCaseInstances = from(content).getList("");
    assertThat(historicCaseInstances).hasSize(1);
  }

  @Test
  void testCaseActivityIdListPostParameter() {

    Map<String, Object> queryParameters = new HashMap<>();
    queryParameters.put("caseActivityIdIn", MockProvider.EXAMPLE_CASE_ACTIVITY_ID_LIST.split(","));

    Response response = given()
        .contentType(POST_JSON_CONTENT_TYPE)
        .body(queryParameters)
    .expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .post(HISTORIC_CASE_INSTANCE_RESOURCE_URL);

    verify(mockedQuery).caseActivityIdIn(MockProvider.EXAMPLE_CASE_ACTIVITY_ID, MockProvider.ANOTHER_EXAMPLE_CASE_ACTIVITY_ID);
    verify(mockedQuery).list();

    String content = response.asString();
    List<Map<String, Object>> historicCaseInstances = from(content).getList("");
    assertThat(historicCaseInstances).hasSize(1);
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given()
      .queryParam("sortBy", sortBy)
      .queryParam("sortOrder", sortOrder)
      .then().expect()
      .statusCode(expectedStatus.getStatusCode())
      .when()
      .get(HISTORIC_CASE_INSTANCE_RESOURCE_URL);
  }

  protected Map<String, String> getCompleteStringQueryParameters() {
    Map<String, String> parameters = new HashMap<>();

    parameters.put("caseInstanceId", MockProvider.EXAMPLE_CASE_INSTANCE_ID);
    parameters.put("caseInstanceBusinessKey", MockProvider.EXAMPLE_CASE_INSTANCE_BUSINESS_KEY);
    parameters.put("caseInstanceBusinessKeyLike", MockProvider.EXAMPLE_CASE_INSTANCE_BUSINESS_KEY_LIKE);
    parameters.put("caseDefinitionId", MockProvider.EXAMPLE_CASE_DEFINITION_ID);
    parameters.put("caseDefinitionKey", MockProvider.EXAMPLE_CASE_DEFINITION_KEY);
    parameters.put("caseDefinitionName", MockProvider.EXAMPLE_CASE_DEFINITION_NAME);
    parameters.put("caseDefinitionNameLike", MockProvider.EXAMPLE_CASE_DEFINITION_NAME_LIKE);
    parameters.put("createdBy", "createdBySomeone");
    parameters.put("superCaseInstanceId", MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_SUPER_CASE_INSTANCE_ID);
    parameters.put("subCaseInstanceId", MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_SUB_CASE_INSTANCE_ID);
    parameters.put("superProcessInstanceId", MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_SUPER_PROCESS_INSTANCE_ID);
    parameters.put("subProcessInstanceId", MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_SUB_PROCESS_INSTANCE_ID);

    return parameters;
  }

  protected void verifyStringParameterQueryInvocations() {
    Map<String, String> stringQueryParameters = getCompleteStringQueryParameters();

    verify(mockedQuery).caseInstanceId(stringQueryParameters.get("caseInstanceId"));
    verify(mockedQuery).caseInstanceBusinessKey(stringQueryParameters.get("caseInstanceBusinessKey"));
    verify(mockedQuery).caseInstanceBusinessKeyLike(stringQueryParameters.get("caseInstanceBusinessKeyLike"));
    verify(mockedQuery).caseDefinitionId(stringQueryParameters.get("caseDefinitionId"));
    verify(mockedQuery).caseDefinitionKey(stringQueryParameters.get("caseDefinitionKey"));
    verify(mockedQuery).caseDefinitionName(stringQueryParameters.get("caseDefinitionName"));
    verify(mockedQuery).caseDefinitionNameLike(stringQueryParameters.get("caseDefinitionNameLike"));
    verify(mockedQuery).createdBy(stringQueryParameters.get("createdBy"));
    verify(mockedQuery).superCaseInstanceId(stringQueryParameters.get("superCaseInstanceId"));
    verify(mockedQuery).subCaseInstanceId(stringQueryParameters.get("subCaseInstanceId"));
    verify(mockedQuery).superProcessInstanceId(stringQueryParameters.get("superProcessInstanceId"));
    verify(mockedQuery).subProcessInstanceId(stringQueryParameters.get("subProcessInstanceId"));
    verify(mockedQuery).caseInstanceId(stringQueryParameters.get("caseInstanceId"));

    verify(mockedQuery).list();
  }

  protected Map<String, Date> getCompleteCreateDateQueryParameters() {
    Map<String, Date> parameters = new HashMap<>();

    parameters.put("createdAfter", DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CREATED_AFTER));
    parameters.put("createdBefore", DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CREATED_BEFORE));

    return parameters;
  }

  protected void verifyCreateParameterQueryInvocations() {
    Map<String, Date> createDateParameters = getCompleteCreateDateQueryParameters();

    verify(mockedQuery).createdBefore(createDateParameters.get("createdBefore"));
    verify(mockedQuery).createdAfter(createDateParameters.get("createdAfter"));

    verify(mockedQuery).list();
  }

  protected Map<String, String> getCompleteCreateDateAsStringQueryParameters() {
    Map<String, String> parameters = new HashMap<>();

    parameters.put("createdAfter", MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CREATED_AFTER);
    parameters.put("createdBefore", MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CREATED_BEFORE);

    return parameters;
  }

  protected void verifyStringCreateParameterQueryInvocations() {
    Map<String, String> createDateParameters = getCompleteCreateDateAsStringQueryParameters();

    verify(mockedQuery).createdBefore(DateTimeUtil.parseDate(createDateParameters.get("createdBefore")));
    verify(mockedQuery).createdAfter(DateTimeUtil.parseDate(createDateParameters.get("createdAfter")));

    verify(mockedQuery).list();
  }

  protected Map<String, Date> getCompleteClosedDateQueryParameters() {
    Map<String, Date> parameters = new HashMap<>();

    parameters.put("closedAfter", DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CLOSED_AFTER));
    parameters.put("closedBefore", DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CLOSED_BEFORE));

    return parameters;
  }

  protected void verifyClosedParameterQueryInvocations() {
    Map<String, Date> closedDateParameters = getCompleteClosedDateQueryParameters();

    verify(mockedQuery).closedAfter(closedDateParameters.get("closedAfter"));
    verify(mockedQuery).closedBefore(closedDateParameters.get("closedBefore"));

    verify(mockedQuery).list();
  }

  protected Map<String, String> getCompleteClosedDateAsStringQueryParameters() {
    Map<String, String> parameters = new HashMap<>();

    parameters.put("closedAfter", MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CLOSED_AFTER);
    parameters.put("closedBefore", MockProvider.EXAMPLE_HISTORIC_CASE_INSTANCE_CLOSED_BEFORE);

    return parameters;
  }

  protected void verifyStringClosedParameterQueryInvocations() {
    Map<String, String> closedDateParameters = getCompleteClosedDateAsStringQueryParameters();

    verify(mockedQuery).closedAfter(DateTimeUtil.parseDate(closedDateParameters.get("closedAfter")));
    verify(mockedQuery).closedBefore(DateTimeUtil.parseDate(closedDateParameters.get("closedBefore")));

    verify(mockedQuery).list();
  }

  protected Map<String, Set<String>> getCompleteCaseInstanceIdSetQueryParameters() {
    Map<String, Set<String>> parameters = new HashMap<>();

    Set<String> caseInstanceIds = new HashSet<>();
    caseInstanceIds.add("firstCaseInstanceId");
    caseInstanceIds.add("secondCaseInstanceId");

    parameters.put("caseInstanceIds", caseInstanceIds);

    return parameters;
  }

  protected void verifyCaseInstanceIdSetInvocation() {
    Map<String, Set<String>> parameters = getCompleteCaseInstanceIdSetQueryParameters();

    verify(mockedQuery).caseInstanceIds(parameters.get("caseInstanceIds"));
    verify(mockedQuery).list();
  }

  protected Map<String, List<String>> getCompleteCaseDefinitionKeyNotInListQueryParameters() {
    Map<String, List<String>> parameters = new HashMap<>();

    List<String> caseInstanceIds = new ArrayList<>();
    caseInstanceIds.add("firstCaseInstanceKey");
    caseInstanceIds.add("secondCaseInstanceKey");

    parameters.put("caseDefinitionKeyNotIn", caseInstanceIds);

    return parameters;
  }

  protected void verifyCaseDefinitionKeyNotInListInvocation() {
    Map<String, List<String>> parameters = getCompleteCaseDefinitionKeyNotInListQueryParameters();

    verify(mockedQuery).caseDefinitionKeyNotIn(parameters.get("caseDefinitionKeyNotIn"));
    verify(mockedQuery).list();
  }

}

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
package org.operaton.bpm.engine.rest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.operaton.bpm.engine.rest.util.JsonPathUtil.from;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.operaton.bpm.engine.batch.BatchStatistics;
import org.operaton.bpm.engine.batch.BatchStatisticsQuery;
import org.operaton.bpm.engine.impl.calendar.DateTimeUtil;
import org.operaton.bpm.engine.rest.dto.batch.BatchStatisticsDto;
import org.operaton.bpm.engine.rest.exception.InvalidRequestException;
import org.operaton.bpm.engine.rest.helper.MockProvider;
import org.operaton.bpm.engine.rest.util.container.TestContainerExtension;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.ws.rs.core.Response.Status;

public class BatchRestServiceStatisticsTest extends AbstractRestServiceTest {

  @RegisterExtension
  public static TestContainerExtension rule = new TestContainerExtension();

  protected static final String BATCH_RESOURCE_URL = TEST_RESOURCE_ROOT_PATH + "/batch";
  protected static final String BATCH_STATISTICS_URL = BATCH_RESOURCE_URL + "/statistics";
  protected static final String BATCH_STATISTICS_COUNT_URL = BATCH_STATISTICS_URL + "/count";

  protected BatchStatisticsQuery queryMock;
  protected List<BatchStatistics> mockBatchStatisticsList;

  @BeforeEach
  void setUpBatchStatisticsMock() {
    mockBatchStatisticsList = MockProvider.createMockBatchStatisticsList();

    queryMock = mock(BatchStatisticsQuery.class);

    when(queryMock.list()).thenReturn(mockBatchStatisticsList);
    when(queryMock.count()).thenReturn((long) mockBatchStatisticsList.size());

    when(processEngine.getManagementService().createBatchStatisticsQuery()).thenReturn(queryMock);
  }

  @Test
  void testQuery() {
    Response response = given()
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(BATCH_STATISTICS_URL);

    verify(queryMock).list();
    verifyNoMoreInteractions(queryMock);

    verifyBatchStatisticsListJson(response.asString());
  }

  @Test
  void testUnknownQueryParameter() {
    Response response = given()
        .queryParam("unknown", "unknown")
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(BATCH_STATISTICS_URL);

    verify(queryMock, never()).batchId(anyString());
    verify(queryMock).list();
    verifyNoMoreInteractions(queryMock);

    verifyBatchStatisticsListJson(response.asString());
  }

  @Test
  void testBatchQueryByBatchId() {
    Response response = given()
        .queryParam("batchId", MockProvider.EXAMPLE_BATCH_ID)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
      .get(BATCH_STATISTICS_URL);

    InOrder inOrder = inOrder(queryMock);
    inOrder.verify(queryMock).batchId(MockProvider.EXAMPLE_BATCH_ID);
    inOrder.verify(queryMock).list();
    inOrder.verifyNoMoreInteractions();

    verifyBatchStatisticsListJson(response.asString());
  }

  @Test
  void testQueryActiveBatches() {
    Response response = given()
        .queryParam("suspended", false)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(BATCH_STATISTICS_URL);

    InOrder inOrder = inOrder(queryMock);
    inOrder.verify(queryMock).active();
    inOrder.verify(queryMock).list();
    inOrder.verifyNoMoreInteractions();

    verifyBatchStatisticsListJson(response.asString());
  }

  @Test
  void testFullBatchQuery() {
    Response response = given()
        .queryParams(getCompleteQueryParameters())
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(BATCH_STATISTICS_URL);

    verifyQueryParameterInvocations();
    verify(queryMock).list();
    verifyNoMoreInteractions(queryMock);

    verifyBatchStatisticsListJson(response.asString());
  }

  @Test
  void testQueryCount() {
    given()
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .get(BATCH_STATISTICS_COUNT_URL);

    verify(queryMock).count();
    verifyNoMoreInteractions(queryMock);
  }

  @Test
  void testFullQueryCount() {
    given()
      .params(getCompleteQueryParameters())
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
    .when()
      .get(BATCH_STATISTICS_COUNT_URL);

    verifyQueryParameterInvocations();
    verify(queryMock).count();
    verifyNoMoreInteractions(queryMock);
  }


  @Test
  void testQueryPagination() {
    when(queryMock.listPage(1, 2))
      .thenReturn(mockBatchStatisticsList);

    Response response = given()
        .queryParam("firstResult", 1)
        .queryParam("maxResults", 2)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(BATCH_STATISTICS_URL);


    verify(queryMock).listPage(1, 2);
    verifyNoMoreInteractions(queryMock);

    verifyBatchStatisticsListJson(response.asString());
  }

  @Test
  void testSortingParameters() {
    InOrder inOrder = Mockito.inOrder(queryMock);
    executeAndVerifySorting("batchId", "desc", Status.OK);
    inOrder.verify(queryMock).orderById();
    inOrder.verify(queryMock).desc();

    inOrder = Mockito.inOrder(queryMock);
    executeAndVerifySorting("batchId", "asc", Status.OK);
    inOrder.verify(queryMock).orderById();
    inOrder.verify(queryMock).asc();

    inOrder = Mockito.inOrder(queryMock);
    executeAndVerifySorting("tenantId", "asc", Status.OK);
    inOrder.verify(queryMock).orderByTenantId();
    inOrder.verify(queryMock).asc();

    inOrder = Mockito.inOrder(queryMock);
    executeAndVerifySorting("tenantId", "desc", Status.OK);
    inOrder.verify(queryMock).orderByTenantId();
    inOrder.verify(queryMock).desc();

    inOrder = Mockito.inOrder(queryMock);
    executeAndVerifySorting("startTime", "asc", Status.OK);
    inOrder.verify(queryMock).orderByStartTime();
    inOrder.verify(queryMock).asc();

    inOrder = Mockito.inOrder(queryMock);
    executeAndVerifySorting("startTime", "desc", Status.OK);
    inOrder.verify(queryMock).orderByStartTime();
    inOrder.verify(queryMock).desc();
  }

  @Test
  void testSortByParameterOnly() {
    given()
      .queryParam("sortBy", "batchId")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type",
        equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message",
        equalTo("Only a single sorting parameter specified. sortBy and sortOrder required"))
    .when()
      .get(BATCH_STATISTICS_URL);
  }

  @Test
  void testSortOrderParameterOnly() {
    given()
      .queryParam("sortOrder", "asc")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type",
        equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message",
        equalTo("Only a single sorting parameter specified. sortBy and sortOrder required"))
    .when()
      .get(BATCH_STATISTICS_URL);
  }

  protected Map<String, Object> getCompleteQueryParameters() {
    Map<String, Object> parameters = new HashMap<>();

    parameters.put("batchId", MockProvider.EXAMPLE_BATCH_ID);
    parameters.put("type", MockProvider.EXAMPLE_BATCH_TYPE);
    parameters.put("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID + "," + MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    parameters.put("withoutTenantId", true);
    parameters.put("suspended", true);
    parameters.put("createdBy", MockProvider.EXAMPLE_USER_ID);
    parameters.put("startedBefore", MockProvider.EXAMPLE_HISTORIC_BATCH_START_TIME);
    parameters.put("startedAfter", MockProvider.EXAMPLE_HISTORIC_BATCH_END_TIME);
    parameters.put("withFailures", true);
    parameters.put("withoutFailures", true);

    return parameters;
  }

  protected void verifyQueryParameterInvocations() {
    verify(queryMock).batchId(MockProvider.EXAMPLE_BATCH_ID);
    verify(queryMock).type(MockProvider.EXAMPLE_BATCH_TYPE);
    verify(queryMock).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID, MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    verify(queryMock).withoutTenantId();
    verify(queryMock).suspended();
    verify(queryMock).createdBy(MockProvider.EXAMPLE_USER_ID);
    verify(queryMock).startedBefore(DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_BATCH_START_TIME));
    verify(queryMock).startedAfter(DateTimeUtil.parseDate(MockProvider.EXAMPLE_HISTORIC_BATCH_END_TIME));
    verify(queryMock).withFailures();
    verify(queryMock).withoutFailures();
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given()
      .queryParam("sortBy", sortBy)
      .queryParam("sortOrder", sortOrder)
    .then().expect()
      .statusCode(expectedStatus.getStatusCode())
    .when()
      .get(BATCH_STATISTICS_URL);
  }

  protected void verifyBatchStatisticsListJson(String batchStatisticsListJson) {
    List<Object> batches = from(batchStatisticsListJson).get();
    assertEquals(1, batches.size(), "There should be one batch statistics returned.");

    BatchStatisticsDto batchStatistics = from(batchStatisticsListJson).getObject("[0]", BatchStatisticsDto.class);
    String returnedStartTime = from(batchStatisticsListJson).getString("[0].startTime");

    assertThat(batchStatistics).as("The returned batch statistics should not be null.").isNotNull();
    assertEquals(MockProvider.EXAMPLE_BATCH_ID, batchStatistics.getId());
    assertEquals(MockProvider.EXAMPLE_BATCH_TYPE, batchStatistics.getType());
    assertEquals(MockProvider.EXAMPLE_BATCH_TOTAL_JOBS, batchStatistics.getTotalJobs());
    assertEquals(MockProvider.EXAMPLE_BATCH_JOBS_CREATED, batchStatistics.getJobsCreated());
    assertEquals(MockProvider.EXAMPLE_BATCH_JOBS_PER_SEED, batchStatistics.getBatchJobsPerSeed());
    assertEquals(MockProvider.EXAMPLE_INVOCATIONS_PER_BATCH_JOB, batchStatistics.getInvocationsPerBatchJob());
    assertEquals(MockProvider.EXAMPLE_SEED_JOB_DEFINITION_ID, batchStatistics.getSeedJobDefinitionId());
    assertEquals(MockProvider.EXAMPLE_MONITOR_JOB_DEFINITION_ID, batchStatistics.getMonitorJobDefinitionId());
    assertEquals(MockProvider.EXAMPLE_BATCH_JOB_DEFINITION_ID, batchStatistics.getBatchJobDefinitionId());
    assertEquals(MockProvider.EXAMPLE_TENANT_ID, batchStatistics.getTenantId());
    assertEquals(MockProvider.EXAMPLE_USER_ID, batchStatistics.getCreateUserId());
    assertEquals(MockProvider.EXAMPLE_HISTORIC_BATCH_START_TIME, returnedStartTime);
    assertEquals(MockProvider.EXAMPLE_BATCH_REMAINING_JOBS, batchStatistics.getRemainingJobs());
    assertEquals(MockProvider.EXAMPLE_BATCH_COMPLETED_JOBS, batchStatistics.getCompletedJobs());
    assertEquals(MockProvider.EXAMPLE_BATCH_FAILED_JOBS, batchStatistics.getFailedJobs());
    assertThat(batchStatistics.isSuspended()).isTrue();
  }

}

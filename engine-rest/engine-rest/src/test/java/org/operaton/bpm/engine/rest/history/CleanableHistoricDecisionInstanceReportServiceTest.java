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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.core.Response.Status;

import org.operaton.bpm.engine.AuthorizationException;
import org.operaton.bpm.engine.history.CleanableHistoricDecisionInstanceReport;
import org.operaton.bpm.engine.history.CleanableHistoricDecisionInstanceReportResult;
import org.operaton.bpm.engine.rest.AbstractRestServiceTest;
import org.operaton.bpm.engine.rest.exception.InvalidRequestException;
import org.operaton.bpm.engine.rest.util.container.TestContainerExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class CleanableHistoricDecisionInstanceReportServiceTest extends AbstractRestServiceTest {

  private static final String EXAMPLE_DD_ID = "anId";
  private static final String EXAMPLE_DD_KEY = "aKey";
  private static final String EXAMPLE_DD_NAME = "aName";
  private static final int EXAMPLE_DD_VERSION = 42;
  private static final int EXAMPLE_TTL = 5;
  private static final long EXAMPLE_FINISHED_DI_COUNT = 1000l;
  private static final long EXAMPLE_CLEANABLE_DI_COUNT = 567l;
  private static final String EXAMPLE_TENANT_ID = "aTenantId";

  protected static final String ANOTHER_EXAMPLE_DD_ID = "anotherDefId";
  protected static final String ANOTHER_EXAMPLE_DD_KEY = "anotherDefKey";
  protected static final String ANOTHER_EXAMPLE_TENANT_ID = "anotherTenantId";


  @RegisterExtension
  public static TestContainerExtension rule = new TestContainerExtension();

  protected static final String HISTORY_URL = TEST_RESOURCE_ROOT_PATH + "/history/decision-definition";
  protected static final String HISTORIC_REPORT_URL = HISTORY_URL + "/cleanable-decision-instance-report";
  protected static final String HISTORIC_REPORT_COUNT_URL = HISTORIC_REPORT_URL + "/count";

  private CleanableHistoricDecisionInstanceReport historicDecisionInstanceReport;

  @BeforeEach
  void setUpRuntimeData() {
    setupHistoryReportMock();
  }

  private void setupHistoryReportMock() {
    CleanableHistoricDecisionInstanceReport report = mock(CleanableHistoricDecisionInstanceReport.class);

    when(report.decisionDefinitionIdIn(anyString())).thenReturn(report);
    when(report.decisionDefinitionKeyIn(anyString())).thenReturn(report);

    CleanableHistoricDecisionInstanceReportResult reportResult = mock(CleanableHistoricDecisionInstanceReportResult.class);

    when(reportResult.getDecisionDefinitionId()).thenReturn(EXAMPLE_DD_ID);
    when(reportResult.getDecisionDefinitionKey()).thenReturn(EXAMPLE_DD_KEY);
    when(reportResult.getDecisionDefinitionName()).thenReturn(EXAMPLE_DD_NAME);
    when(reportResult.getDecisionDefinitionVersion()).thenReturn(EXAMPLE_DD_VERSION);
    when(reportResult.getHistoryTimeToLive()).thenReturn(EXAMPLE_TTL);
    when(reportResult.getFinishedDecisionInstanceCount()).thenReturn(EXAMPLE_FINISHED_DI_COUNT);
    when(reportResult.getCleanableDecisionInstanceCount()).thenReturn(EXAMPLE_CLEANABLE_DI_COUNT);
    when(reportResult.getTenantId()).thenReturn(EXAMPLE_TENANT_ID);

    CleanableHistoricDecisionInstanceReportResult anotherReportResult = mock(CleanableHistoricDecisionInstanceReportResult.class);

    when(anotherReportResult.getDecisionDefinitionId()).thenReturn(ANOTHER_EXAMPLE_DD_ID);
    when(anotherReportResult.getDecisionDefinitionKey()).thenReturn(ANOTHER_EXAMPLE_DD_KEY);
    when(anotherReportResult.getDecisionDefinitionName()).thenReturn("dpName");
    when(anotherReportResult.getDecisionDefinitionVersion()).thenReturn(33);
    when(anotherReportResult.getHistoryTimeToLive()).thenReturn(5);
    when(anotherReportResult.getFinishedDecisionInstanceCount()).thenReturn(10L);
    when(anotherReportResult.getCleanableDecisionInstanceCount()).thenReturn(0L);
    when(anotherReportResult.getTenantId()).thenReturn(ANOTHER_EXAMPLE_TENANT_ID);


    List<CleanableHistoricDecisionInstanceReportResult> mocks = new ArrayList<>();
    mocks.add(reportResult);
    mocks.add(anotherReportResult);

    when(report.list()).thenReturn(mocks);
    when(report.count()).thenReturn((long) mocks.size());

    historicDecisionInstanceReport = report;
    when(processEngine.getHistoryService().createCleanableHistoricDecisionInstanceReport()).thenReturn(historicDecisionInstanceReport);
  }

  @Test
  void testGetReport() {
    given()
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
    .when().get(HISTORIC_REPORT_URL);

    InOrder inOrder = Mockito.inOrder(historicDecisionInstanceReport);
    inOrder.verify(historicDecisionInstanceReport).list();
    verifyNoMoreInteractions(historicDecisionInstanceReport);
  }

  @Test
  void testReportRetrieval() {
    Response response = given()
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .contentType(ContentType.JSON)
    .when().get(HISTORIC_REPORT_URL);

    // assert query invocation
    InOrder inOrder = Mockito.inOrder(historicDecisionInstanceReport);
    inOrder.verify(historicDecisionInstanceReport).list();

    String content = response.asString();
    List<Map<String, Object>> reportResults = from(content).getList("");
    Assertions.assertEquals(2, reportResults.size(), "There should be two report results returned.");
    assertThat(reportResults.get(0)).isNotNull();

    String returnedDefinitionId = from(content).getString("[0].decisionDefinitionId");
    String returnedDefinitionKey = from(content).getString("[0].decisionDefinitionKey");
    String returnedDefinitionName = from(content).getString("[0].decisionDefinitionName");
    int returnedDefinitionVersion = from(content).getInt("[0].decisionDefinitionVersion");
    int returnedTTL = from(content).getInt("[0].historyTimeToLive");
    long returnedFinishedCount= from(content).getLong("[0].finishedDecisionInstanceCount");
    long returnedCleanableCount = from(content).getLong("[0].cleanableDecisionInstanceCount");
    String returnedTenantId = from(content).getString("[0].tenantId");

    Assertions.assertEquals(EXAMPLE_DD_ID, returnedDefinitionId);
    Assertions.assertEquals(EXAMPLE_DD_KEY, returnedDefinitionKey);
    Assertions.assertEquals(EXAMPLE_DD_NAME, returnedDefinitionName);
    Assertions.assertEquals(EXAMPLE_DD_VERSION, returnedDefinitionVersion);
    Assertions.assertEquals(EXAMPLE_TTL, returnedTTL);
    Assertions.assertEquals(EXAMPLE_FINISHED_DI_COUNT, returnedFinishedCount);
    Assertions.assertEquals(EXAMPLE_CLEANABLE_DI_COUNT, returnedCleanableCount);
    Assertions.assertEquals(EXAMPLE_TENANT_ID, returnedTenantId);
  }

  @Test
  void testMissingAuthorization() {
    String message = "not authorized";
    when(historicDecisionInstanceReport.list()).thenThrow(new AuthorizationException(message));

    given()
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when().get(HISTORIC_REPORT_URL);
  }

  @Test
  void testQueryByDefinitionId() {
    given()
      .queryParam("decisionDefinitionIdIn",  EXAMPLE_DD_ID + "," + ANOTHER_EXAMPLE_DD_ID)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
      .when()
        .get(HISTORIC_REPORT_URL);

    verify(historicDecisionInstanceReport).decisionDefinitionIdIn(EXAMPLE_DD_ID, ANOTHER_EXAMPLE_DD_ID);
    verify(historicDecisionInstanceReport).list();
    verifyNoMoreInteractions(historicDecisionInstanceReport);
  }

  @Test
  void testQueryByDefinitionKey() {
    given()
      .queryParam("decisionDefinitionKeyIn", EXAMPLE_DD_KEY + "," + ANOTHER_EXAMPLE_DD_KEY)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
      .when()
        .get(HISTORIC_REPORT_URL);

    verify(historicDecisionInstanceReport).decisionDefinitionKeyIn(EXAMPLE_DD_KEY, ANOTHER_EXAMPLE_DD_KEY);
    verify(historicDecisionInstanceReport).list();
    verifyNoMoreInteractions(historicDecisionInstanceReport);
  }

  @Test
  void testQueryByTenantId() {
    given()
      .queryParam("tenantIdIn", EXAMPLE_TENANT_ID + "," + ANOTHER_EXAMPLE_TENANT_ID)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
      .when()
        .get(HISTORIC_REPORT_URL);

    verify(historicDecisionInstanceReport).tenantIdIn(EXAMPLE_TENANT_ID, ANOTHER_EXAMPLE_TENANT_ID);
    verify(historicDecisionInstanceReport).list();
    verifyNoMoreInteractions(historicDecisionInstanceReport);
  }

  @Test
  void testQueryWithoutTenantId() {
    given()
      .queryParam("withoutTenantId", true)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
      .when()
        .get(HISTORIC_REPORT_URL);

    verify(historicDecisionInstanceReport).withoutTenantId();
    verify(historicDecisionInstanceReport).list();
    verifyNoMoreInteractions(historicDecisionInstanceReport);
  }

  @Test
  void testQueryCompact() {
    given()
      .queryParam("compact", true)
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
      .when()
        .get(HISTORIC_REPORT_URL);

    verify(historicDecisionInstanceReport).compact();
    verify(historicDecisionInstanceReport).list();
    verifyNoMoreInteractions(historicDecisionInstanceReport);
  }

  @Test
  void testFullQuery() {
    given()
      .params(getCompleteQueryParameters())
    .then()
      .expect()
        .statusCode(Status.OK.getStatusCode())
        .contentType(ContentType.JSON)
      .when()
        .get(HISTORIC_REPORT_URL);

    verifyQueryParameterInvocations();
    verify(historicDecisionInstanceReport).list();
  }

  @Test
  void testQueryCount() {
    expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(2))
    .when()
      .get(HISTORIC_REPORT_COUNT_URL);

    verify(historicDecisionInstanceReport).count();
    verifyNoMoreInteractions(historicDecisionInstanceReport);
  }

  @Test
  void testFullQueryCount() {
    given()
      .params(getCompleteQueryParameters())
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(2))
    .when()
      .get(HISTORIC_REPORT_COUNT_URL);

    verifyQueryParameterInvocations();
    verify(historicDecisionInstanceReport).count();
  }

  @Test
  void testOrderByFinishedDecisionInstanceAsc() {
    given()
      .queryParam("sortBy", "finished")
      .queryParam("sortOrder", "asc")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
        .get(HISTORIC_REPORT_URL);

    verify(historicDecisionInstanceReport).orderByFinished();
    verify(historicDecisionInstanceReport).asc();
    verify(historicDecisionInstanceReport).list();
    verifyNoMoreInteractions(historicDecisionInstanceReport);
  }

  @Test
  void testOrderByFinishedDecisionInstanceDesc() {
    given()
      .queryParam("sortBy", "finished")
      .queryParam("sortOrder", "desc")
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
        .get(HISTORIC_REPORT_URL);

    verify(historicDecisionInstanceReport).orderByFinished();
    verify(historicDecisionInstanceReport).desc();
    verify(historicDecisionInstanceReport).list();
    verifyNoMoreInteractions(historicDecisionInstanceReport);
  }

  @Test
  void testSortOrderParameterOnly() {
    given()
    .queryParam("sortOrder", "asc")
  .then()
    .expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
      .contentType(ContentType.JSON)
      .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
      .body("message", containsString("Only a single sorting parameter specified. sortBy and sortOrder required"))
    .when()
      .get(HISTORIC_REPORT_URL);
  }

  @Test
  void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("finished", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given()
      .queryParam("sortBy", sortBy)
      .queryParam("sortOrder", sortOrder)
    .then()
      .expect()
        .statusCode(expectedStatus.getStatusCode())
      .when()
        .get(HISTORIC_REPORT_URL);
  }

  protected Map<String, Object> getCompleteQueryParameters() {
    Map<String, Object> parameters = new HashMap<>();

    parameters.put("decisionDefinitionIdIn", EXAMPLE_DD_ID + "," + ANOTHER_EXAMPLE_DD_ID);
    parameters.put("decisionDefinitionKeyIn", EXAMPLE_DD_KEY + "," + ANOTHER_EXAMPLE_DD_KEY);
    parameters.put("tenantIdIn", EXAMPLE_TENANT_ID + "," + ANOTHER_EXAMPLE_TENANT_ID);
    parameters.put("withoutTenantId", true);
    parameters.put("compact", true);

    return parameters;
  }

  protected void verifyQueryParameterInvocations() {
    verify(historicDecisionInstanceReport).decisionDefinitionIdIn(EXAMPLE_DD_ID, ANOTHER_EXAMPLE_DD_ID);
    verify(historicDecisionInstanceReport).decisionDefinitionKeyIn(EXAMPLE_DD_KEY, ANOTHER_EXAMPLE_DD_KEY);
    verify(historicDecisionInstanceReport).tenantIdIn(EXAMPLE_TENANT_ID, ANOTHER_EXAMPLE_TENANT_ID);
    verify(historicDecisionInstanceReport).withoutTenantId();
    verify(historicDecisionInstanceReport).compact();
  }
}

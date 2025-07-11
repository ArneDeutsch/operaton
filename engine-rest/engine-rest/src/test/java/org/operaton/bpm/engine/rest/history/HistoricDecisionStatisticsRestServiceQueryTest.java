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

import org.operaton.bpm.engine.history.HistoricDecisionInstanceStatistics;
import org.operaton.bpm.engine.history.HistoricDecisionInstanceStatisticsQuery;
import org.operaton.bpm.engine.impl.HistoricDecisionInstanceStatisticsQueryImpl;
import org.operaton.bpm.engine.rest.AbstractRestServiceTest;
import org.operaton.bpm.engine.rest.helper.MockProvider;
import org.operaton.bpm.engine.rest.util.container.TestContainerExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.ws.rs.core.Response;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Askar Akhmerov
 */
public class HistoricDecisionStatisticsRestServiceQueryTest extends AbstractRestServiceTest {

  @RegisterExtension
  public static TestContainerExtension rule = new TestContainerExtension();

  protected static final String HISTORY_URL = TEST_RESOURCE_ROOT_PATH + "/history";
  protected static final String HISTORIC_DECISION_STATISTICS_URL = HISTORY_URL + "/decision-requirements-definition/{id}/statistics";

  private HistoricDecisionInstanceStatisticsQuery historicDecisionInstanceStatisticsQuery;

  @BeforeEach
  void setUpRuntimeData() {
    setupHistoricDecisionStatisticsMock();
  }

  @AfterEach
  void tearDown() {
    Mockito.reset(processEngine.getHistoryService(), historicDecisionInstanceStatisticsQuery);
  }

  private void setupHistoricDecisionStatisticsMock() {
    List<HistoricDecisionInstanceStatistics> mocks = MockProvider.createMockHistoricDecisionStatistics();

    historicDecisionInstanceStatisticsQuery =
        mock(HistoricDecisionInstanceStatisticsQueryImpl.class);
    when(processEngine.getHistoryService()
        .createHistoricDecisionInstanceStatisticsQuery(MockProvider.EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID))
    .thenReturn(historicDecisionInstanceStatisticsQuery);

    when(historicDecisionInstanceStatisticsQuery.decisionInstanceId(MockProvider.EXAMPLE_DECISION_INSTANCE_ID)).thenReturn(historicDecisionInstanceStatisticsQuery);
    when(historicDecisionInstanceStatisticsQuery.unlimitedList()).thenReturn(mocks);
  }

  @Test
  void testHistoricDefinitionInstanceStatisticsRetrieval() {
    given().pathParam("id", MockProvider.EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID)
        .then().expect()
        .statusCode(Response.Status.OK.getStatusCode())
          .body("$.size()", is(2))
          .body("decisionDefinitionKey", hasItems(MockProvider.EXAMPLE_DECISION_DEFINITION_KEY, MockProvider.ANOTHER_DECISION_DEFINITION_KEY))
          .body("evaluations", hasItems(1, 2))
        .when().get(HISTORIC_DECISION_STATISTICS_URL);

    verify(processEngine.getHistoryService()).createHistoricDecisionInstanceStatisticsQuery(MockProvider.EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID);
  }

  @Test
  void testHistoricDefinitionInstanceStatisticsRetrievalWithDefinitionInstance() {
    given().pathParam("id", MockProvider.EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID)
        .queryParam("decisionInstanceId", MockProvider.EXAMPLE_DECISION_INSTANCE_ID)
        .then().expect()
        .statusCode(Response.Status.OK.getStatusCode())
          .body("$.size()", is(2))
          .body("decisionDefinitionKey", hasItems(MockProvider.EXAMPLE_DECISION_DEFINITION_KEY, MockProvider.ANOTHER_DECISION_DEFINITION_KEY))
          .body("evaluations", hasItems(1, 2))
        .when().get(HISTORIC_DECISION_STATISTICS_URL);

    verify(processEngine.getHistoryService()).createHistoricDecisionInstanceStatisticsQuery(MockProvider.EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID);
    verify(historicDecisionInstanceStatisticsQuery).decisionInstanceId(MockProvider.EXAMPLE_DECISION_INSTANCE_ID);
  }
}

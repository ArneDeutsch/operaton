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
package org.operaton.bpm.qa.upgrade.batch;

import java.util.Date;
import org.operaton.bpm.engine.HistoryService;
import org.operaton.bpm.engine.ManagementService;
import org.operaton.bpm.engine.RepositoryService;
import org.operaton.bpm.engine.RuntimeService;
import org.operaton.bpm.engine.batch.Batch;
import org.operaton.bpm.engine.repository.ProcessDefinition;
import org.operaton.bpm.engine.test.Deployment;
import org.operaton.bpm.qa.upgrade.DescribesScenario;
import org.operaton.bpm.qa.upgrade.ScenarioSetup;

public class SetRemovalTimeToProcessInstanceScenario {

  private SetRemovalTimeToProcessInstanceScenario() {
  }

  @Deployment
  public static String deployOneTask() {
    return "org/operaton/bpm/qa/upgrade/batch/SetRemovalTimeToProcessInstanceScenario.oneTaskProcess.bpmn20.xml";
  }

  @DescribesScenario("createSetRetriesBatch")
  public static ScenarioSetup createSetRemovalTimeBatch() {
    return (engine, scenarioName) -> {
      // services
      RuntimeService runtimeService = engine.getRuntimeService();
      HistoryService historyService = engine.getHistoryService();
      RepositoryService repositoryService = engine.getRepositoryService();
      ManagementService managementService = engine.getManagementService();

      // definition
      ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
          .processDefinitionKey("createProcessForSetRemovalTimeBatch_719").singleResult();

      // create set removal time batch
      Date removalTime = new Date(1363609000000L);
      String processInstanceId = runtimeService.startProcessInstanceByKey(definition.getKey()).getId();
      Batch batch = historyService.setRemovalTimeToHistoricProcessInstances().absoluteRemovalTime(removalTime).byIds(processInstanceId).executeAsync();
      managementService.setProperty("SetRemovalTimeToProcessInstanceTest.batchId", batch.getId());
      managementService.setProperty("SetRemovalTimeToProcessInstanceTest.processInstanceId", processInstanceId);
    };
  }
}

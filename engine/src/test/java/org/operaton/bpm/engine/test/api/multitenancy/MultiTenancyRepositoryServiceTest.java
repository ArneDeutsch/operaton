/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.operaton.bpm.engine.test.api.multitenancy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.operaton.bpm.engine.RepositoryService;
import org.operaton.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.operaton.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.operaton.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.operaton.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.operaton.bpm.engine.impl.repository.ResourceDefinitionEntity;
import org.operaton.bpm.engine.repository.CaseDefinition;
import org.operaton.bpm.engine.repository.DecisionDefinition;
import org.operaton.bpm.engine.repository.Deployment;
import org.operaton.bpm.engine.repository.DeploymentBuilder;
import org.operaton.bpm.engine.repository.ProcessDefinition;
import org.operaton.bpm.engine.test.junit5.ProcessEngineExtension;
import org.operaton.bpm.engine.test.junit5.ProcessEngineTestExtension;
import org.operaton.bpm.model.bpmn.Bpmn;
import org.operaton.bpm.model.bpmn.BpmnModelInstance;

public class MultiTenancyRepositoryServiceTest {

  protected static final String TENANT_TWO = "tenant2";
  protected static final String TENANT_ONE = "tenant1";

  protected static final BpmnModelInstance emptyProcess = Bpmn.createExecutableProcess().startEvent().done();
  protected static final String CMMN = "org/operaton/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testSimpleDeployment.cmmn";
  protected static final String DMN = "org/operaton/bpm/engine/test/api/multitenancy/simpleDecisionTable.dmn";

  @RegisterExtension
  protected static ProcessEngineExtension engineRule = ProcessEngineExtension.builder().build();
  @RegisterExtension
  protected static ProcessEngineTestExtension testRule = new ProcessEngineTestExtension(engineRule);

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected RepositoryService repositoryService;

  @Test
  public void deploymentWithoutTenantId() {
    createDeploymentBuilder()
      .deploy();

    Deployment deployment = repositoryService
        .createDeploymentQuery()
        .singleResult();

    assertThat(deployment).isNotNull();
    assertThat(deployment.getTenantId()).isNull();
  }

  @Test
  public void deploymentWithTenantId() {
    createDeploymentBuilder()
      .tenantId(TENANT_ONE)
      .deploy();

    Deployment deployment = repositoryService
        .createDeploymentQuery()
        .singleResult();

    assertThat(deployment).isNotNull();
    assertThat(deployment.getTenantId()).isEqualTo(TENANT_ONE);
  }

  @Test
  public void processDefinitionVersionWithTenantId() {
    createDeploymentBuilder()
      .tenantId(TENANT_ONE)
      .deploy();

    createDeploymentBuilder()
      .tenantId(TENANT_ONE)
      .deploy();

    createDeploymentBuilder()
      .tenantId(TENANT_TWO)
      .deploy();

    List<ProcessDefinition> processDefinitions = repositoryService
        .createProcessDefinitionQuery()
        .orderByTenantId()
        .asc()
        .orderByProcessDefinitionVersion()
        .asc()
        .list();

    assertThat(processDefinitions).hasSize(3);
    // process definition was deployed twice for tenant one
    assertThat(processDefinitions.get(0).getVersion()).isEqualTo(1);
    assertThat(processDefinitions.get(1).getVersion()).isEqualTo(2);
    // process definition version of tenant two have to be independent from tenant one
    assertThat(processDefinitions.get(2).getVersion()).isEqualTo(1);
  }

  @Test
  public void deploymentWithDuplicateFilteringForSameTenant() {
    // given: a deployment with tenant ID
    createDeploymentBuilder()
      .enableDuplicateFiltering(false)
      .name("twice")
      .tenantId(TENANT_ONE)
      .deploy();

    // if the same process is deployed with the same tenant ID again
    createDeploymentBuilder()
      .enableDuplicateFiltering(false)
      .name("twice")
      .tenantId(TENANT_ONE)
      .deploy();

    // then it does not create a new deployment
    assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(1L);
  }

  @Test
  public void deploymentWithDuplicateFilteringForDifferentTenants() {
    // given: a deployment with tenant ID
    createDeploymentBuilder()
      .enableDuplicateFiltering(false)
      .name("twice")
      .tenantId(TENANT_ONE)
      .deploy();

    // if the same process is deployed with the another tenant ID
    createDeploymentBuilder()
      .enableDuplicateFiltering(false)
      .name("twice")
      .tenantId(TENANT_TWO)
      .deploy();

    // then a new deployment is created
    assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(2L);
  }

  @Test
  public void deploymentWithDuplicateFilteringIgnoreDeploymentForNoTenant() {
    // given: a deployment without tenant ID
    createDeploymentBuilder()
      .enableDuplicateFiltering(false)
      .name("twice")
      .deploy();

    // if the same process is deployed with tenant ID
    createDeploymentBuilder()
      .enableDuplicateFiltering(false)
      .name("twice")
      .tenantId(TENANT_ONE)
      .deploy();

    // then a new deployment is created
    assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(2L);
  }

  @Test
  public void deploymentWithDuplicateFilteringIgnoreDeploymentForTenant() {
    // given: a deployment with tenant ID
    createDeploymentBuilder()
      .enableDuplicateFiltering(false)
      .name("twice")
      .tenantId(TENANT_ONE)
      .deploy();

    // if the same process is deployed without tenant ID
    createDeploymentBuilder()
      .enableDuplicateFiltering(false)
      .name("twice")
      .deploy();

    // then a new deployment is created
    assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(2L);
  }

  @Test
  public void getPreviousProcessDefinitionWithTenantId() {
    testRule.deployForTenant(TENANT_ONE, emptyProcess);
    testRule.deployForTenant(TENANT_ONE, emptyProcess);
    testRule.deployForTenant(TENANT_ONE, emptyProcess);

    testRule.deployForTenant(TENANT_TWO, emptyProcess);
    testRule.deployForTenant(TENANT_TWO, emptyProcess);

    List<ProcessDefinition> latestProcessDefinitions = repositoryService.createProcessDefinitionQuery()
      .latestVersion()
      .orderByTenantId()
      .asc()
      .list();

    ProcessDefinitionEntity previousDefinitionTenantOne = getPreviousDefinition((ProcessDefinitionEntity) latestProcessDefinitions.get(0));
    ProcessDefinitionEntity previousDefinitionTenantTwo = getPreviousDefinition((ProcessDefinitionEntity) latestProcessDefinitions.get(1));

    assertThat(previousDefinitionTenantOne.getVersion()).isEqualTo(2);
    assertThat(previousDefinitionTenantOne.getTenantId()).isEqualTo(TENANT_ONE);

    assertThat(previousDefinitionTenantTwo.getVersion()).isEqualTo(1);
    assertThat(previousDefinitionTenantTwo.getTenantId()).isEqualTo(TENANT_TWO);
  }

  @Test
  public void getPreviousCaseDefinitionWithTenantId() {
    testRule.deployForTenant(TENANT_ONE, CMMN);
    testRule.deployForTenant(TENANT_ONE, CMMN);
    testRule.deployForTenant(TENANT_ONE, CMMN);

    testRule.deployForTenant(TENANT_TWO, CMMN);
    testRule.deployForTenant(TENANT_TWO, CMMN);

    List<CaseDefinition> latestCaseDefinitions = repositoryService.createCaseDefinitionQuery()
      .latestVersion()
      .orderByTenantId()
      .asc()
      .list();

    CaseDefinitionEntity previousDefinitionTenantOne = getPreviousDefinition((CaseDefinitionEntity) latestCaseDefinitions.get(0));
    CaseDefinitionEntity previousDefinitionTenantTwo = getPreviousDefinition((CaseDefinitionEntity) latestCaseDefinitions.get(1));

    assertThat(previousDefinitionTenantOne.getVersion()).isEqualTo(2);
    assertThat(previousDefinitionTenantOne.getTenantId()).isEqualTo(TENANT_ONE);

    assertThat(previousDefinitionTenantTwo.getVersion()).isEqualTo(1);
    assertThat(previousDefinitionTenantTwo.getTenantId()).isEqualTo(TENANT_TWO);
  }

  @Test
  public void getPreviousDecisionDefinitionWithTenantId() {
    testRule.deployForTenant(TENANT_ONE, DMN);
    testRule.deployForTenant(TENANT_ONE, DMN);
    testRule.deployForTenant(TENANT_ONE, DMN);

    testRule.deployForTenant(TENANT_TWO, DMN);
    testRule.deployForTenant(TENANT_TWO, DMN);

    List<DecisionDefinition> latestDefinitions = repositoryService.createDecisionDefinitionQuery()
      .latestVersion()
      .orderByTenantId()
      .asc()
      .list();

    DecisionDefinitionEntity previousDefinitionTenantOne = getPreviousDefinition((DecisionDefinitionEntity) latestDefinitions.get(0));
    DecisionDefinitionEntity previousDefinitionTenantTwo = getPreviousDefinition((DecisionDefinitionEntity) latestDefinitions.get(1));

    assertThat(previousDefinitionTenantOne.getVersion()).isEqualTo(2);
    assertThat(previousDefinitionTenantOne.getTenantId()).isEqualTo(TENANT_ONE);

    assertThat(previousDefinitionTenantTwo.getVersion()).isEqualTo(1);
    assertThat(previousDefinitionTenantTwo.getTenantId()).isEqualTo(TENANT_TWO);
  }

  protected <T extends ResourceDefinitionEntity> T getPreviousDefinition(final T definitionEntity) {
    return ((ProcessEngineConfigurationImpl) processEngineConfiguration).getCommandExecutorTxRequired().execute(commandContext -> (T) definitionEntity.getPreviousDefinition());
  }

  protected DeploymentBuilder createDeploymentBuilder() {
    return repositoryService
        .createDeployment()
        .addModelInstance("testProcess.bpmn", emptyProcess);
  }

  @AfterEach
  public void tearDown() {
    for(Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

}

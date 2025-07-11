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
package org.operaton.bpm.engine.test.concurrency;

import org.operaton.bpm.engine.impl.interceptor.CommandContext;
import org.operaton.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.operaton.bpm.engine.repository.ProcessDefinition;
import org.operaton.bpm.engine.runtime.EventSubscription;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeleteProcessDefinitionTest extends ConcurrencyTestCase {

  @AfterEach
  void tearDown() {
    repositoryService.createDeploymentQuery().list().forEach(deployment -> repositoryService.deleteDeployment(deployment.getId(), true));
    processEngineConfiguration.getDeploymentCache().purgeCache();
  }

  @Test
  void testDeploymentOfProcessDefinitionWithOrphanMessageEvent() {
    // given
    String resource = "org/operaton/bpm/engine/test/api/repository/processWithNewInvoiceMessage.bpmn20.xml";
    List<ProcessDefinition> processDefinitions = deployProcessDefinitionTwice(resource);
    assertThat(processDefinitions).hasSize(2);
    deleteProcessDefinitionsSimultaneously(processDefinitions.get(0).getId(), processDefinitions.get(1).getId());

    assertThat(repositoryService.createProcessDefinitionQuery().list()).isEmpty();
    assertThat(runtimeService.createEventSubscriptionQuery().count()).isEqualTo(1);

    // when
    repositoryService.createDeployment().addClasspathResource(resource).deploy();

    // then
    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(1);
    assertThat(runtimeService.createEventSubscriptionQuery().count()).isEqualTo(1);
  }

  @Test
  void testDeploymentOfProcessDefinitionWithOrphanJob() {
    // given
    String resource = "org/operaton/bpm/engine/test/bpmn/event/timer/StartTimerEventTest.testTimeCycle.bpmn20.xml";
    List<ProcessDefinition> processDefinitions = deployProcessDefinitionTwice(resource);
    assertThat(processDefinitions).hasSize(2);
    deleteProcessDefinitionsSimultaneously(processDefinitions.get(0).getId(), processDefinitions.get(1).getId());

    assertThat(repositoryService.createProcessDefinitionQuery().list()).isEmpty();
    assertThat(managementService.createJobQuery().count()).isEqualTo(1);

    // when
    repositoryService.createDeployment().addClasspathResource(resource).deploy();

    // then
    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(1);
    assertThat(managementService.createJobQuery().count()).isEqualTo(1);
  }

  @Test
  void testDeploymentOfProcessDefinitionWithOrphanSignalEvent() {
    // given
    String resource = "org/operaton/bpm/engine/test/api/repository/processWithStartSignalEvent.bpmn20.xml";
    List<ProcessDefinition> processDefinitions = deployProcessDefinitionTwice(resource);
    assertThat(processDefinitions).hasSize(2);
    deleteProcessDefinitionsSimultaneously(processDefinitions.get(0).getId(), processDefinitions.get(1).getId());

    assertThat(repositoryService.createProcessDefinitionQuery().list()).isEmpty();
    assertThat(runtimeService.createEventSubscriptionQuery().count()).isEqualTo(1);

    // when
    repositoryService.createDeployment().addClasspathResource(resource).deploy();

    // then
    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(1);
    assertThat(runtimeService.createEventSubscriptionQuery().count()).isEqualTo(1);
  }

  @Test
  void testDeploymentOfProcessDefinitionWithOrphanEventAndPreviousVersion() {
    // given
    String resource = "org/operaton/bpm/engine/test/api/repository/processWithNewInvoiceMessage.bpmn20.xml";
    repositoryService.createDeployment().addClasspathResource(resource).deploy();
    repositoryService.createDeployment().addClasspathResource(resource).deploy();
    repositoryService.createDeployment().addClasspathResource(resource).deploy();
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().processDefinitionKey("otherMessageProcess").list();
    assertThat(processDefinitions).hasSize(3);

    deleteProcessDefinitionsSimultaneously(processDefinitions.get(1).getId(), processDefinitions.get(2).getId());

    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(1);
    assertThat(runtimeService.createEventSubscriptionQuery().count()).isEqualTo(1);

    // when
    repositoryService.createDeployment().addClasspathResource(resource).deploy();

    // then
    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(2);
    assertThat(runtimeService.createEventSubscriptionQuery().count()).isEqualTo(1);
  }

  @Test
  void testDeploymentOfProcessDefinitionWithOrphanConditionalEvent() {
    // given
    String resource = "org/operaton/bpm/engine/test/api/repository/processWithConditionalStartEvent.bpmn20.xml";
    List<ProcessDefinition> definitions = deployProcessDefinitionTwice(resource);
    deleteProcessDefinitionsSimultaneously(definitions.get(0).getId(), definitions.get(1).getId());
    // at this point we have one orphan subscription in the database

    // when
    repositoryService.createDeployment().addClasspathResource(resource).deploy();

    // then
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    EventSubscription subscription = runtimeService.createEventSubscriptionQuery().singleResult();
    assertThat(((EventSubscriptionEntity) subscription).getConfiguration()).isEqualTo(processDefinition.getId());
  }

  protected static class ControllableDeleteProcessDefinitionCommand extends ControllableCommand<Void> {

    protected String processDefinitionId;
    protected Exception exception;

    public ControllableDeleteProcessDefinitionCommand(String processDefinitionId) {
      this.processDefinitionId = processDefinitionId;
    }

    @Override
    public Void execute(CommandContext commandContext) {
      monitor.sync(); // thread will block here until makeContinue() is called from main thread
      commandContext.getProcessEngineConfiguration().getRepositoryService().deleteProcessDefinition(processDefinitionId);
      monitor.sync(); // thread will block here until waitUntilDone() is called form main thread
      return null;
    }
  }

  protected void deleteProcessDefinitionsSimultaneously(String id1, String id2) {
    ThreadControl thread1 = executeControllableCommand(new ControllableDeleteProcessDefinitionCommand(id1));
    thread1.reportInterrupts();
    thread1.waitForSync();

    ThreadControl thread2 = executeControllableCommand(new ControllableDeleteProcessDefinitionCommand(id2));
    thread2.reportInterrupts();
    thread2.waitForSync();

    // delete process definition version 1 without committing
    thread1.makeContinue();
    thread1.waitForSync();

    // delete process definition version 2 without committing
    thread2.makeContinue();
    thread2.waitForSync();

    // commit deletion on version 1
    thread1.makeContinue();
    thread1.waitUntilDone();

    // commit deletion on version 2
    thread2.makeContinue();
    thread2.waitUntilDone();
  }

  protected List<ProcessDefinition> deployProcessDefinitionTwice(String resource) {
    repositoryService.createDeployment().addClasspathResource(resource).deploy();
    repositoryService.createDeployment().addClasspathResource(resource).deploy();
    return repositoryService.createProcessDefinitionQuery().list();
  }
}

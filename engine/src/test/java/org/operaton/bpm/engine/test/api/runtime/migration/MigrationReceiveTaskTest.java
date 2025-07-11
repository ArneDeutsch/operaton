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
package org.operaton.bpm.engine.test.api.runtime.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.operaton.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.operaton.bpm.engine.migration.MigrationPlan;
import org.operaton.bpm.engine.repository.ProcessDefinition;
import org.operaton.bpm.engine.runtime.EventSubscription;
import org.operaton.bpm.engine.runtime.ProcessInstance;
import org.operaton.bpm.engine.test.api.runtime.migration.models.MessageReceiveModels;
import org.operaton.bpm.engine.test.junit5.ProcessEngineExtension;
import org.operaton.bpm.engine.test.junit5.migration.MigrationTestExtension;
import org.operaton.bpm.model.bpmn.BpmnModelInstance;

/**
 * @author Thorben Lindhauer
 *
 */
class MigrationReceiveTaskTest {

  @RegisterExtension
  static ProcessEngineExtension rule = ProcessEngineExtension.builder().build();
  @RegisterExtension
  MigrationTestExtension testHelper = new MigrationTestExtension(rule);

  @Test
  void testCannotMigrateActivityInstance() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(MessageReceiveModels.ONE_RECEIVE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(MessageReceiveModels.ONE_RECEIVE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("receiveTask", "receiveTask")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    testHelper.assertEventSubscriptionMigrated("receiveTask", "receiveTask", MessageReceiveModels.MESSAGE_NAME);

    // and it is possible to trigger the receive task
    rule.getRuntimeService().correlateMessage(MessageReceiveModels.MESSAGE_NAME);

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  void testMigrateEventSubscriptionProperties() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(MessageReceiveModels.ONE_RECEIVE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(MessageReceiveModels.ONE_RECEIVE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("receiveTask", "receiveTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    EventSubscription eventSubscriptionBefore = testHelper.snapshotBeforeMigration.getEventSubscriptions().get(0);

    List<EventSubscription> eventSubscriptionsAfter = testHelper.snapshotAfterMigration.getEventSubscriptions();
    assertThat(eventSubscriptionsAfter).hasSize(1);
    EventSubscription eventSubscriptionAfter = eventSubscriptionsAfter.get(0);
    assertThat(eventSubscriptionAfter.getCreated()).isEqualTo(eventSubscriptionBefore.getCreated());
    assertThat(eventSubscriptionAfter.getExecutionId()).isEqualTo(eventSubscriptionBefore.getExecutionId());
    assertThat(eventSubscriptionAfter.getProcessInstanceId()).isEqualTo(eventSubscriptionBefore.getProcessInstanceId());
  }

  @Test
  void testMigrateEventSubscriptionChangeActivityId() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(MessageReceiveModels.ONE_RECEIVE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(modify(MessageReceiveModels.ONE_RECEIVE_TASK_PROCESS)
        .changeElementId("receiveTask", "newReceiveTask"));

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("receiveTask", "newReceiveTask")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    testHelper.assertEventSubscriptionMigrated("receiveTask", "newReceiveTask", MessageReceiveModels.MESSAGE_NAME);

    // and it is possible to trigger the receive task
    rule.getRuntimeService().correlateMessage(MessageReceiveModels.MESSAGE_NAME);

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  void testMigrateEventSubscriptionPreserveMessageName() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(MessageReceiveModels.ONE_RECEIVE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(modify(MessageReceiveModels.ONE_RECEIVE_TASK_PROCESS)
        .renameMessage(MessageReceiveModels.MESSAGE_NAME, "new" + MessageReceiveModels.MESSAGE_NAME));

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("receiveTask", "receiveTask")
        .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then the message event subscription's event name has not changed
    testHelper.assertEventSubscriptionMigrated("receiveTask", "receiveTask", MessageReceiveModels.MESSAGE_NAME);

    // and it is possible to trigger the receive task
    rule.getRuntimeService().correlateMessage(MessageReceiveModels.MESSAGE_NAME);

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  void testMigrateEventSubscriptionUpdateMessageName() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(MessageReceiveModels.ONE_RECEIVE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(modify(MessageReceiveModels.ONE_RECEIVE_TASK_PROCESS)
      .renameMessage(MessageReceiveModels.MESSAGE_NAME, "new" + MessageReceiveModels.MESSAGE_NAME));

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("receiveTask", "receiveTask")
          .updateEventTrigger()
        .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then the message event subscription's event name has not changed
    testHelper.assertEventSubscriptionMigrated(
        "receiveTask", MessageReceiveModels.MESSAGE_NAME,
        "receiveTask", "new" + MessageReceiveModels.MESSAGE_NAME);

    // and it is possible to trigger the event
    rule.getRuntimeService().correlateMessage("new" + MessageReceiveModels.MESSAGE_NAME);

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  void testMigrateParallelMultiInstanceEventSubscription() {
    BpmnModelInstance parallelMiReceiveTaskProcess = modify(MessageReceiveModels.ONE_RECEIVE_TASK_PROCESS)
      .activityBuilder("receiveTask")
        .multiInstance()
        .parallel()
        .cardinality("3")
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(parallelMiReceiveTaskProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(parallelMiReceiveTaskProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("receiveTask#multiInstanceBody", "receiveTask#multiInstanceBody")
      .mapActivities("receiveTask", "receiveTask")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    testHelper.assertEventSubscriptionsMigrated("receiveTask", "receiveTask", MessageReceiveModels.MESSAGE_NAME);

    // and it is possible to trigger the receive tasks
    rule.getRuntimeService().createMessageCorrelation(MessageReceiveModels.MESSAGE_NAME).correlateAll();

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  void testMigrateSequentialMultiInstanceEventSubscription() {
    BpmnModelInstance parallelMiReceiveTaskProcess = modify(MessageReceiveModels.ONE_RECEIVE_TASK_PROCESS)
      .activityBuilder("receiveTask")
        .multiInstance()
        .sequential()
        .cardinality("3")
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(parallelMiReceiveTaskProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(parallelMiReceiveTaskProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("receiveTask#multiInstanceBody", "receiveTask#multiInstanceBody")
      .mapActivities("receiveTask", "receiveTask")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    testHelper.assertEventSubscriptionsMigrated("receiveTask", "receiveTask", MessageReceiveModels.MESSAGE_NAME);

    // and it is possible to trigger the receive tasks
    for (int i = 0; i < 3; i++) {
      rule.getRuntimeService().correlateMessage(MessageReceiveModels.MESSAGE_NAME);
    }

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  void testMigrateEventSubscriptionAddParentScope() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(MessageReceiveModels.ONE_RECEIVE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(MessageReceiveModels.SUBPROCESS_RECEIVE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("receiveTask", "receiveTask")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    testHelper.assertEventSubscriptionMigrated("receiveTask", "receiveTask", MessageReceiveModels.MESSAGE_NAME);

    // and it is possible to trigger the receive task
    rule.getRuntimeService().correlateMessage(MessageReceiveModels.MESSAGE_NAME);

    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }
}

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

import static org.operaton.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.operaton.bpm.engine.test.api.runtime.migration.models.ConditionalModels.BOUNDARY_ID;
import static org.operaton.bpm.engine.test.api.runtime.migration.models.ConditionalModels.CONDITIONAL_PROCESS_KEY;
import static org.operaton.bpm.engine.test.api.runtime.migration.models.ConditionalModels.CONDITION_ID;
import static org.operaton.bpm.engine.test.api.runtime.migration.models.EventSubProcessModels.EVENT_SUB_PROCESS_ID;
import static org.operaton.bpm.engine.test.api.runtime.migration.models.EventSubProcessModels.EVENT_SUB_PROCESS_START_ID;
import static org.operaton.bpm.engine.test.api.runtime.migration.models.EventSubProcessModels.EVENT_SUB_PROCESS_TASK_ID;
import static org.operaton.bpm.engine.test.api.runtime.migration.models.EventSubProcessModels.USER_TASK_ID;
import static org.operaton.bpm.engine.test.api.runtime.migration.models.EventSubProcessModels.VAR_CONDITION;
import static org.operaton.bpm.engine.test.util.MigrationPlanAssert.assertThat;
import static org.operaton.bpm.engine.test.util.MigrationPlanAssert.migrate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.operaton.bpm.engine.migration.MigrationInstructionsBuilder;
import org.operaton.bpm.engine.migration.MigrationPlan;
import org.operaton.bpm.engine.repository.ProcessDefinition;
import org.operaton.bpm.engine.test.api.runtime.migration.models.CallActivityModels;
import org.operaton.bpm.engine.test.api.runtime.migration.models.CompensationModels;
import org.operaton.bpm.engine.test.api.runtime.migration.models.EventBasedGatewayModels;
import org.operaton.bpm.engine.test.api.runtime.migration.models.EventSubProcessModels;
import org.operaton.bpm.engine.test.api.runtime.migration.models.ExternalTaskModels;
import org.operaton.bpm.engine.test.api.runtime.migration.models.GatewayModels;
import org.operaton.bpm.engine.test.api.runtime.migration.models.MessageReceiveModels;
import org.operaton.bpm.engine.test.api.runtime.migration.models.MultiInstanceProcessModels;
import org.operaton.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.operaton.bpm.engine.test.api.runtime.migration.models.ServiceTaskModels;
import org.operaton.bpm.engine.test.api.runtime.migration.models.TransactionModels;
import org.operaton.bpm.engine.test.junit5.ProcessEngineExtension;
import org.operaton.bpm.engine.test.junit5.migration.MigrationTestExtension;
import org.operaton.bpm.engine.test.util.MigrationPlanAssert;
import org.operaton.bpm.model.bpmn.Bpmn;
import org.operaton.bpm.model.bpmn.BpmnModelInstance;
import org.operaton.bpm.model.bpmn.instance.UserTask;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationPlanGenerationTest {

  public static final String MESSAGE_NAME = "Message";
  public static final String SIGNAL_NAME = "Signal";
  public static final String TIMER_DATE = "2016-02-11T12:13:14Z";
  public static final String ERROR_CODE = "Error";
  public static final String ESCALATION_CODE = "Escalation";

  @RegisterExtension
  static ProcessEngineExtension rule = ProcessEngineExtension.builder().build();
  @RegisterExtension
  MigrationTestExtension testHelper = new MigrationTestExtension(rule);

  @Test
  void testMapEqualActivitiesInProcessDefinitionScope() {
    BpmnModelInstance sourceProcess = ProcessModels.ONE_TASK_PROCESS;
    BpmnModelInstance targetProcess = ProcessModels.ONE_TASK_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("userTask").to("userTask")
      );
  }

  @Test
  void testMapEqualActivitiesInSameSubProcessScope() {
    BpmnModelInstance sourceProcess = ProcessModels.SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = ProcessModels.SUBPROCESS_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("subProcess").to("subProcess"),
        migrate("userTask").to("userTask")
      );

  }

  @Test
  void testMapEqualActivitiesToSubProcessScope() {
    BpmnModelInstance sourceProcess = ProcessModels.ONE_TASK_PROCESS;
    BpmnModelInstance targetProcess = ProcessModels.SUBPROCESS_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasEmptyInstructions();
  }

  @Test
  void testMapEqualActivitiesToNestedSubProcessScope() {
    BpmnModelInstance sourceProcess = ProcessModels.SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = modify(ProcessModels.DOUBLE_SUBPROCESS_PROCESS)
      .changeElementId("outerSubProcess", "subProcess"); // make ID match with subprocess ID of source definition

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("subProcess").to("subProcess")
      );
  }

  @Test
  void testMapEqualActivitiesToSurroundingSubProcessScope() {
    BpmnModelInstance sourceProcess = ProcessModels.SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = modify(ProcessModels.DOUBLE_SUBPROCESS_PROCESS)
      .changeElementId("innerSubProcess", "subProcess"); // make ID match with subprocess ID of source definition

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasEmptyInstructions();
  }

  @Test
  void testMapEqualActivitiesToDeeplyNestedSubProcessScope() {
    BpmnModelInstance sourceProcess = ProcessModels.ONE_TASK_PROCESS;
    BpmnModelInstance targetProcess = ProcessModels.DOUBLE_SUBPROCESS_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasEmptyInstructions();
  }

  @Test
  void testMapEqualActivitiesToSiblingScope() {
    BpmnModelInstance sourceProcess = ProcessModels.PARALLEL_SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = modify(ProcessModels.PARALLEL_SUBPROCESS_PROCESS)
      .swapElementIds("userTask1", "userTask2");

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("subProcess1").to("subProcess1"),
        migrate("subProcess2").to("subProcess2"),
        migrate("fork").to("fork")
      );
  }

  @Test
  void testMapEqualActivitiesToNestedSiblingScope() {
    BpmnModelInstance sourceProcess = ProcessModels.PARALLEL_DOUBLE_SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = modify(ProcessModels.PARALLEL_DOUBLE_SUBPROCESS_PROCESS)
      .swapElementIds("userTask1", "userTask2");

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("subProcess1").to("subProcess1"),
        migrate("nestedSubProcess1").to("nestedSubProcess1"),
        migrate("subProcess2").to("subProcess2"),
        migrate("nestedSubProcess2").to("nestedSubProcess2"),
        migrate("fork").to("fork")
      );
  }

  @Test
  void testMapEqualActivitiesWhichBecomeScope() {
    BpmnModelInstance sourceProcess = ProcessModels.ONE_TASK_PROCESS;
    BpmnModelInstance targetProcess = ProcessModels.SCOPE_TASK_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("userTask").to("userTask")
      );
  }

  @Test
  void testMapEqualActivitiesWithParallelMultiInstance() {
    BpmnModelInstance testProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .<UserTask>getModelElementById("userTask").builder()
        .multiInstance().parallel().cardinality("3").multiInstanceDone().done();

    assertGeneratedMigrationPlan(testProcess, testProcess)
      .hasInstructions(
        migrate("userTask").to("userTask"),
        migrate("userTask#multiInstanceBody").to("userTask#multiInstanceBody"));
  }

  @Test
  void testMapEqualActivitiesIgnoreUnsupportedActivities() {
    BpmnModelInstance sourceProcess = ProcessModels.UNSUPPORTED_ACTIVITIES;
    BpmnModelInstance targetProcess = ProcessModels.UNSUPPORTED_ACTIVITIES;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasEmptyInstructions();
  }

  @Test
  void testMapEqualUnsupportedAsyncBeforeActivities() {
    BpmnModelInstance testModel = modify(ProcessModels.UNSUPPORTED_ACTIVITIES)
      .flowNodeBuilder("startEvent").operatonAsyncBefore()
      .moveToNode("decisionTask").operatonAsyncBefore()
      .moveToNode("throwEvent").operatonAsyncAfter()
      .moveToNode("serviceTask").operatonAsyncBefore()
      .moveToNode("sendTask").operatonAsyncBefore()
      .moveToNode("scriptTask").operatonAsyncBefore()
      .moveToNode("endEvent").operatonAsyncBefore()
      .done();

    assertGeneratedMigrationPlan(testModel, testModel)
      .hasInstructions(
        migrate("startEvent").to("startEvent"),
        migrate("decisionTask").to("decisionTask"),
        migrate("throwEvent").to("throwEvent"),
        migrate("serviceTask").to("serviceTask"),
        migrate("sendTask").to("sendTask"),
        migrate("scriptTask").to("scriptTask"),
        migrate("endEvent").to("endEvent")
      );
  }

  @Test
  void testMapEqualUnsupportedAsyncAfterActivities() {
    BpmnModelInstance testModel = modify(ProcessModels.UNSUPPORTED_ACTIVITIES)
      .flowNodeBuilder("startEvent").operatonAsyncAfter()
      .moveToNode("decisionTask").operatonAsyncAfter()
      .moveToNode("throwEvent").operatonAsyncAfter()
      .moveToNode("serviceTask").operatonAsyncAfter()
      .moveToNode("sendTask").operatonAsyncAfter()
      .moveToNode("scriptTask").operatonAsyncAfter()
      .moveToNode("endEvent").operatonAsyncAfter()
      .done();

    assertGeneratedMigrationPlan(testModel, testModel)
      .hasInstructions(
        migrate("startEvent").to("startEvent"),
        migrate("decisionTask").to("decisionTask"),
        migrate("throwEvent").to("throwEvent"),
        migrate("serviceTask").to("serviceTask"),
        migrate("sendTask").to("sendTask"),
        migrate("scriptTask").to("scriptTask"),
        migrate("endEvent").to("endEvent")
      );
  }

  @Test
  void testMapEqualActivitiesToParentScope() {
    BpmnModelInstance sourceProcess = modify(ProcessModels.DOUBLE_SUBPROCESS_PROCESS)
      .changeElementId("outerSubProcess", "subProcess");
    BpmnModelInstance targetProcess = ProcessModels.SUBPROCESS_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("subProcess").to("subProcess")
      );
  }

  @Test
  void testMapEqualActivitiesFromScopeToProcessDefinition() {
    BpmnModelInstance sourceProcess = ProcessModels.SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = ProcessModels.ONE_TASK_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasEmptyInstructions();
  }

  @Test
  void testMapEqualActivitiesFromDoubleScopeToProcessDefinition() {
    BpmnModelInstance sourceProcess = ProcessModels.DOUBLE_SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = ProcessModels.ONE_TASK_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasEmptyInstructions();
  }

  @Test
  void testMapEqualActivitiesFromTripleScopeToProcessDefinition() {
    BpmnModelInstance sourceProcess = ProcessModels.TRIPLE_SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = ProcessModels.ONE_TASK_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasEmptyInstructions();
  }

  @Test
  void testMapEqualActivitiesFromTripleScopeToSingleNewScope() {
    BpmnModelInstance sourceProcess = ProcessModels.TRIPLE_SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = ProcessModels.SUBPROCESS_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasEmptyInstructions();
  }

  @Test
  void testMapEqualActivitiesFromTripleScopeToTwoNewScopes() {
    BpmnModelInstance sourceProcess = ProcessModels.TRIPLE_SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = ProcessModels.DOUBLE_SUBPROCESS_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasEmptyInstructions();
  }

  @Test
  void testMapEqualActivitiesToNewScopes() {
    BpmnModelInstance sourceProcess = ProcessModels.DOUBLE_SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = modify(ProcessModels.DOUBLE_SUBPROCESS_PROCESS)
      .changeElementId("outerSubProcess", "newOuterSubProcess")
      .changeElementId("innerSubProcess", "newInnerSubProcess");

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasEmptyInstructions();
  }

  @Test
  void testMapEqualActivitiesOutsideOfScope() {
    BpmnModelInstance sourceProcess = ProcessModels.PARALLEL_GATEWAY_SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = ProcessModels.PARALLEL_TASK_AND_SUBPROCESS_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("subProcess").to("subProcess"),
        migrate("userTask1").to("userTask1")
      );
  }

  @Test
  void testMapEqualActivitiesToHorizontalScope() {
    BpmnModelInstance sourceProcess = ProcessModels.PARALLEL_TASK_AND_SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = ProcessModels.PARALLEL_GATEWAY_SUBPROCESS_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("subProcess").to("subProcess"),
        migrate("userTask1").to("userTask1")
      );
  }

  @Test
  void testMapEqualActivitiesFromTaskWithBoundaryEvent() {
    BpmnModelInstance sourceProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent(null).message("Message")
      .done();
    BpmnModelInstance targetProcess = ProcessModels.ONE_TASK_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("userTask").to("userTask")
      );
  }

  @Test
  void testMapEqualActivitiesToTaskWithBoundaryEvent() {
    BpmnModelInstance sourceProcess = ProcessModels.ONE_TASK_PROCESS;
    BpmnModelInstance targetProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent(null).message("Message")
      .done();

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("userTask").to("userTask")
      );
  }

  @Test
  void testMapEqualActivitiesWithBoundaryEvent() {
    BpmnModelInstance testProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
        .boundaryEvent("messageBoundary").message(MESSAGE_NAME)
      .moveToActivity("userTask")
        .boundaryEvent("signalBoundary").signal(SIGNAL_NAME)
      .moveToActivity("userTask")
        .boundaryEvent("timerBoundary").timerWithDate(TIMER_DATE)
      .done();

    assertGeneratedMigrationPlan(testProcess, testProcess)
      .hasInstructions(
        migrate("subProcess").to("subProcess"),
        migrate("messageBoundary").to("messageBoundary"),
        migrate("userTask").to("userTask"),
        migrate("signalBoundary").to("signalBoundary"),
        migrate("timerBoundary").to("timerBoundary")
      );
  }

  @Test
  void testNotMapBoundaryEventsWithDifferentIds() {
    BpmnModelInstance sourceProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("message").message(MESSAGE_NAME)
      .done();
    BpmnModelInstance targetProcess = modify(sourceProcess)
      .changeElementId("message", "newMessage");

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("userTask").to("userTask")
      );
  }

  @Test
  void testIgnoreNotSupportedBoundaryEvents() {
    BpmnModelInstance testProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
        .boundaryEvent("messageBoundary").message(MESSAGE_NAME)
      .moveToActivity("subProcess")
        .boundaryEvent("errorBoundary").error(ERROR_CODE)
      .moveToActivity("subProcess")
        .boundaryEvent("escalationBoundary").escalation(ESCALATION_CODE)
      .moveToActivity("userTask")
        .boundaryEvent("signalBoundary").signal(SIGNAL_NAME)
      .done();

    assertGeneratedMigrationPlan(testProcess, testProcess)
      .hasInstructions(
        migrate("subProcess").to("subProcess"),
        migrate("messageBoundary").to("messageBoundary"),
        migrate("userTask").to("userTask"),
        migrate("signalBoundary").to("signalBoundary")
      );
  }

  @Test
  void testNotMigrateBoundaryToParallelActivity() {
    BpmnModelInstance sourceProcess = modify(ProcessModels.PARALLEL_GATEWAY_PROCESS)
      .activityBuilder("userTask1")
        .boundaryEvent("message").message(MESSAGE_NAME)
      .done();
    BpmnModelInstance targetProcess = modify(ProcessModels.PARALLEL_GATEWAY_PROCESS)
      .activityBuilder("userTask2")
        .boundaryEvent("message").message(MESSAGE_NAME)
      .done();

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("userTask1").to("userTask1"),
        migrate("userTask2").to("userTask2"),
        migrate("fork").to("fork")
      );
  }

  @Test
  void testNotMigrateBoundaryToChildActivity() {
    BpmnModelInstance sourceProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
        .boundaryEvent("message").message(MESSAGE_NAME)
      .done();
    BpmnModelInstance targetProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("message").message(MESSAGE_NAME)
      .done();

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("subProcess").to("subProcess"),
        migrate("userTask").to("userTask")
      );
  }

  @Test
  void testMigrateProcessInstanceWithEventSubProcess() {
    BpmnModelInstance testProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .addSubProcessTo(ProcessModels.PROCESS_KEY)
      .id("eventSubProcess")
      .triggerByEvent()
      .embeddedSubProcess()
        .startEvent("eventSubProcessStart").message(MESSAGE_NAME)
        .endEvent()
      .subProcessDone()
      .done();

    assertGeneratedMigrationPlan(testProcess, testProcess)
      .hasInstructions(
        migrate("eventSubProcess").to("eventSubProcess"),
        migrate("eventSubProcessStart").to("eventSubProcessStart"),
        migrate("userTask").to("userTask")
      );

    assertGeneratedMigrationPlan(testProcess, ProcessModels.ONE_TASK_PROCESS)
      .hasInstructions(
        migrate("userTask").to("userTask")
      );

    assertGeneratedMigrationPlan(ProcessModels.ONE_TASK_PROCESS, testProcess)
      .hasInstructions(
        migrate("userTask").to("userTask")
      );
  }

  @Test
  void testMigrateSubProcessWithEventSubProcess() {
    BpmnModelInstance testProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .addSubProcessTo("subProcess")
      .id("eventSubProcess")
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent("eventSubProcessStart").message(MESSAGE_NAME)
      .endEvent()
      .subProcessDone()
      .done();

    assertGeneratedMigrationPlan(testProcess, testProcess)
      .hasInstructions(
        migrate("eventSubProcess").to("eventSubProcess"),
        migrate("eventSubProcessStart").to("eventSubProcessStart"),
        migrate("subProcess").to("subProcess"),
        migrate("userTask").to("userTask")
      );

    assertGeneratedMigrationPlan(testProcess, ProcessModels.SUBPROCESS_PROCESS)
      .hasInstructions(
        migrate("subProcess").to("subProcess"),
        migrate("userTask").to("userTask")
      );

    assertGeneratedMigrationPlan(ProcessModels.SUBPROCESS_PROCESS, testProcess)
      .hasInstructions(
        migrate("subProcess").to("subProcess"),
        migrate("userTask").to("userTask")
      );

  }

  @Test
  void testMigrateUserTaskInEventSubProcess() {
    BpmnModelInstance testProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .addSubProcessTo("subProcess")
      .id("eventSubProcess")
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent("eventSubProcessStart").message(MESSAGE_NAME)
      .userTask("innerTask")
      .endEvent()
      .subProcessDone()
      .done();

    assertGeneratedMigrationPlan(testProcess, testProcess)
      .hasInstructions(
          migrate("eventSubProcess").to("eventSubProcess"),
          migrate("eventSubProcessStart").to("eventSubProcessStart"),
          migrate("innerTask").to("innerTask"),
          migrate("subProcess").to("subProcess"),
          migrate("userTask").to("userTask")
      );

    assertGeneratedMigrationPlan(testProcess, ProcessModels.SUBPROCESS_PROCESS)
      .hasInstructions(
          migrate("subProcess").to("subProcess"),
          migrate("userTask").to("userTask")
      );

    assertGeneratedMigrationPlan(ProcessModels.SUBPROCESS_PROCESS, testProcess)
    .hasInstructions(
        migrate("subProcess").to("subProcess"),
        migrate("userTask").to("userTask")
    );

  }

  @Test
  void testNotMigrateActivitiesOfDifferentType() {
    BpmnModelInstance sourceProcess = ProcessModels.ONE_TASK_PROCESS;
    BpmnModelInstance targetProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .swapElementIds("userTask", "subProcess");

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasEmptyInstructions();
  }

  @Test
  void testNotMigrateBoundaryEventsOfDifferentType() {
    BpmnModelInstance sourceProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
      .done();
    BpmnModelInstance targetProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("boundary").signal(SIGNAL_NAME)
      .done();

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("userTask").to("userTask")
      );
  }

  @Test
  void testNotMigrateMultiInstanceOfDifferentType() {
    BpmnModelInstance sourceProcess = MultiInstanceProcessModels.SEQ_MI_ONE_TASK_PROCESS;
    BpmnModelInstance targetProcess = MultiInstanceProcessModels.PAR_MI_ONE_TASK_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasEmptyInstructions();
  }

  @Test
  void testNotMigrateBoundaryEventsWithInvalidEventScopeInstruction() {
    BpmnModelInstance sourceProcess = modify(ProcessModels.ONE_TASK_PROCESS)
        .activityBuilder("userTask")
        .boundaryEvent("boundary")
        .message("foo")
        .done();
    BpmnModelInstance targetProcess = modify(ProcessModels.ONE_RECEIVE_TASK_PROCESS)
        .changeElementId("receiveTask", "userTask")
        .activityBuilder("userTask")
        .boundaryEvent("boundary")
        .message("foo")
        .done();

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasEmptyInstructions();
  }

  @Test
  void testMapReceiveTasks() {
    assertGeneratedMigrationPlan(MessageReceiveModels.ONE_RECEIVE_TASK_PROCESS, MessageReceiveModels.ONE_RECEIVE_TASK_PROCESS)
      .hasInstructions(
        migrate("receiveTask").to("receiveTask"),
        migrate("userTask").to("userTask"));
  }

  @Test
  void testMapMessageCatchEvents() {
    assertGeneratedMigrationPlan(MessageReceiveModels.ONE_MESSAGE_CATCH_PROCESS, MessageReceiveModels.ONE_MESSAGE_CATCH_PROCESS)
      .hasInstructions(
        migrate("messageCatch").to("messageCatch"),
        migrate("userTask").to("userTask"));
  }

  @Test
  void testMapCallActivitiesToBpmnTest() {
    assertGeneratedMigrationPlan(CallActivityModels.oneBpmnCallActivityProcess("foo"), CallActivityModels.oneBpmnCallActivityProcess("foo"))
      .hasInstructions(
        migrate("callActivity").to("callActivity"),
        migrate("userTask").to("userTask"));
  }

  @Test
  void testMapCallActivitiesToCmmnTest() {
    assertGeneratedMigrationPlan(CallActivityModels.oneCmmnCallActivityProcess("foo"), CallActivityModels.oneCmmnCallActivityProcess("foo"))
      .hasInstructions(
        migrate("callActivity").to("callActivity"),
        migrate("userTask").to("userTask"));
  }

  @Test
  void testMapCallActivitiesFromBpmnToCmmnTest() {
    assertGeneratedMigrationPlan(CallActivityModels.oneBpmnCallActivityProcess("foo"), CallActivityModels.oneCmmnCallActivityProcess("foo"))
      .hasInstructions(
        migrate("callActivity").to("callActivity"),
        migrate("userTask").to("userTask"));
  }

  @Test
  void testMapCallActivitiesFromCmmnToBpmnTest() {
    assertGeneratedMigrationPlan(CallActivityModels.oneCmmnCallActivityProcess("foo"), CallActivityModels.oneBpmnCallActivityProcess("foo"))
      .hasInstructions(
        migrate("callActivity").to("callActivity"),
        migrate("userTask").to("userTask"));
  }

  @Test
  void testMapEventBasedGateway() {
    assertGeneratedMigrationPlan(EventBasedGatewayModels.TIMER_EVENT_BASED_GW_PROCESS, EventBasedGatewayModels.SIGNAL_EVENT_BASED_GW_PROCESS)
      .hasInstructions(
        migrate("eventBasedGateway").to("eventBasedGateway"));
  }

  @Test
  void testMapEventBasedGatewayWithIdenticalFollowingEvents() {
    assertGeneratedMigrationPlan(EventBasedGatewayModels.TIMER_EVENT_BASED_GW_PROCESS, EventBasedGatewayModels.TIMER_EVENT_BASED_GW_PROCESS)
      .hasInstructions(
        migrate("eventBasedGateway").to("eventBasedGateway"),
        migrate("timerCatch").to("timerCatch"),
        migrate("afterTimerCatch").to("afterTimerCatch"));
  }

  // event sub process

  @Test
  void testMapTimerEventSubProcessAndStartEvent() {
    assertGeneratedMigrationPlan(EventSubProcessModels.TIMER_EVENT_SUBPROCESS_PROCESS, EventSubProcessModels.TIMER_EVENT_SUBPROCESS_PROCESS)
      .hasInstructions(
        migrate("userTask").to("userTask"),
        migrate("eventSubProcess").to("eventSubProcess"),
        migrate("eventSubProcessStart").to("eventSubProcessStart"),
        migrate("eventSubProcessTask").to("eventSubProcessTask"));
  }

  @Test
  void testMapMessageEventSubProcessAndStartEvent() {
    assertGeneratedMigrationPlan(EventSubProcessModels.MESSAGE_EVENT_SUBPROCESS_PROCESS, EventSubProcessModels.MESSAGE_EVENT_SUBPROCESS_PROCESS)
      .hasInstructions(
        migrate("userTask").to("userTask"),
        migrate("eventSubProcess").to("eventSubProcess"),
        migrate("eventSubProcessStart").to("eventSubProcessStart"),
        migrate("eventSubProcessTask").to("eventSubProcessTask"));
  }

  @Test
  void testMapSignalEventSubProcessAndStartEvent() {
    assertGeneratedMigrationPlan(EventSubProcessModels.SIGNAL_EVENT_SUBPROCESS_PROCESS, EventSubProcessModels.SIGNAL_EVENT_SUBPROCESS_PROCESS)
      .hasInstructions(
        migrate("userTask").to("userTask"),
        migrate("eventSubProcess").to("eventSubProcess"),
        migrate("eventSubProcessStart").to("eventSubProcessStart"),
        migrate("eventSubProcessTask").to("eventSubProcessTask"));
  }

  @Test
  void testMapEscalationEventSubProcessAndStartEvent() {
    assertGeneratedMigrationPlan(EventSubProcessModels.ESCALATION_EVENT_SUBPROCESS_PROCESS, EventSubProcessModels.ESCALATION_EVENT_SUBPROCESS_PROCESS)
      .hasInstructions(
        migrate("userTask").to("userTask"),
        migrate("eventSubProcess").to("eventSubProcess"),
        migrate("eventSubProcessTask").to("eventSubProcessTask"));
  }

  @Test
  void testMapErrorEventSubProcessAndStartEvent() {
    assertGeneratedMigrationPlan(EventSubProcessModels.ERROR_EVENT_SUBPROCESS_PROCESS, EventSubProcessModels.ERROR_EVENT_SUBPROCESS_PROCESS)
      .hasInstructions(
        migrate("userTask").to("userTask"),
        migrate("eventSubProcess").to("eventSubProcess"),
        migrate("eventSubProcessTask").to("eventSubProcessTask"));
  }

  @Test
  void testMapCompensationEventSubProcessAndStartEvent() {
    assertGeneratedMigrationPlan(EventSubProcessModels.COMPENSATE_EVENT_SUBPROCESS_PROCESS, EventSubProcessModels.COMPENSATE_EVENT_SUBPROCESS_PROCESS)
      .hasInstructions(
        migrate("subProcess").to("subProcess"),
        migrate("userTask").to("userTask"),
        migrate("eventSubProcessStart").to("eventSubProcessStart"));
  }

  @Test
  void testNotMapEventSubProcessStartEventOfDifferentType() {
    assertGeneratedMigrationPlan(EventSubProcessModels.TIMER_EVENT_SUBPROCESS_PROCESS, EventSubProcessModels.SIGNAL_EVENT_SUBPROCESS_PROCESS)
      .hasInstructions(
        migrate("userTask").to("userTask"),
        migrate("eventSubProcess").to("eventSubProcess"),
        migrate("eventSubProcessTask").to("eventSubProcessTask"));
  }

  @Test
  void testMapEventSubProcessStartEventWhenSubProcessesAreNotEqual() {
    BpmnModelInstance sourceModel = EventSubProcessModels.TIMER_EVENT_SUBPROCESS_PROCESS;
    BpmnModelInstance targetModel = modify(EventSubProcessModels.TIMER_EVENT_SUBPROCESS_PROCESS)
        .changeElementId("eventSubProcess", "newEventSubProcess");

    assertGeneratedMigrationPlan(sourceModel, targetModel)
      .hasInstructions(
        migrate("userTask").to("userTask"),
        migrate("eventSubProcessStart").to("eventSubProcessStart"));
  }

  @Test
  void testMapEventSubProcessToEmbeddedSubProcess() {
    BpmnModelInstance sourceModel = modify(EventSubProcessModels.TIMER_EVENT_SUBPROCESS_PROCESS)
        .changeElementId("eventSubProcess", "subProcess");
    BpmnModelInstance targetModel = ProcessModels.SUBPROCESS_PROCESS;

    assertGeneratedMigrationPlan(sourceModel, targetModel)
      .hasInstructions(
        migrate("subProcess").to("subProcess"));
  }

  @Test
  void testMapEmbeddedSubProcessToEventSubProcess() {
    BpmnModelInstance sourceModel = ProcessModels.SUBPROCESS_PROCESS;
    BpmnModelInstance targetModel = modify(EventSubProcessModels.TIMER_EVENT_SUBPROCESS_PROCESS)
        .changeElementId("eventSubProcess", "subProcess");

    assertGeneratedMigrationPlan(sourceModel, targetModel)
      .hasInstructions(
        migrate("subProcess").to("subProcess"));
  }

  @Test
  void testMapExternalServiceTask() {
    BpmnModelInstance sourceModel = ExternalTaskModels.ONE_EXTERNAL_TASK_PROCESS;
    BpmnModelInstance targetModel = ExternalTaskModels.ONE_EXTERNAL_TASK_PROCESS;

    assertGeneratedMigrationPlan(sourceModel, targetModel)
      .hasInstructions(
        migrate("externalTask").to("externalTask"));
  }

  @Test
  void testMapExternalServiceToDifferentType() {
    BpmnModelInstance sourceModel = ExternalTaskModels.ONE_EXTERNAL_TASK_PROCESS;
    BpmnModelInstance targetModel = ProcessModels.newModel()
      .startEvent()
      .sendTask("externalTask")
        .operatonType("external")
        .operatonTopic("foo")
      .endEvent()
      .done();

    assertGeneratedMigrationPlan(sourceModel, targetModel)
      .hasInstructions(
        migrate("externalTask").to("externalTask"));
  }

  @Test
  void testNotMapExternalToClassDelegateServiceTask() {
    BpmnModelInstance sourceModel = ExternalTaskModels.ONE_EXTERNAL_TASK_PROCESS;
    BpmnModelInstance targetModel = modify(ServiceTaskModels.oneClassDelegateServiceTask("foo.Bar"))
      .changeElementId("serviceTask", "externalTask");

    assertGeneratedMigrationPlan(sourceModel, targetModel)
      .hasEmptyInstructions();
  }

  @Test
  void testMapParallelGateways() {
    BpmnModelInstance model = GatewayModels.PARALLEL_GW;

    assertGeneratedMigrationPlan(model, model)
      .hasInstructions(
        migrate("fork").to("fork"),
        migrate("join").to("join"),
        migrate("parallel1").to("parallel1"),
        migrate("parallel2").to("parallel2"),
        migrate("afterJoin").to("afterJoin")
      );
  }

  @Test
  void testMapInclusiveGateways() {
    BpmnModelInstance model = GatewayModels.INCLUSIVE_GW;

    assertGeneratedMigrationPlan(model, model)
      .hasInstructions(
        migrate("fork").to("fork"),
        migrate("join").to("join"),
        migrate("parallel1").to("parallel1"),
        migrate("parallel2").to("parallel2"),
        migrate("afterJoin").to("afterJoin")
      );
  }

  @Test
  void testNotMapParallelToInclusiveGateway() {

    assertGeneratedMigrationPlan(GatewayModels.PARALLEL_GW, GatewayModels.INCLUSIVE_GW)
      .hasInstructions(
        migrate("parallel1").to("parallel1"),
        migrate("parallel2").to("parallel2"),
        migrate("afterJoin").to("afterJoin")
      );
  }

  @Test
  void testMapTransaction() {

    assertGeneratedMigrationPlan(TransactionModels.ONE_TASK_TRANSACTION, TransactionModels.ONE_TASK_TRANSACTION)
      .hasInstructions(
        migrate("transaction").to("transaction"),
        migrate("userTask").to("userTask")
      );
  }

  @Test
  void testMapEmbeddedSubProcessToTransaction() {
    BpmnModelInstance sourceProcess = ProcessModels.SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = modify(TransactionModels.ONE_TASK_TRANSACTION)
        .changeElementId("transaction", "subProcess");

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("subProcess").to("subProcess"),
        migrate("userTask").to("userTask")
      );
  }

  @Test
  void testMapTransactionToEventSubProcess() {

    BpmnModelInstance sourceProcess = TransactionModels.ONE_TASK_TRANSACTION;
    BpmnModelInstance targetProcess = modify(EventSubProcessModels.MESSAGE_EVENT_SUBPROCESS_PROCESS)
      .changeElementId("eventSubProcess", "transaction")
      .changeElementId("userTask", "foo")
      .changeElementId("eventSubProcessTask", "userTask");

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("transaction").to("transaction"),
        migrate("userTask").to("userTask")
      );
  }

  @Test
  void testMapNoUpdateEventTriggers() {
    BpmnModelInstance model = MessageReceiveModels.ONE_MESSAGE_CATCH_PROCESS;

    assertGeneratedMigrationPlan(model, model, false)
      .hasInstructions(
        migrate("userTask").to("userTask").updateEventTrigger(false),
        migrate("messageCatch").to("messageCatch").updateEventTrigger(false)
      );
  }

  @Test
  void testMapUpdateEventTriggers() {
    BpmnModelInstance model = MessageReceiveModels.ONE_MESSAGE_CATCH_PROCESS;

    assertGeneratedMigrationPlan(model, model, true)
      .hasInstructions(
        migrate("userTask").to("userTask").updateEventTrigger(false),
        migrate("messageCatch").to("messageCatch").updateEventTrigger(true)
      );
  }

  @Test
  void testMigrationPlanCreationWithEmptyDeploymentCache() {
    // given
    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    rule.getProcessEngineConfiguration().getDeploymentCache().discardProcessDefinitionCache();

    // when
    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
      .mapEqualActivities()
      .build();

    // then
    assertThat(migrationPlan).isNotNull();
  }

  @Test
  void testMapCompensationBoundaryEvents() {

    assertGeneratedMigrationPlan(CompensationModels.ONE_COMPENSATION_TASK_MODEL, CompensationModels.ONE_COMPENSATION_TASK_MODEL, true)
      .hasInstructions(
        migrate("userTask1").to("userTask1").updateEventTrigger(false),
        migrate("userTask2").to("userTask2").updateEventTrigger(false),
        migrate("compensationBoundary").to("compensationBoundary").updateEventTrigger(false)
      );
  }

  @Test
  void testMapCompensationStartEvents() {
    assertGeneratedMigrationPlan(CompensationModels.COMPENSATION_EVENT_SUBPROCESS_MODEL, CompensationModels.COMPENSATION_EVENT_SUBPROCESS_MODEL, true)
      .hasInstructions(
        migrate("subProcess").to("subProcess").updateEventTrigger(false),
        migrate("userTask1").to("userTask1").updateEventTrigger(false),
        migrate("eventSubProcessStart").to("eventSubProcessStart").updateEventTrigger(false),
        migrate("userTask2").to("userTask2").updateEventTrigger(false),
        migrate("compensationBoundary").to("compensationBoundary").updateEventTrigger(false)
      );

    // should not map eventSubProcess because it active compensation is not supported
  }

  @Test
  void testMapIntermediateConditionalEvent() {
    BpmnModelInstance sourceProcess = Bpmn.createExecutableProcess(CONDITIONAL_PROCESS_KEY)
      .startEvent()
      .intermediateCatchEvent(CONDITION_ID)
        .condition(VAR_CONDITION)
      .userTask(USER_TASK_ID)
      .endEvent()
      .done();

    assertGeneratedMigrationPlan(sourceProcess, sourceProcess, false)
      .hasInstructions(
        migrate(CONDITION_ID).to(CONDITION_ID).updateEventTrigger(true),
        migrate(USER_TASK_ID).to(USER_TASK_ID).updateEventTrigger(false)
      );
  }


  @Test
  void testMapConditionalEventSubProcess() {
    assertGeneratedMigrationPlan(EventSubProcessModels.FALSE_CONDITIONAL_EVENT_SUBPROCESS_PROCESS, EventSubProcessModels.CONDITIONAL_EVENT_SUBPROCESS_PROCESS, false)
      .hasInstructions(
        migrate(EVENT_SUB_PROCESS_START_ID).to(EVENT_SUB_PROCESS_START_ID).updateEventTrigger(true),
        migrate(EVENT_SUB_PROCESS_ID).to(EVENT_SUB_PROCESS_ID).updateEventTrigger(false),
        migrate(EVENT_SUB_PROCESS_TASK_ID).to(EVENT_SUB_PROCESS_TASK_ID),
        migrate(USER_TASK_ID).to(USER_TASK_ID)
      );
  }

  @Test
  void testMapConditionalBoundaryEvents() {
    BpmnModelInstance sourceProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder(USER_TASK_ID)
      .boundaryEvent(BOUNDARY_ID)
      .condition(VAR_CONDITION)
      .done();

    assertGeneratedMigrationPlan(sourceProcess, sourceProcess, false)
      .hasInstructions(
        migrate(BOUNDARY_ID).to(BOUNDARY_ID).updateEventTrigger(true),
        migrate(USER_TASK_ID).to(USER_TASK_ID).updateEventTrigger(false)
      );
  }

  // helper

  protected MigrationPlanAssert assertGeneratedMigrationPlan(BpmnModelInstance sourceProcess, BpmnModelInstance targetProcess) {
    return assertGeneratedMigrationPlan(sourceProcess, targetProcess, false);
  }

  protected MigrationPlanAssert assertGeneratedMigrationPlan(BpmnModelInstance sourceProcess, BpmnModelInstance targetProcess, boolean updateEventTriggers) {
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    MigrationInstructionsBuilder migrationInstructionsBuilder = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities();

    if (updateEventTriggers) {
      migrationInstructionsBuilder.updateEventTriggers();
    }

    MigrationPlan migrationPlan = migrationInstructionsBuilder
      .build();

    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition);

    return assertThat(migrationPlan);
  }

}

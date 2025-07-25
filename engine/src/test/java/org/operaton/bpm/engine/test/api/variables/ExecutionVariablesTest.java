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
package org.operaton.bpm.engine.test.api.variables;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.operaton.bpm.engine.RuntimeService;
import org.operaton.bpm.engine.TaskService;
import org.operaton.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.operaton.bpm.engine.runtime.Execution;
import org.operaton.bpm.engine.runtime.ProcessInstance;
import org.operaton.bpm.engine.runtime.VariableInstance;
import org.operaton.bpm.engine.task.Task;
import org.operaton.bpm.engine.test.Deployment;
import org.operaton.bpm.engine.test.junit5.ProcessEngineExtension;
import org.operaton.bpm.engine.test.junit5.ProcessEngineTestExtension;

/**
 * @author Roman Smirnov
 *
 */
class ExecutionVariablesTest {

  @RegisterExtension
  static ProcessEngineExtension engineRule = ProcessEngineExtension.builder().build();
  @RegisterExtension
  ProcessEngineTestExtension testRule = new ProcessEngineTestExtension(engineRule);

  RuntimeService runtimeService;
  TaskService taskService;

  @Deployment
  @Test
  void testTreeCompactionWithLocalVariableOnConcurrentExecution() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Execution innerTaskExecution = runtimeService
        .createExecutionQuery()
        .activityId("innerTask")
        .singleResult();

    Execution subProcessConcurrentExecution = runtimeService
        .createExecutionQuery()
        .executionId(((ExecutionEntity) innerTaskExecution).getParentId())
        .singleResult();

    Task task = taskService
        .createTaskQuery()
        .taskDefinitionKey("task")
        .singleResult();

    // when
    runtimeService.setVariableLocal(subProcessConcurrentExecution.getId(), "foo", "bar");
    // and completing the concurrent task, thereby pruning the sub process concurrent execution
    taskService.complete(task.getId());

    // then the variable still exists
    VariableInstance variable = runtimeService.createVariableInstanceQuery().singleResult();
    assertThat(variable).isNotNull();
    assertThat(variable.getName()).isEqualTo("foo");
    assertThat(variable.getExecutionId()).isEqualTo(processInstance.getId());
  }

  @Deployment(resources = "org/operaton/bpm/engine/test/api/variables/ExecutionVariablesTest.testTreeCompactionWithLocalVariableOnConcurrentExecution.bpmn20.xml")
  @Test
  void testStableVariableInstanceIdsOnCompaction() {
    runtimeService.startProcessInstanceByKey("process");

    Execution innerTaskExecution = runtimeService
        .createExecutionQuery()
        .activityId("innerTask")
        .singleResult();

    Execution subProcessConcurrentExecution = runtimeService
        .createExecutionQuery()
        .executionId(((ExecutionEntity) innerTaskExecution).getParentId())
        .singleResult();

    Task task = taskService
        .createTaskQuery()
        .taskDefinitionKey("task")
        .singleResult();

    // when
    runtimeService.setVariableLocal(subProcessConcurrentExecution.getId(), "foo", "bar");
    VariableInstance variableBeforeCompaction = runtimeService.createVariableInstanceQuery().singleResult();

    // and completing the concurrent task, thereby pruning the sub process concurrent execution
    taskService.complete(task.getId());

    // then the variable still exists
    VariableInstance variableAfterCompaction = runtimeService.createVariableInstanceQuery().singleResult();
    assertThat(variableAfterCompaction.getId()).isEqualTo(variableBeforeCompaction.getId());
  }

  @Deployment(resources = "org/operaton/bpm/engine/test/api/variables/ExecutionVariablesTest.testTreeCompactionForkParallelGateway.bpmn20.xml")
  @Test
  void testStableVariableInstanceIdsOnCompactionAndExpansion() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Execution task1Execution = runtimeService
        .createExecutionQuery()
        .activityId("task1")
        .singleResult();

    Task task2 = taskService
        .createTaskQuery()
        .taskDefinitionKey("task2")
        .singleResult();

    // when
    runtimeService.setVariableLocal(task1Execution.getId(), "foo", "bar");
    VariableInstance variableBeforeCompaction = runtimeService.createVariableInstanceQuery().singleResult();

    // compacting the tree
    taskService.complete(task2.getId());

    // expanding the tree
    runtimeService.createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("task2")
      .execute();

    // then the variable still exists
    VariableInstance variableAfterCompaction = runtimeService.createVariableInstanceQuery().singleResult();
    assertThat(variableAfterCompaction.getId()).isEqualTo(variableBeforeCompaction.getId());
  }

  @Deployment
  @Test
  void testTreeCompactionForkParallelGateway() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Task task1 = taskService
        .createTaskQuery()
        .taskDefinitionKey("task1")
        .singleResult();

    Execution task2Execution = runtimeService
        .createExecutionQuery()
        .activityId("task2")
        .singleResult();

    // when
    runtimeService.setVariableLocal(task2Execution.getId(), "foo", "bar");
    // and completing the other task, thereby pruning the concurrent execution
    taskService.complete(task1.getId());

    // then the variable still exists
    VariableInstance variable = runtimeService.createVariableInstanceQuery().singleResult();
    assertThat(variable).isNotNull();
    assertThat(variable.getName()).isEqualTo("foo");
    assertThat(variable.getExecutionId()).isEqualTo(processInstance.getId());
  }

  @Deployment
  @Test
  void testTreeCompactionNestedForkParallelGateway() {
    // given
    runtimeService.startProcessInstanceByKey("process");

    Task task1 = taskService
        .createTaskQuery()
        .taskDefinitionKey("task1")
        .singleResult();

    Execution task2Execution = runtimeService
        .createExecutionQuery()
        .activityId("task2")
        .singleResult();
    String subProcessScopeExecutionId = ((ExecutionEntity) task2Execution).getParentId();

    // when
    runtimeService.setVariableLocal(task2Execution.getId(), "foo", "bar");
    // and completing the other task, thereby pruning the concurrent execution
    taskService.complete(task1.getId());

    // then the variable still exists on the subprocess scope execution
    VariableInstance variable = runtimeService.createVariableInstanceQuery().singleResult();
    assertThat(variable).isNotNull();
    assertThat(variable.getName()).isEqualTo("foo");
    assertThat(variable.getExecutionId()).isEqualTo(subProcessScopeExecutionId);
  }

  @Deployment(resources = "org/operaton/bpm/engine/test/api/variables/ExecutionVariablesTest.testTreeCompactionForkParallelGateway.bpmn20.xml")
  @Test
  void testTreeCompactionWithVariablesOnScopeAndConcurrentExecution() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Task task1 = taskService
        .createTaskQuery()
        .taskDefinitionKey("task1")
        .singleResult();

    Execution task2Execution = runtimeService
        .createExecutionQuery()
        .activityId("task2")
        .singleResult();

    // when
    runtimeService.setVariable(processInstance.getId(), "foo", "baz");
    runtimeService.setVariableLocal(task2Execution.getId(), "foo", "bar");
    // and completing the other task, thereby pruning the concurrent execution
    taskService.complete(task1.getId());

    // then something happens
    VariableInstance variable = runtimeService.createVariableInstanceQuery().singleResult();
    assertThat(variable).isNotNull();
    assertThat(variable.getName()).isEqualTo("foo");
    assertThat(variable.getExecutionId()).isEqualTo(processInstance.getId());
  }

  @Deployment
  @Test
  void testForkWithThreeBranchesAndJoinOfTwoBranchesParallelGateway() {
    // given
    runtimeService.startProcessInstanceByKey("process");

    Execution task2Execution = runtimeService
        .createExecutionQuery()
        .activityId("task2")
        .singleResult();

    // when
    runtimeService.setVariableLocal(task2Execution.getId(), "foo", "bar");
    taskService.complete(taskService.createTaskQuery().taskDefinitionKey("task1").singleResult().getId());
    taskService.complete(taskService.createTaskQuery().taskDefinitionKey("task2").singleResult().getId());

    // then
    assertThat(runtimeService.createVariableInstanceQuery().count()).isZero();
  }

  @Deployment
  @Test
  void testForkWithThreeBranchesAndJoinOfTwoBranchesInclusiveGateway() {
    // given
    runtimeService.startProcessInstanceByKey("process");

    Execution task2Execution = runtimeService
        .createExecutionQuery()
        .activityId("task2")
        .singleResult();

    // when
    runtimeService.setVariableLocal(task2Execution.getId(), "foo", "bar");
    taskService.complete(taskService.createTaskQuery().taskDefinitionKey("task1").singleResult().getId());
    taskService.complete(taskService.createTaskQuery().taskDefinitionKey("task2").singleResult().getId());

    // then
    assertThat(runtimeService.createVariableInstanceQuery().count()).isZero();
  }

  @Deployment(resources = "org/operaton/bpm/engine/test/api/variables/ExecutionVariablesTest.testTreeCompactionForkParallelGateway.bpmn20.xml")
  @Test
  void testTreeCompactionAndExpansionWithConcurrentLocalVariables() {

    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Execution task1Execution = runtimeService.createExecutionQuery().activityId("task1").singleResult();
    Task task2 = taskService.createTaskQuery().taskDefinitionKey("task2").singleResult();

    runtimeService.setVariableLocal(task1Execution.getId(), "var", "value");

    // when compacting the tree
    taskService.complete(task2.getId());

    // and expanding again
    runtimeService.createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("task2")
      .execute();

    // then the variable is again assigned to task1's concurrent execution
    Task task1 = taskService.createTaskQuery().taskDefinitionKey("task1").singleResult();
    VariableInstance variable = runtimeService.createVariableInstanceQuery().singleResult();

    assertThat(variable.getExecutionId()).isEqualTo(task1.getExecutionId());
  }

  @Deployment(resources = "org/operaton/bpm/engine/test/api/variables/ExecutionVariablesTest.testTreeCompactionForkParallelGateway.bpmn20.xml")
  @Test
  void testTreeCompactionAndExpansionWithScopeExecutionVariables() {

    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    Task task2 = taskService.createTaskQuery().taskDefinitionKey("task2").singleResult();

    runtimeService.setVariableLocal(processInstance.getId(), "var", "value");

    // when compacting the tree
    taskService.complete(task2.getId());

    // and expanding again
    runtimeService.createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("task2")
      .execute();

    // then the variable is still assigned to the scope execution execution
    VariableInstance variable = runtimeService.createVariableInstanceQuery().singleResult();

    assertThat(variable.getExecutionId()).isEqualTo(processInstance.getId());
  }

}

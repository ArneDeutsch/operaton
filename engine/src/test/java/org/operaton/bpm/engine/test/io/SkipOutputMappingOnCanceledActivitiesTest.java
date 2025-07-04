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
package org.operaton.bpm.engine.test.io;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.operaton.bpm.engine.ExternalTaskService;
import org.operaton.bpm.engine.ProcessEngineException;
import org.operaton.bpm.engine.RuntimeService;
import org.operaton.bpm.engine.TaskService;
import org.operaton.bpm.engine.externaltask.LockedExternalTask;
import org.operaton.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.operaton.bpm.engine.task.Task;
import org.operaton.bpm.engine.test.Deployment;
import org.operaton.bpm.engine.test.junit5.ProcessEngineExtension;

@ExtendWith(ProcessEngineExtension.class)
class SkipOutputMappingOnCanceledActivitiesTest {

  protected static final String WORKER_ID = "aWorkerId";
  protected static final long LOCK_TIME = 10000L;
  protected static final String TOPIC_NAME = "externalTaskTopic";

  ProcessEngineConfigurationImpl processEngineConfiguration;
  RuntimeService runtimeService;
  ExternalTaskService externalTaskService;
  TaskService taskService;

  @AfterEach
  void tearDown() {
    processEngineConfiguration.setSkipOutputMappingOnCanceledActivities(false);
  }

  @Test
  @Deployment(resources = "org/operaton/bpm/engine/test/io/SkipOutputMappingOnCanceledActivitiesTest.oneExternalTaskWithOutputMappingAndCatchingErrorBoundaryEvent.bpmn")
  void shouldSkipOutputMappingOnBpmnErrorAtExternalTask() {
    // given a process with one external task which has output mapping configured
    processEngineConfiguration.setSkipOutputMappingOnCanceledActivities(true);
    runtimeService.startProcessInstanceByKey("externalTaskProcess");

    // when
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    assertThat(externalTasks).hasSize(1);
    externalTaskService.handleBpmnError(externalTasks.get(0).getId(), WORKER_ID, "errorCode", null);

    // then
    // expect no mapping failure
    // error was caught
    // output mapping is skipped
    Task userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
  }

  @Test
  @Deployment(resources = "org/operaton/bpm/engine/test/io/SkipOutputMappingOnCanceledActivitiesTest.oneExternalTaskWithOutputMappingAndCatchingErrorBoundaryEvent.bpmn")
  void shouldNotSkipOutputMappingOnBpmnErrorAtExternalTask() {
    // given a process with one external task which has output mapping configured
    processEngineConfiguration.setSkipOutputMappingOnCanceledActivities(false);
    runtimeService.startProcessInstanceByKey("externalTaskProcess");

    // when/then
    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, WORKER_ID)
      .topic(TOPIC_NAME, LOCK_TIME)
      .execute();
    assertThat(externalTasks).hasSize(1);
    String externalTaskId = externalTasks.get(0).getId();
    assertThatThrownBy(() -> externalTaskService.handleBpmnError(externalTaskId, WORKER_ID, "errorCode", null))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Propagation of bpmn error errorCode failed.");
  }

  @Test
  @Deployment(resources = "org/operaton/bpm/engine/test/io/SkipOutputMappingOnCanceledActivitiesTest.oneSubprocessWithOutputMappingAndCatchingErrorBoundaryEvent.bpmn")
  void shouldSkipOutputMappingOnBpmnErrorInSubprocess() {
    // given a process with one external task which has output mapping configured
    processEngineConfiguration.setSkipOutputMappingOnCanceledActivities(true);
    runtimeService.startProcessInstanceByKey("subProcess");

    // when
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getName()).isEqualTo("userTask in Subprocess");
    taskService.handleBpmnError(task.getId(), "errorCode");

    // then
    // expect no mapping failure
    // error was caught
    // output mapping is skipped
    Task userTask = taskService.createTaskQuery().singleResult();
    assertThat(userTask).isNotNull();
    assertThat(userTask.getName()).isEqualTo("userTask");
  }

  @Test
  @Deployment(resources = "org/operaton/bpm/engine/test/io/SkipOutputMappingOnCanceledActivitiesTest.oneSubprocessWithOutputMappingAndCatchingErrorBoundaryEvent.bpmn")
  void shouldNotSkipOutputMappingOnBpmnErrorInSubprocess() {
    // given a process with one external task which has output mapping configured
    processEngineConfiguration.setSkipOutputMappingOnCanceledActivities(false);
    runtimeService.startProcessInstanceByKey("subProcess");

    // when/then
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getName()).isEqualTo("userTask in Subprocess");
    String taskId = task.getId();

    assertThatThrownBy(() -> taskService.handleBpmnError(taskId, "errorCode"))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Propagation of bpmn error errorCode failed.");
  }
}

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
package org.operaton.bpm.engine.test.bpmn.behavior;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.operaton.bpm.engine.ProcessEngineException;
import org.operaton.bpm.engine.RuntimeService;
import org.operaton.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.operaton.bpm.engine.test.Deployment;
import org.operaton.bpm.engine.test.junit5.ProcessEngineExtension;
import org.operaton.bpm.engine.test.junit5.ProcessEngineLoggingExtension;
import org.operaton.bpm.engine.test.junit5.ProcessEngineTestExtension;

import ch.qos.logback.classic.Level;

class BpmnBehaviorLoggerTest {

  @RegisterExtension
  static ProcessEngineExtension engineRule = ProcessEngineExtension.builder().build();
  @RegisterExtension
  ProcessEngineTestExtension testRule = new ProcessEngineTestExtension(engineRule);
  @RegisterExtension
  ProcessEngineLoggingExtension processEngineLoggingRule = new ProcessEngineLoggingExtension().watch(
      "org.operaton.bpm.engine.bpmn.behavior", Level.INFO);

  ProcessEngineConfigurationImpl processEngineConfiguration;
  RuntimeService runtimeService;

  @AfterEach
  void tearDown() {
    processEngineConfiguration.setEnableExceptionsAfterUnhandledBpmnError(false);
  }

  @Test
  @Deployment(resources = {
      "org/operaton/bpm/engine/test/bpmn/behavior/BpmnBehaviorLoggerTest.UnhandledBpmnError.bpmn20.xml"})
  void shouldIncludeBpmnErrorMessageInUnhandledBpmnError() {
    // given
    processEngineConfiguration.setEnableExceptionsAfterUnhandledBpmnError(true);
    String errorMessage = "Execution with id 'serviceTask' throws an error event with errorCode 'errorCode' and errorMessage 'ouch!', but no error handler was defined";
    // when & then
    assertThatThrownBy(() -> runtimeService.startProcessInstanceByKey("testProcess"))
        .isInstanceOf(ProcessEngineException.class)
        .hasMessageContaining(errorMessage);
  }

  @Test
  @Deployment(resources = {
      "org/operaton/bpm/engine/test/bpmn/behavior/BpmnBehaviorLoggerTest.UnhandledBpmnError.bpmn20.xml"})
  void shouldLogBpmnErrorMessageInUnhandledBpmnErrorWithoutException() {
    // given
    String logMessage = "Execution with id 'serviceTask' throws an error event with errorCode 'errorCode' and errorMessage 'ouch!'";
    // when
    runtimeService.startProcessInstanceByKey("testProcess");
    // then
    assertThat(processEngineLoggingRule.getFilteredLog(logMessage)).hasSize(1);
  }
}

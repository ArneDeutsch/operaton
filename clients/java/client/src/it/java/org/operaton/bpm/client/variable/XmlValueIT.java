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
package org.operaton.bpm.client.variable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.operaton.bpm.client.ExternalTaskClient;
import org.operaton.bpm.client.dto.ProcessDefinitionDto;
import org.operaton.bpm.client.dto.ProcessInstanceDto;
import org.operaton.bpm.client.dto.TaskDto;
import org.operaton.bpm.client.dto.VariableInstanceDto;
import org.operaton.bpm.client.rule.ClientRule;
import org.operaton.bpm.client.rule.EngineRule;
import org.operaton.bpm.client.task.ExternalTask;
import org.operaton.bpm.client.task.ExternalTaskService;
import org.operaton.bpm.client.util.RecordingExternalTaskHandler;
import org.operaton.bpm.client.util.RecordingInvocationHandler;
import org.operaton.bpm.client.util.RecordingInvocationHandler.RecordedInvocation;
import org.operaton.bpm.client.variable.value.XmlValue;
import org.operaton.bpm.engine.variable.Variables;
import org.operaton.bpm.engine.variable.value.TypedValue;
import org.operaton.bpm.model.bpmn.BpmnModelInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.operaton.bpm.client.util.ProcessModels.EXTERNAL_TASK_TOPIC_BAR;
import static org.operaton.bpm.client.util.ProcessModels.EXTERNAL_TASK_TOPIC_FOO;
import static org.operaton.bpm.client.util.ProcessModels.PROCESS_KEY_2;
import static org.operaton.bpm.client.util.ProcessModels.TWO_EXTERNAL_TASK_PROCESS;
import static org.operaton.bpm.client.util.ProcessModels.USER_TASK_ID;
import static org.operaton.bpm.client.util.ProcessModels.createProcessWithExclusiveGateway;
import static org.operaton.bpm.client.variable.ClientValues.XML;

public class XmlValueIT {

  protected static final String VARIABLE_NAME_XML = "xmlVariable";

  protected static final String VARIABLE_VALUE_XML_SERIALIZED = "<elementName attrName=\"attrValue\" />";
  protected static final String VARIABLE_VALUE_XML_SERIALIZED_BROKEN = "<elementName attrName=attrValue\" />";

  protected static final XmlValue VARIABLE_VALUE_XML_VALUE = ClientValues.xmlValue(VARIABLE_VALUE_XML_SERIALIZED);

  protected static final XmlValue VARIABLE_VALUE_XML_VALUE_BROKEN = ClientValues.xmlValue(VARIABLE_VALUE_XML_SERIALIZED_BROKEN);

  @RegisterExtension
  static ClientRule clientRule = new ClientRule();
  @RegisterExtension
  static EngineRule engineRule = new EngineRule();

  protected ExternalTaskClient client;

  protected ProcessDefinitionDto processDefinition;
  protected ProcessInstanceDto processInstance;

  protected RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler();
  protected RecordingInvocationHandler invocationHandler = new RecordingInvocationHandler();

  @BeforeEach
  public void setup() {
    client = clientRule.client();
    processDefinition = engineRule.deploy(TWO_EXTERNAL_TASK_PROCESS).get(0);

    handler.clear();
    invocationHandler.clear();
  }

  @Test
  public void shouldGetXml() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, VARIABLE_VALUE_XML_VALUE);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    String variableValue = task.getVariable(VARIABLE_NAME_XML);
    assertThat(variableValue).isNotNull();
    assertThat(variableValue).isEqualTo(VARIABLE_VALUE_XML_SERIALIZED);
  }

  @Test
  public void shouldGetTypedXml() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, VARIABLE_VALUE_XML_VALUE);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    XmlValue typedValue = task.getVariableTyped(VARIABLE_NAME_XML);
    assertThat(typedValue.getType()).isEqualTo(XML);
    assertThat(typedValue.getValue()).isEqualTo(VARIABLE_VALUE_XML_SERIALIZED);
  }

  @Test
  public void shouldGetNull() {
    // given
    XmlValue xmlValue = ClientValues.xmlValue(null);

    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, xmlValue);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    String variableValue = task.getVariable(VARIABLE_NAME_XML);
    assertThat(variableValue ).isNull();
  }

  @Test
  public void shoulGetNullTyped() {
    // given
    XmlValue xmlValue = ClientValues.xmlValue(null);

    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, xmlValue);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    XmlValue typedValue = task.getVariableTyped(VARIABLE_NAME_XML);
    assertThat(typedValue.getType()).isEqualTo(XML);
    assertThat(typedValue.getValue()).isNull();
  }

  @Test
  public void shouldReturnBrokenXml() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_XML, VARIABLE_VALUE_XML_VALUE_BROKEN);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    String variableValue = task.getVariable(VARIABLE_NAME_XML);
    assertThat(variableValue).isEqualTo(VARIABLE_VALUE_XML_SERIALIZED_BROKEN);
  }

  @Test
  public void shoudSetVariable() {
    // given
    engineRule.startProcessInstance(processDefinition.getId());

    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(invocationHandler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !invocationHandler.getInvocations().isEmpty());

    RecordedInvocation invocation = invocationHandler.getInvocations().get(0);
    ExternalTask fooTask = invocation.getExternalTask();
    ExternalTaskService fooService = invocation.getExternalTaskService();

    client.subscribe(EXTERNAL_TASK_TOPIC_BAR)
      .handler(handler)
      .open();

    // when
    Map<String, Object> variables = Variables.createVariables();
    variables.put(VARIABLE_NAME_XML, ClientValues.xmlValue(VARIABLE_VALUE_XML_SERIALIZED));
    fooService.complete(fooTask, variables);

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    String variableValue = task.getVariable(VARIABLE_NAME_XML);
    assertThat(variableValue).isEqualTo(VARIABLE_VALUE_XML_SERIALIZED);

    TypedValue typedValue = task.getVariableTyped(VARIABLE_NAME_XML);
    assertThat(typedValue.getValue()).isEqualTo(VARIABLE_VALUE_XML_SERIALIZED);
    assertThat(typedValue.getType()).isEqualTo(XML);
  }

  @Test
  public void shoudSetVariableNull() {
    // given
    engineRule.startProcessInstance(processDefinition.getId());

    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(invocationHandler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !invocationHandler.getInvocations().isEmpty());

    RecordedInvocation invocation = invocationHandler.getInvocations().get(0);
    ExternalTask fooTask = invocation.getExternalTask();
    ExternalTaskService fooService = invocation.getExternalTaskService();

    client.subscribe(EXTERNAL_TASK_TOPIC_BAR)
      .handler(handler)
      .open();

    // when
    Map<String, Object> variables = Variables.createVariables();
    variables.put(VARIABLE_NAME_XML, ClientValues.xmlValue(null));
    fooService.complete(fooTask, variables);

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    String variableValue = task.getVariable(VARIABLE_NAME_XML);
    assertThat(variableValue).isNull();

    TypedValue typedValue = task.getVariableTyped(VARIABLE_NAME_XML);
    assertThat(typedValue.getValue()).isNull();
    assertThat(typedValue.getType()).isEqualTo(XML);
  }

  @Test
  public void shoudSetTransientVariable() {
    // given
    BpmnModelInstance process = createProcessWithExclusiveGateway(PROCESS_KEY_2, "${XML(" + VARIABLE_NAME_XML + ").attr('attrName').value() == 'attrValue'}");
    ProcessDefinitionDto definition = engineRule.deploy(process).get(0);
    ProcessInstanceDto processInstance = engineRule.startProcessInstance(definition.getId());

    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      Map<String, Object> variables = new HashMap<>();
      variables.put(VARIABLE_NAME_XML, ClientValues.xmlValue(VARIABLE_VALUE_XML_SERIALIZED, true));
      client.complete(task, variables);
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    TaskDto task = engineRule.getTaskByProcessInstanceId(processInstance.getId());
    assertThat(task).isNotNull();
    assertThat(task.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(task.getTaskDefinitionKey()).isEqualTo(USER_TASK_ID);

    List<VariableInstanceDto> variables = engineRule.getVariablesByProcessInstanceIdAndVariableName(processInstance.getId(), VARIABLE_NAME_XML);
    assertThat(variables).isEmpty();
  }

}

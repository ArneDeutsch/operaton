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
package org.operaton.bpm.engine.test.assertions.bpmn;

import org.operaton.bpm.engine.runtime.ProcessInstance;
import org.operaton.bpm.engine.test.Deployment;
import org.operaton.bpm.engine.test.assertions.helpers.ProcessAssertTestCase;
import static org.operaton.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;

import org.junit.jupiter.api.Test;

class ProcessInstanceAssertHasVariablesTest extends ProcessAssertTestCase {

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-hasVariables.bpmn"
  })
  void hasVariablesOneSuccess() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-hasVariables", withVariables("aVariable", "aValue")
    );
    // Then
    assertThat(processInstance).hasVariables();
    // And
    assertThat(processInstance).hasVariables("aVariable");
    // When
    complete(task(processInstance));
    // Then
    assertThat(processInstance).hasVariables();
    // And
    assertThat(processInstance).hasVariables("aVariable");
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-hasVariables.bpmn"
  })
  void hasVariablesOneFailure() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-hasVariables", withVariables("aVariable", "aValue")
    );
    // Then
    expect(() -> assertThat(processInstance).hasVariables("anotherVariable"));
    // And
    expect(() -> assertThat(processInstance).hasVariables("aVariable", "anotherVariable"));
    // When
    complete(task(processInstance));
    // Then
    expect(() -> assertThat(processInstance).hasVariables("anotherVariable"));
    // And
    expect(() -> assertThat(processInstance).hasVariables("aVariable", "anotherVariable"));
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-hasVariables.bpmn"
  })
  void hasVariablesTwoSuccess() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-hasVariables", withVariables("firstVariable", "firstValue", "secondVariable", "secondValue")
    );
    // Then
    assertThat(processInstance).hasVariables();
    // And
    assertThat(processInstance).hasVariables("firstVariable");
    // And
    assertThat(processInstance).hasVariables("secondVariable");
    // And
    assertThat(processInstance).hasVariables("firstVariable", "secondVariable");
    // And
    assertThat(processInstance).hasVariables("secondVariable", "firstVariable");
    // When
    complete(task(processInstance));
    // Then
    assertThat(processInstance).hasVariables();
    // And
    assertThat(processInstance).hasVariables("firstVariable");
    // And
    assertThat(processInstance).hasVariables("secondVariable");
    // And
    assertThat(processInstance).hasVariables("firstVariable", "secondVariable");
    // And
    assertThat(processInstance).hasVariables("secondVariable", "firstVariable");
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-hasVariables.bpmn"
  })
  void hasVariablesTwoFailure() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-hasVariables", withVariables("firstVariable", "firstValue", "secondVariable", "secondValue")
    );
    // Then
    expect(() -> assertThat(processInstance).hasVariables("anotherVariable"));
    // And
    expect(() -> assertThat(processInstance).hasVariables("firstVariable", "anotherVariable"));
    // And
    expect(() -> assertThat(processInstance).hasVariables("secondVariable", "anotherVariable"));
    // And
    expect(() -> assertThat(processInstance).hasVariables("firstVariable", "secondVariable", "anotherVariable"));
    // When
    complete(task(processInstance));
    // Then
    expect(() -> assertThat(processInstance).hasVariables("anotherVariable"));
    // And
    expect(() -> assertThat(processInstance).hasVariables("firstVariable", "anotherVariable"));
    // And
    expect(() -> assertThat(processInstance).hasVariables("secondVariable", "anotherVariable"));
    // And
    expect(() -> assertThat(processInstance).hasVariables("firstVariable", "secondVariable", "anotherVariable"));
  }

  @Test
  @Deployment(resources = {"bpmn/ProcessInstanceAssert-hasVariables.bpmn"
  })
  void hasVariablesNoneFailure() {
    // When
    final ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "ProcessInstanceAssert-hasVariables"
    );
    // Then
    expect(() -> assertThat(processInstance).hasVariables());
    // And
    expect(() -> assertThat(processInstance).hasVariables("aVariable"));
    // When
    complete(task(processInstance));
    // Then
    expect(() -> assertThat(processInstance).hasVariables());
    // And
    expect(() -> assertThat(processInstance).hasVariables("aVariable"));
  }

}

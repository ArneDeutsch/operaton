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
package org.operaton.bpm.engine.test.standalone.pvm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.operaton.bpm.engine.impl.pvm.ProcessDefinitionBuilder;
import org.operaton.bpm.engine.impl.pvm.PvmExecution;
import org.operaton.bpm.engine.impl.pvm.PvmProcessDefinition;
import org.operaton.bpm.engine.impl.pvm.PvmProcessInstance;
import org.operaton.bpm.engine.impl.pvm.runtime.ExecutionImpl;
import org.operaton.bpm.engine.test.standalone.pvm.activities.Automatic;
import org.operaton.bpm.engine.test.standalone.pvm.activities.EmbeddedSubProcess;
import org.operaton.bpm.engine.test.standalone.pvm.activities.EventScopeCreatingSubprocess;
import org.operaton.bpm.engine.test.standalone.pvm.activities.WaitState;


/**
 *
 * @author Daniel Meyer
 */
class PvmEventScopesTest {

  /**
   *
   *                       create evt scope --+
   *                                          |
   *                                          v
   *
   *           +------------------------------+
   *           | embedded subprocess          |
   * +-----+   |  +-----------+   +---------+ |   +----+   +---+
   * |start|-->|  |startInside|-->|endInside| |-->|wait|-->|end|
   * +-----+   |  +-----------+   +---------+ |   +----+   +---+
   *           +------------------------------+
   *
   *                                                           ^
   *                                                           |
   *                                       destroy evt scope --+
   *
   */
  @Test
  void testActivityEndDestroysEventScopes() {
      PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("embeddedsubprocess")
      .endActivity()
      .createActivity("embeddedsubprocess")
        .scope()
        .behavior(new EventScopeCreatingSubprocess())
        .createActivity("startInside")
          .behavior(new Automatic())
          .transition("endInside")
        .endActivity()
         .createActivity("endInside")
          .behavior(new Automatic())
        .endActivity()
      .transition("wait")
      .endActivity()
      .createActivity("wait")
        .behavior(new WaitState())
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new Automatic())
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    boolean eventScopeFound = false;
    List<ExecutionImpl> executions = ((ExecutionImpl)processInstance).getExecutions();
    for (ExecutionImpl executionImpl : executions) {
      if(executionImpl.isEventScope()) {
        eventScopeFound = true;
        break;
      }
    }

    assertThat(eventScopeFound).isTrue();

    processInstance.signal(null, null);

    assertThat(processInstance.isEnded()).isTrue();

  }


  /** 
   *           +----------------------------------------------------------------------+
   *           | embedded subprocess                                                  |
   *           |                                                                      |
   *           |                                create evt scope --+                  |
   *           |                                                   |                  |
   *           |                                                   v                  |
   *           |                                                                      |
   *           |                  +--------------------------------+                  |
   *           |                  | nested embedded subprocess     |                  |
   * +-----+   | +-----------+    |  +-----------------+           |   +----+   +---+ |   +---+
   * |start|-->| |startInside|--> |  |startNestedInside|           |-->|wait|-->|end| |-->|end|
   * +-----+   | +-----------+    |  +-----------------+           |   +----+   +---+ |   +---+
   *           |                  +--------------------------------+                  |
   *           |                                                                      |
   *           +----------------------------------------------------------------------+
   *
   *                                                                                  ^
   *                                                                                  |
   *                                                              destroy evt scope --+
   */
  @Test
  void testTransitionDestroysEventScope() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("embeddedsubprocess")
      .endActivity()
      .createActivity("embeddedsubprocess")
        .scope()
        .behavior(new EmbeddedSubProcess())
        .createActivity("startInside")
          .behavior(new Automatic())
          .transition("nestedSubProcess")
        .endActivity()
          .createActivity("nestedSubProcess")
          .scope()
          .behavior(new EventScopeCreatingSubprocess())
            .createActivity("startNestedInside")
              .behavior(new Automatic())
              .endActivity()
            .transition("wait")
          .endActivity()
          .createActivity("wait")
            .behavior(new WaitState())
            .transition("endInside")
          .endActivity()
          .createActivity("endInside")
            .behavior(new Automatic())
            .endActivity()
      .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new Automatic())
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    List<String> expectedActiveActivityIds = new ArrayList<>();
    expectedActiveActivityIds.add("wait");
    assertThat(processInstance.findActiveActivityIds()).isEqualTo(expectedActiveActivityIds);


    PvmExecution execution = processInstance.findExecution("wait");
    execution.signal(null, null);

    assertThat(processInstance.isEnded()).isTrue();

  }

}

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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.operaton.bpm.engine.impl.cmd.CompleteTaskCmd;
import org.operaton.bpm.engine.impl.cmd.SetTaskVariablesCmd;
import org.operaton.bpm.engine.impl.interceptor.CommandContext;
import org.operaton.bpm.engine.test.Deployment;
import org.operaton.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

/**
 * @author Svetlana Dorokhova
 *
 */
class CompetingCompleteTaskSetVariableTest extends ConcurrencyTestCase {

  protected static class ControllableCompleteTaskCommand extends ConcurrencyTestCase.ControllableCommand<Void> {

    protected String taskId;

    protected Exception exception;

    public ControllableCompleteTaskCommand(String taskId) {
      this.taskId = taskId;
    }

    @Override
    public Void execute(CommandContext commandContext) {
      monitor.sync();  // thread will block here until makeContinue() is called from main thread

      new CompleteTaskCmd(taskId, null).execute(commandContext);

      monitor.sync();  // thread will block here until waitUntilDone() is called form main thread

      return null;
    }

  }

  public class ControllableSetTaskVariablesCommand extends ConcurrencyTestCase.ControllableCommand<Void> {

    protected String taskId;

    protected Map<String, ? extends Object> variables;

    protected Exception exception;

    public ControllableSetTaskVariablesCommand(String taskId,  Map<String, ? extends Object> variables) {
      this.taskId = taskId;
      this.variables = variables;
    }

    @Override
    public Void execute(CommandContext commandContext) {
      monitor.sync();  // thread will block here until makeContinue() is called from main thread

      new SetTaskVariablesCmd(taskId, variables, true).execute(commandContext);

      monitor.sync();  // thread will block here until waitUntilDone() is called form main thread

      return null;
    }
  }

  @Deployment
  @Test
  void testCompleteTaskSetLocalVariable() {
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    final String taskId = taskService.createTaskQuery().singleResult().getId();

    ConcurrencyTestHelper.ThreadControl thread1 = executeControllableCommand(new ControllableSetTaskVariablesCommand(taskId, Variables.createVariables().putValue("var", "value")));
    thread1.reportInterrupts();
    thread1.waitForSync();

    ConcurrencyTestHelper.ThreadControl thread2 = executeControllableCommand(new ControllableCompleteTaskCommand(taskId));
    thread2.reportInterrupts();
    thread2.waitForSync();

    //set task variable, but not commit transaction
    thread1.makeContinue();
    thread1.waitForSync();

    //complete task -> task is removed, execution is removed
    thread2.makeContinue();
    thread2.waitForSync();

    //commit transaction with task variable
    thread1.makeContinue();
    thread1.waitUntilDone();

    //try to commit task completion
    thread2.makeContinue();
    thread2.waitUntilDone();

    //variable was persisted
    assertThat(runtimeService.createVariableInstanceQuery().taskIdIn(taskId).count()).isEqualTo(1);

    //task was not removed
    assertThat(thread2.exception).isNotNull();
    assertThat(taskService.createTaskQuery().taskId(taskId).count()).isEqualTo(1);

  }
}

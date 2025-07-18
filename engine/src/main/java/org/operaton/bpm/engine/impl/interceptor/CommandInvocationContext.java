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
package org.operaton.bpm.engine.impl.interceptor;

import java.util.ArrayList;
import java.util.List;

import org.operaton.bpm.application.InvocationContext;
import org.operaton.bpm.application.ProcessApplicationReference;
import org.operaton.bpm.engine.ProcessEngineException;
import org.operaton.bpm.engine.impl.ProcessEngineLogger;
import org.operaton.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.operaton.bpm.engine.impl.cmd.CommandLogger;
import org.operaton.bpm.engine.impl.context.Context;
import org.operaton.bpm.engine.impl.context.ProcessApplicationContextUtil;
import org.operaton.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.operaton.bpm.engine.impl.pvm.runtime.AtomicOperation;

/**
 * In contrast to {@link CommandContext}, this context holds resources that are only valid
 * during execution of a single command (i.e. the current command or an exception that was thrown
 * during its execution).
 *
 * @author Thorben Lindhauer
 */
public class CommandInvocationContext {

  private static final CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  protected Throwable throwable;
  protected Command< ? > command;
  protected boolean isExecuting = false;
  protected List<AtomicOperationInvocation> queuedInvocations = new ArrayList<>();
  protected BpmnStackTrace bpmnStackTrace = new BpmnStackTrace();
  protected ProcessDataContext processDataContext;

  /**
   * All-args constructor.
   *
   * @param command        the associated command of this command invocation context.
   * @param configuration  the process engine configuration
   * @param isOuterCommand when true, the flag marks this command invocation context as outer and parks any MDC value
   *                       associated with the Logging Context Parameters as external properties.
   *                       when set to false, this command invocation context will be associated to an inner command,
   *                       no external properties will be parked
   */
  public CommandInvocationContext(Command<?> command, ProcessEngineConfigurationImpl configuration, boolean isOuterCommand) {
    this.command = command;
    // only outer commands park external properties
    this.processDataContext = new ProcessDataContext(configuration, false, isOuterCommand);
  }

  public Throwable getThrowable() {
    return throwable;
  }

  public Command<?> getCommand() {
    return command;
  }

  public void trySetThrowable(Throwable t) {
    if (this.throwable == null) {
      this.throwable = t;
    }
    else {
      LOG.maskedExceptionInCommandContext(throwable);
    }
  }

  public void performOperation(AtomicOperation executionOperation, ExecutionEntity execution) {
    performOperation(executionOperation, execution, false);
  }

  public void performOperationAsync(AtomicOperation executionOperation, ExecutionEntity execution) {
    performOperation(executionOperation, execution, true);
  }

  public void performOperation(final AtomicOperation executionOperation, final ExecutionEntity execution, final boolean performAsync) {
    AtomicOperationInvocation invocation = new AtomicOperationInvocation(executionOperation, execution, performAsync);
    queuedInvocations.add(0, invocation);
    performNext();
  }

  protected void performNext() {
    AtomicOperationInvocation nextInvocation = queuedInvocations.get(0);

    if(nextInvocation.operation.isAsyncCapable() && isExecuting) {
      // will be picked up by while loop below
      return;
    }

    ProcessApplicationReference targetProcessApplication = getTargetProcessApplication(nextInvocation.execution);
    if(requiresContextSwitch(targetProcessApplication)) {

      Context.executeWithinProcessApplication(() -> {
        performNext();
        return null;
      }, targetProcessApplication, new InvocationContext(nextInvocation.execution));
    }
    else {
      if(!nextInvocation.operation.isAsyncCapable()) {
        // if operation is not async capable, perform right away.
        invokeNext();
      }
      else {
        try  {
          isExecuting = true;
          while (! queuedInvocations.isEmpty()) {
            // assumption: all operations are executed within the same process application...
            invokeNext();
          }
        }
        finally {
          isExecuting = false;
        }
      }
    }
  }

  protected void invokeNext() {
    AtomicOperationInvocation invocation = queuedInvocations.remove(0);
    try {
      invocation.execute(bpmnStackTrace, processDataContext);
    } catch(RuntimeException e) {
      // log bpmn stacktrace
      bpmnStackTrace.printStackTrace(Context.getProcessEngineConfiguration().isBpmnStacktraceVerbose());
      // rethrow
      throw e;
    }
  }

  protected boolean requiresContextSwitch(ProcessApplicationReference processApplicationReference) {
    return ProcessApplicationContextUtil.requiresContextSwitch(processApplicationReference);
  }

  protected ProcessApplicationReference getTargetProcessApplication(ExecutionEntity execution) {
    return ProcessApplicationContextUtil.getTargetProcessApplication(execution);
  }

  public void rethrow() {
    if (throwable != null) {
      if (throwable instanceof Error error) {
        throw error;
      } else if (throwable instanceof RuntimeException runtimeException) {
        throw runtimeException;
      } else {
        throw new ProcessEngineException("exception while executing command " + command, throwable);
      }
    }
  }

  public ProcessDataContext getProcessDataContext() {
    return processDataContext;
  }
}

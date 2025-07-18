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
package org.operaton.bpm.engine.cdi.impl.context;

import org.operaton.bpm.engine.ProcessEngineException;
import org.operaton.bpm.engine.runtime.Execution;
import org.operaton.bpm.engine.task.Task;
import org.operaton.bpm.engine.variable.VariableMap;
import org.operaton.bpm.engine.variable.value.TypedValue;

/**
 * Represents a means for associating an execution with a context.
 * <p />
 * This enables activiti-cdi to provide contextual business process management
 * services, without relying on a specific context like i.e. the conversation
 * context.
 *
 * @author Daniel Meyer
 */
public interface ContextAssociationManager {

  /**
   * Disassociates the current process instance with a context / scope
   *
   * @throws ProcessEngineException if no process instance is currently associated
   */
  void disAssociate();

  /**
   * @return the id of the execution currently associated or null
   */
  String getExecutionId();

  /**
   * get the current execution
   */
  Execution getExecution();

  /**
   * associate with the provided execution
   */
  void setExecution(Execution execution);

  /**
   * set a current task
   */
  void setTask(Task task);

  /**
   * get the current task
   */
  Task getTask();

  /**
   * set a process variable
   */
  void setVariable(String variableName, Object value);

  /**
   * get a process variable
   */
  TypedValue getVariable(String variableName);

  /**
   * @return a {@link VariableMap} of process variables cached between flushes
   */
  VariableMap getCachedVariables();

  /**
   * set a local process variable
   */
  void setVariableLocal(String variableName, Object value);

  /**
   * get a local process variable
   */
  TypedValue getVariableLocal(String variableName);

  /**
   * @return a {@link VariableMap} of local process variables cached between flushes
   */
  VariableMap getCachedLocalVariables();

  /**
   * allows to flush the cached variables.
   */
  void flushVariableCache();

}

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
package org.operaton.bpm.engine.impl.cmmn;

import static org.operaton.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.operaton.bpm.engine.exception.NotAllowedException;
import org.operaton.bpm.engine.exception.NotFoundException;
import org.operaton.bpm.engine.exception.NotValidException;
import org.operaton.bpm.engine.exception.NullValueException;
import org.operaton.bpm.engine.exception.cmmn.CaseDefinitionNotFoundException;
import org.operaton.bpm.engine.exception.cmmn.CaseExecutionNotFoundException;
import org.operaton.bpm.engine.exception.cmmn.CaseIllegalStateTransitionException;
import org.operaton.bpm.engine.impl.cmmn.cmd.CaseExecutionVariableCmd;
import org.operaton.bpm.engine.impl.cmmn.cmd.CloseCaseInstanceCmd;
import org.operaton.bpm.engine.impl.cmmn.cmd.CompleteCaseExecutionCmd;
import org.operaton.bpm.engine.impl.cmmn.cmd.DisableCaseExecutionCmd;
import org.operaton.bpm.engine.impl.cmmn.cmd.ManualStartCaseExecutionCmd;
import org.operaton.bpm.engine.impl.cmmn.cmd.ReenableCaseExecutionCmd;
import org.operaton.bpm.engine.impl.cmmn.cmd.TerminateCaseExecutionCmd;
import org.operaton.bpm.engine.impl.interceptor.Command;
import org.operaton.bpm.engine.impl.interceptor.CommandContext;
import org.operaton.bpm.engine.impl.interceptor.CommandExecutor;
import org.operaton.bpm.engine.runtime.CaseExecutionCommandBuilder;
import org.operaton.bpm.engine.variable.VariableMap;
import org.operaton.bpm.engine.variable.impl.VariableMapImpl;

/**
 * @author Roman Smirnov
 *
 */
public class CaseExecutionCommandBuilderImpl implements CaseExecutionCommandBuilder {

  protected CommandExecutor commandExecutor;
  protected CommandContext commandContext;

  protected String caseExecutionId;
  protected VariableMapImpl variables;
  protected VariableMapImpl variablesLocal;
  protected Collection<String> variableDeletions;
  protected Collection<String> variableLocalDeletions;

  public CaseExecutionCommandBuilderImpl(CommandExecutor commandExecutor, String caseExecutionId) {
    this(caseExecutionId);
    ensureNotNull("commandExecutor", commandExecutor);
    this.commandExecutor = commandExecutor;
  }

  public CaseExecutionCommandBuilderImpl(CommandContext commandContext, String caseExecutionId) {
    this(caseExecutionId);
    ensureNotNull("commandContext", commandContext);
    this.commandContext = commandContext;
  }

  private CaseExecutionCommandBuilderImpl(String caseExecutionId) {
    this.caseExecutionId = caseExecutionId;
  }

  @Override
  public CaseExecutionCommandBuilder setVariable(String variableName, Object variableValue) {
    ensureNotNull(NotValidException.class, "variableName", variableName);
    ensureVariableShouldNotBeRemoved(variableName);
    ensureVariablesInitialized();
    variables.put(variableName, variableValue);
    return this;
  }

  @Override
  public CaseExecutionCommandBuilder setVariables(Map<String, Object> variables) {
    if (variables != null) {
      ensureVariablesShouldNotBeRemoved(variables.keySet());
      ensureVariablesInitialized();
      this.variables.putAll(variables);
    }
    return this;
  }

  @Override
  public CaseExecutionCommandBuilder setVariableLocal(String localVariableName, Object localVariableValue) {
    ensureNotNull(NotValidException.class, "localVariableName", localVariableName);
    ensureVariableShouldNotBeRemoved(localVariableName);
    ensureVariablesLocalInitialized();
    variablesLocal.put(localVariableName, localVariableValue);
    return this;
  }

  @Override
  public CaseExecutionCommandBuilder setVariablesLocal(Map<String, Object> variablesLocal) {
    if (variablesLocal != null) {
      ensureVariablesShouldNotBeRemoved(variablesLocal.keySet());
      ensureVariablesLocalInitialized();
      this.variablesLocal.putAll(variablesLocal);
    }
    return this;
  }

  @Override
  public CaseExecutionCommandBuilder removeVariable(String variableName) {
    ensureNotNull(NotValidException.class, "variableName", variableName);
    ensureVariableShouldNotBeSet(variableName);
    ensureVariableDeletionsInitialized();
    variableDeletions.add(variableName);
    return this;
  }

  @Override
  public CaseExecutionCommandBuilder removeVariables(Collection<String> variableNames) {
    if (variableNames != null) {
      ensureVariablesShouldNotBeSet(variableNames);
      ensureVariableDeletionsInitialized();
      variableDeletions.addAll(variableNames);
    }
    return this;
  }

  @Override
  public CaseExecutionCommandBuilder removeVariableLocal(String variableName) {
    ensureNotNull(NotValidException.class, "localVariableName", variableName);
    ensureVariableShouldNotBeSet(variableName);
    ensureVariableDeletionsLocalInitialized();
    variableLocalDeletions.add(variableName);
    return this;
  }

  @Override
  public CaseExecutionCommandBuilder removeVariablesLocal(Collection<String> variableNames) {
    if (variableNames != null) {
      ensureVariablesShouldNotBeSet(variableNames);
      ensureVariableDeletionsLocalInitialized();
      variableLocalDeletions.addAll(variableNames);
    }
    return this;
  }

  protected void ensureVariablesShouldNotBeRemoved(Collection<String> variableNames) {
    for (String variableName : variableNames) {
      ensureVariableShouldNotBeRemoved(variableName);
    }
  }

  protected void ensureVariableShouldNotBeRemoved(String variableName) {
    if ((variableDeletions != null && variableDeletions.contains(variableName))
        || (variableLocalDeletions != null && variableLocalDeletions.contains(variableName))) {
      throw new NotValidException("Cannot set and remove a variable with the same variable name: '"+variableName+"' within a command.");
    }
  }

  protected void ensureVariablesShouldNotBeSet(Collection<String> variableNames) {
    for (String variableName : variableNames) {
      ensureVariableShouldNotBeSet(variableName);
    }
  }

  protected void ensureVariableShouldNotBeSet(String variableName) {
    if ((variables != null && variables.keySet().contains(variableName))
        || (variablesLocal != null && variablesLocal.keySet().contains(variableName))) {
      throw new NotValidException("Cannot set and remove a variable with the same variable name: '"+variableName+"' within a command.");
    }
  }

  protected void ensureVariablesInitialized() {
    if (this.variables == null) {
      this.variables = new VariableMapImpl();
    }
  }

  protected void ensureVariablesLocalInitialized() {
    if (this.variablesLocal == null) {
      this.variablesLocal = new VariableMapImpl();
    }
  }

  protected void ensureVariableDeletionsInitialized() {
    if (variableDeletions == null) {
      variableDeletions = new ArrayList<>();
    }
  }

  protected void ensureVariableDeletionsLocalInitialized() {
    if (variableLocalDeletions == null) {
      variableLocalDeletions = new ArrayList<>();
    }
  }


  @Override
  public void execute() {
    CaseExecutionVariableCmd command = new CaseExecutionVariableCmd(this);
    executeCommand(command);
  }

  @Override
  public void manualStart() {
    ManualStartCaseExecutionCmd command = new ManualStartCaseExecutionCmd(this);
    executeCommand(command);
  }

  @Override
  public void disable() {
    DisableCaseExecutionCmd command = new DisableCaseExecutionCmd(this);
    executeCommand(command);
  }

  @Override
  public void reenable() {
    ReenableCaseExecutionCmd command = new ReenableCaseExecutionCmd(this);
    executeCommand(command);
  }

  @Override
  public void complete() {
    CompleteCaseExecutionCmd command = new CompleteCaseExecutionCmd(this);
    executeCommand(command);
  }

  @Override
  public void close() {
    CloseCaseInstanceCmd command = new CloseCaseInstanceCmd(this);
    executeCommand(command);
  }

  @Override
  public void terminate() {
    TerminateCaseExecutionCmd command = new TerminateCaseExecutionCmd(this);
    executeCommand(command);
  }

  protected void executeCommand(Command<?> command) {
    try {
      if(commandExecutor != null) {
        commandExecutor.execute(command);
      } else {
        command.execute(commandContext);
      }

    } catch (NullValueException e) {
      throw new NotValidException(e.getMessage(), e);

    } catch (CaseExecutionNotFoundException | CaseDefinitionNotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);
    } catch (CaseIllegalStateTransitionException e) {
      throw new NotAllowedException(e.getMessage(), e);

    }

  }

  // getters ////////////////////////////////////////////////////////////////////////////////

  public String getCaseExecutionId() {
    return caseExecutionId;
  }

  public VariableMap getVariables() {
    return variables;
  }

  public VariableMap getVariablesLocal() {
    return variablesLocal;
  }

  public Collection<String> getVariableDeletions() {
    return variableDeletions;
  }

  public Collection<String> getVariableLocalDeletions() {
    return variableLocalDeletions;
  }

}

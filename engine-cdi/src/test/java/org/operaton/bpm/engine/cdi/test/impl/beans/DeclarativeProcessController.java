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
package org.operaton.bpm.engine.cdi.test.impl.beans;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.operaton.bpm.engine.cdi.annotation.CompleteTask;
import org.operaton.bpm.engine.cdi.annotation.ProcessVariable;
import org.operaton.bpm.engine.cdi.annotation.ProcessVariableLocalTyped;
import org.operaton.bpm.engine.cdi.annotation.ProcessVariableTyped;
import org.operaton.bpm.engine.cdi.annotation.StartProcess;
import org.operaton.bpm.engine.variable.Variables;
import org.operaton.bpm.engine.variable.value.StringValue;
import org.operaton.bpm.engine.variable.value.TypedValue;

/**
 *
 * @author Daniel Meyer
 */
@Dependent
public class DeclarativeProcessController {

  @ProcessVariable
  String name; // this is going to be set as a process variable

  @ProcessVariableTyped
  String untypedName;

  @ProcessVariableTyped
  StringValue typedName;

  @Inject
  @ProcessVariableTyped
  TypedValue injectedValue;

  @Inject
  @ProcessVariableLocalTyped
  TypedValue injectedLocalValue;

  @StartProcess("keyOfTheProcess")
  public void startProcessByKey() {
    name = "operaton";
    untypedName = "untypedName";
    typedName = Variables.stringValue("typedName");
  }

  @CompleteTask(endConversation = false)
  public void completeTask() {
    // behavior defined by annotation
  }

  @CompleteTask(endConversation = true)
  public void completeTaskEndConversation() {
    // behavior defined by annotation
  }

  public TypedValue getInjectedValue() {
    return injectedValue;
  }

  public TypedValue getInjectedLocalValue() {
    return injectedLocalValue;
  }

}

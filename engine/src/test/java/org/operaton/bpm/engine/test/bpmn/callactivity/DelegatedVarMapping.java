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
package org.operaton.bpm.engine.test.bpmn.callactivity;

import org.operaton.bpm.engine.delegate.DelegateExecution;
import org.operaton.bpm.engine.delegate.DelegateVariableMapping;
import org.operaton.bpm.engine.delegate.VariableScope;
import org.operaton.bpm.engine.variable.VariableMap;

/**
 * Represents an example implementation for the {@link DelegateVariableMapping} interface.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class DelegatedVarMapping implements DelegateVariableMapping {

  @Override
  public void mapInputVariables(DelegateExecution execution, VariableMap variables) {
    variables.putValue("TestInputVar", "inValue");
  }

  @Override
  public void mapOutputVariables(DelegateExecution execution, VariableScope subInstance) {
    execution.setVariable("TestOutputVar", "outValue");
  }
}

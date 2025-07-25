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
package org.operaton.bpm.engine.rest.dto.task;

import org.operaton.bpm.engine.rest.dto.VariableValueDto;

import java.util.Map;

public class CompleteTaskDto {

  private Map<String, VariableValueDto> variables;
  private boolean withVariablesInReturn;

  public Map<String, VariableValueDto> getVariables() {
    return variables;
  }

  public void setVariables(Map<String, VariableValueDto> variables) {
    this.variables = variables;
  }

  public boolean isWithVariablesInReturn() {
    return withVariablesInReturn;
  }

  public void setWithVariablesInReturn(boolean withVariablesInReturn) {
    this.withVariablesInReturn = withVariablesInReturn;
  }
}

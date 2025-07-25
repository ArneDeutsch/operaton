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
package org.operaton.bpm.engine.impl.persistence.entity;

import org.operaton.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.operaton.bpm.engine.impl.core.variable.scope.VariableStore.VariableStoreObserver;

/**
 * @author Thorben Lindhauer
 *
 */
public class CaseExecutionEntityReferencer implements VariableStoreObserver<VariableInstanceEntity> {

  protected CaseExecutionEntity caseExecution;

  public CaseExecutionEntityReferencer(CaseExecutionEntity caeExecution) {
    this.caseExecution = caeExecution;
  }

  @Override
  public void onAdd(VariableInstanceEntity variable) {
    variable.setCaseExecution(caseExecution);
  }

  @Override
  public void onRemove(VariableInstanceEntity variable) {
    variable.setCaseExecution(null);
  }

}

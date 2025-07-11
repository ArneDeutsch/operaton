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
package org.operaton.bpm.engine.impl.pvm.runtime.operation;

import org.operaton.bpm.engine.ActivityTypes;
import org.operaton.bpm.engine.impl.bpmn.helper.BpmnProperties;
import org.operaton.bpm.engine.impl.core.model.CoreModelElement;
import org.operaton.bpm.engine.impl.pvm.PvmActivity;
import org.operaton.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * <p>Base atomic operation used for implementing atomic operations which
 * create a new concurrent execution for executing an activity. This atomic
 * operation makes sure the execution is created under the correct parent.</p>
 *
 * @author Thorben Lindhauer
 * @author Daniel Meyer
 * @author Roman Smirnov
 *
 */
public abstract class PvmAtomicOperationCreateConcurrentExecution implements PvmAtomicOperation {

  @Override
  public void execute(PvmExecutionImpl execution) {

    // Invariant: execution is the Scope Execution for the activity's flow scope.

    PvmActivity activityToStart = execution.getNextActivity();
    execution.setNextActivity(null);

    PvmExecutionImpl propagatingExecution = execution.createConcurrentExecution();

    // set next activity on propagating execution
    propagatingExecution.setActivity(activityToStart);
    setDelayedPayloadToNewScope(propagatingExecution, (CoreModelElement) activityToStart);
    concurrentExecutionCreated(propagatingExecution);
  }

  protected abstract void concurrentExecutionCreated(PvmExecutionImpl propagatingExecution);

  @Override
  public boolean isAsync(PvmExecutionImpl execution) {
    return false;
  }

  protected void setDelayedPayloadToNewScope(PvmExecutionImpl execution, CoreModelElement scope) {
    String activityType = (String) scope.getProperty(BpmnProperties.TYPE.getName());
    if (ActivityTypes.START_EVENT_MESSAGE.equals(activityType) // Event subprocess message start event
        || ActivityTypes.BOUNDARY_MESSAGE.equals(activityType)) {
      PvmExecutionImpl processInstance = execution.getProcessInstance();
      if (processInstance.getPayloadForTriggeredScope() != null) {
        execution.setVariablesLocal(processInstance.getPayloadForTriggeredScope());
        // clear the process instance
        processInstance.setPayloadForTriggeredScope(null);
      }
    }
  }

}

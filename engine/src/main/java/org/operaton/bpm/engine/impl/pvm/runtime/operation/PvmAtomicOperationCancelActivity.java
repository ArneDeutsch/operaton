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
import org.operaton.bpm.engine.impl.pvm.process.ActivityStartBehavior;
import org.operaton.bpm.engine.impl.pvm.runtime.LegacyBehavior;
import org.operaton.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * Implements {@link ActivityStartBehavior#CANCEL_EVENT_SCOPE}.
 *
 * @author Throben Lindhauer
 * @author Daniel Meyer
 * @author Roman Smirnov
 *
 */
public abstract class PvmAtomicOperationCancelActivity implements PvmAtomicOperation {

  @Override
  public void execute(PvmExecutionImpl execution) {

    // Assumption: execution is scope
    PvmActivity cancellingActivity = execution.getNextActivity();
    execution.setNextActivity(null);

    // first, cancel and destroy the current scope
    execution.setActive(true);

    PvmExecutionImpl propagatingExecution = null;

    if(LegacyBehavior.isConcurrentScope(execution)) {
      // this is legacy behavior
      LegacyBehavior.cancelConcurrentScope(execution, (PvmActivity) cancellingActivity.getEventScope());
      propagatingExecution = execution;
    }
    else {
      // Unlike PvmAtomicOperationTransitionDestroyScope this needs to use delete() (instead of destroy() and remove()).
      // The reason is that PvmAtomicOperationTransitionDestroyScope is executed when a scope (or non scope) is left using
      // a sequence flow. In that case the execution will have completed all the work inside the current activity
      // and will have no more child executions. In PvmAtomicOperationCancelScope the scope is cancelled due to
      // a boundary event firing. In that case the execution has not completed all the work in the current scope / activity
      // and it is necessary to delete the complete hierarchy of executions below and including the execution itself.
      execution.deleteCascade("Cancel scope activity "+cancellingActivity+" executed.");
      propagatingExecution = execution.getParent();
    }

    propagatingExecution.setActivity(cancellingActivity);
    setDelayedPayloadToNewScope(propagatingExecution, (CoreModelElement) cancellingActivity);
    propagatingExecution.setActive(true);
    propagatingExecution.setEnded(false);
    activityCancelled(propagatingExecution);
  }

  protected abstract void activityCancelled(PvmExecutionImpl execution);

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

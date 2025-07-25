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
package org.operaton.bpm.engine.impl.bpmn.behavior;

import org.operaton.bpm.engine.impl.bpmn.delegate.ActivityBehaviorInvocation;
import org.operaton.bpm.engine.impl.bpmn.delegate.ActivityBehaviorSignalInvocation;
import org.operaton.bpm.engine.impl.context.Context;
import org.operaton.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.operaton.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.operaton.bpm.engine.impl.pvm.delegate.SignallableActivityBehavior;

/**
 * @author Roman Smirnov
 *
 */
public class CustomActivityBehavior implements ActivityBehavior, SignallableActivityBehavior {

  protected ActivityBehavior delegateActivityBehavior;

  public CustomActivityBehavior(ActivityBehavior activityBehavior) {
    this.delegateActivityBehavior = activityBehavior;
  }

  @Override
  public void execute(ActivityExecution execution) throws Exception {
    Context
      .getProcessEngineConfiguration()
      .getDelegateInterceptor()
      .handleInvocation(new ActivityBehaviorInvocation(delegateActivityBehavior, execution));
  }

  @Override
  public void signal(ActivityExecution execution, String signalEvent, Object signalData) throws Exception {
    Context
      .getProcessEngineConfiguration()
      .getDelegateInterceptor()
      .handleInvocation(new ActivityBehaviorSignalInvocation((SignallableActivityBehavior) delegateActivityBehavior, execution, signalEvent, signalData));
  }

  public ActivityBehavior getDelegateActivityBehavior() {
    return delegateActivityBehavior;
  }

}

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
package org.operaton.bpm.engine.impl.cmd;

import java.util.Collections;
import java.util.Set;

import org.operaton.bpm.engine.ProcessEngineException;
import org.operaton.bpm.engine.impl.cfg.CommandChecker;
import org.operaton.bpm.engine.impl.context.Context;
import org.operaton.bpm.engine.impl.interceptor.Command;
import org.operaton.bpm.engine.impl.interceptor.CommandContext;

/**
 * @author Daniel Meyer
 *
 */
public class UnregisterProcessApplicationCmd implements Command<Void> {

  protected boolean removeProcessesFromCache;
  protected Set<String> deploymentIds;

  public UnregisterProcessApplicationCmd(String deploymentId, boolean removeProcessesFromCache) {
    this(Collections.singleton(deploymentId), removeProcessesFromCache);
  }

  public UnregisterProcessApplicationCmd(Set<String> deploymentIds, boolean removeProcessesFromCache) {
    this.deploymentIds = deploymentIds;
    this.removeProcessesFromCache = removeProcessesFromCache;
  }

  @Override
  public Void execute(CommandContext commandContext) {

    if(deploymentIds == null) {
      throw new ProcessEngineException("Deployment Ids cannot be null.");
    }

    commandContext.getAuthorizationManager().checkOperatonAdminOrPermission(CommandChecker::checkUnregisterProcessApplication);

    Context.getProcessEngineConfiguration()
      .getProcessApplicationManager()
      .unregisterProcessApplicationForDeployments(deploymentIds, removeProcessesFromCache);

    return null;

  }

}

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

import org.operaton.bpm.engine.history.UserOperationLogEntry;
import org.operaton.bpm.engine.impl.interceptor.CommandContext;
import org.operaton.bpm.engine.impl.persistence.entity.PropertyChange;
import org.operaton.bpm.engine.impl.persistence.entity.TaskEntity;
import org.operaton.bpm.engine.impl.persistence.entity.UserOperationLogManager;

/**
 * @author Daniel Meyer
 *
 */
public class AddUserIdentityLinkCmd extends AbstractAddIdentityLinkCmd {

  private static final long serialVersionUID = 1L;

  public AddUserIdentityLinkCmd(String taskId, String userId, String type) {
    super(taskId, userId, null, type);
  }

  @Override
  protected void logOperation(CommandContext context, TaskEntity task) {
    PropertyChange propertyChange = new PropertyChange(type, null, userId);
    UserOperationLogManager logManager = context.getOperationLogManager();

    logManager.logLinkOperation(UserOperationLogEntry.OPERATION_TYPE_ADD_USER_LINK, task, propertyChange);
  }

}

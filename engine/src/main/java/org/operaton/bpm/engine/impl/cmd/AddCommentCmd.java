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

import static org.operaton.bpm.engine.ProcessEngineConfiguration.HISTORY_REMOVAL_TIME_STRATEGY_START;
import static org.operaton.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;
import java.util.Date;
import org.operaton.bpm.engine.ProcessEngineException;
import org.operaton.bpm.engine.impl.context.Context;
import org.operaton.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.operaton.bpm.engine.impl.interceptor.Command;
import org.operaton.bpm.engine.impl.interceptor.CommandContext;
import org.operaton.bpm.engine.impl.persistence.entity.CommentEntity;
import org.operaton.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.operaton.bpm.engine.impl.persistence.entity.TaskEntity;
import org.operaton.bpm.engine.impl.util.ClockUtil;
import org.operaton.bpm.engine.task.Comment;
import org.operaton.bpm.engine.task.Event;


/**
 * @author Tom Baeyens
 */
public class AddCommentCmd implements Command<Comment>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String taskId;
  protected String processInstanceId;
  protected String message;

  public AddCommentCmd(String taskId, String processInstanceId, String message) {
    this.taskId = taskId;
    this.processInstanceId = processInstanceId;
    this.message = message;
  }

  @Override
  public Comment execute(CommandContext commandContext) {

    if (processInstanceId == null && taskId == null) {
      throw new ProcessEngineException("Process instance id and task id is null");
    }

    ensureNotNull("Message", message);

    String userId = commandContext.getAuthenticatedUserId();
    CommentEntity comment = new CommentEntity();
    comment.setUserId(userId);
    comment.setType(CommentEntity.TYPE_COMMENT);
    comment.setTime(ClockUtil.getCurrentTime());
    comment.setTaskId(taskId);
    comment.setProcessInstanceId(processInstanceId);
    comment.setAction(Event.ACTION_ADD_COMMENT);

    ExecutionEntity execution = getExecution(commandContext);
    if (execution != null) {
      comment.setRootProcessInstanceId(execution.getRootProcessInstanceId());
    }

    if (isHistoryRemovalTimeStrategyStart()) {
      provideRemovalTime(comment);
    }

    String eventMessage = comment.toEventMessage(message);
    comment.setMessage(eventMessage);

    comment.setFullMessage(message);

    commandContext
      .getCommentManager()
      .insert(comment);

    TaskEntity task = getTask(commandContext);
    if (task != null) {
      task.triggerUpdateEvent();
    }

    return comment;
  }

  protected ExecutionEntity getExecution(CommandContext commandContext) {

    if (taskId != null) {
      TaskEntity task = getTask(commandContext);
      if (task != null) {
        return task.getExecution();
      } else {
        return null;
      }
    } else {
      return getProcessInstance(commandContext);
    }
  }

  protected ExecutionEntity getProcessInstance(CommandContext commandContext) {
    if (processInstanceId != null) {
      return commandContext.getExecutionManager().findExecutionById(processInstanceId);
    } else {
      return null;
    }
  }

  protected TaskEntity getTask(CommandContext commandContext) {
    if (taskId != null) {
      return commandContext.getTaskManager().findTaskById(taskId);
    } else {
      return null;
    }
  }

  protected boolean isHistoryRemovalTimeStrategyStart() {
    return HISTORY_REMOVAL_TIME_STRATEGY_START.equals(getHistoryRemovalTimeStrategy());
  }

  protected String getHistoryRemovalTimeStrategy() {
    return Context.getProcessEngineConfiguration()
      .getHistoryRemovalTimeStrategy();
  }

  protected HistoricProcessInstanceEventEntity getHistoricRootProcessInstance(String rootProcessInstanceId) {
    return Context.getCommandContext()
      .getDbEntityManager()
      .selectById(HistoricProcessInstanceEventEntity.class, rootProcessInstanceId);
  }

  protected void provideRemovalTime(CommentEntity comment) {
    String rootProcessInstanceId = comment.getRootProcessInstanceId();
    if (rootProcessInstanceId != null) {
      HistoricProcessInstanceEventEntity historicRootProcessInstance = getHistoricRootProcessInstance(rootProcessInstanceId);

      if (historicRootProcessInstance != null) {
        Date removalTime = historicRootProcessInstance.getRemovalTime();
        comment.setRemovalTime(removalTime);
      }
    }
  }

}

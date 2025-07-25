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

import java.util.Date;

import org.operaton.bpm.engine.BadUserRequestException;
import org.operaton.bpm.engine.form.OperatonFormRef;
import org.operaton.bpm.engine.rest.dto.converter.DelegationStateConverter;
import org.operaton.bpm.engine.task.DelegationState;
import org.operaton.bpm.engine.task.Task;

public class TaskDto {

  private String id;
  private String name;
  private String assignee;
  private Date created;
  private Date due;
  private Date followUp;
  private Date lastUpdated;
  private String delegationState;
  private String description;
  private String executionId;
  private String owner;
  private String parentTaskId;
  private int priority;
  private String processDefinitionId;
  private String processInstanceId;
  private String taskDefinitionKey;
  private String caseExecutionId;
  private String caseInstanceId;
  private String caseDefinitionId;
  private boolean suspended;
  private String formKey;
  private OperatonFormRef operatonFormRef;
  private String tenantId;
  /**
   * Returns task State of task
   */
  private String taskState;

  public TaskDto() {
  }

  public TaskDto(Task task) {
    this.id = task.getId();
    this.name = task.getName();
    this.assignee = task.getAssignee();
    this.created = task.getCreateTime();
    this.lastUpdated = task.getLastUpdated();
    this.due = task.getDueDate();
    this.followUp = task.getFollowUpDate();

    if (task.getDelegationState() != null) {
      this.delegationState = task.getDelegationState().toString();
    }

    this.description = task.getDescription();
    this.executionId = task.getExecutionId();
    this.owner = task.getOwner();
    this.parentTaskId = task.getParentTaskId();
    this.priority = task.getPriority();
    this.processDefinitionId = task.getProcessDefinitionId();
    this.processInstanceId = task.getProcessInstanceId();
    this.taskDefinitionKey = task.getTaskDefinitionKey();
    this.caseDefinitionId = task.getCaseDefinitionId();
    this.caseExecutionId = task.getCaseExecutionId();
    this.caseInstanceId = task.getCaseInstanceId();
    this.suspended = task.isSuspended();
    this.tenantId = task.getTenantId();
    this.taskState = task.getTaskState();
    try {
      this.formKey = task.getFormKey();
      this.operatonFormRef = task.getOperatonFormRef();
    }
    catch (BadUserRequestException e) {
      // ignore (initializeFormKeys was not called)
    }
  }
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAssignee() {
    return assignee;
  }

  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }

  public Date getCreated() {
    return created;
  }

  public Date getDue() {
    return due;
  }

  public void setDue(Date due) {
    this.due = due;
  }

  public String getDelegationState() {
    return delegationState;
  }

  public void setDelegationState(String delegationState) {
    this.delegationState = delegationState;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getExecutionId() {
    return executionId;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getParentTaskId() {
    return parentTaskId;
  }

  public void setParentTaskId(String parentTaskId) {
    this.parentTaskId = parentTaskId;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getTaskDefinitionKey() {
    return taskDefinitionKey;
  }

  public Date getFollowUp() {
    return followUp;
  }

  public void setFollowUp(Date followUp) {
    this.followUp = followUp;
  }

  public Date getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  public String getCaseDefinitionId() {
    return caseDefinitionId;
  }

  public String getCaseExecutionId() {
    return caseExecutionId;
  }

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public void setCaseInstanceId(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
  }

  public boolean isSuspended() {
    return suspended;
  }

  public String getFormKey() {
    return formKey;
  }

  public OperatonFormRef getOperatonFormRef() {
    return operatonFormRef;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public String getTaskState() {
    return taskState;
  }
  public void setTaskState(String taskState) {
    this.taskState = taskState;
  }

  public static TaskDto fromEntity(Task task) {
    TaskDto dto = new TaskDto();
    dto.id = task.getId();
    dto.name = task.getName();
    dto.assignee = task.getAssignee();
    dto.created = task.getCreateTime();
    dto.lastUpdated = task.getLastUpdated();
    dto.due = task.getDueDate();
    dto.followUp = task.getFollowUpDate();

    if (task.getDelegationState() != null) {
      dto.delegationState = task.getDelegationState().toString();
    }

    dto.description = task.getDescription();
    dto.executionId = task.getExecutionId();
    dto.owner = task.getOwner();
    dto.parentTaskId = task.getParentTaskId();
    dto.priority = task.getPriority();
    dto.processDefinitionId = task.getProcessDefinitionId();
    dto.processInstanceId = task.getProcessInstanceId();
    dto.taskDefinitionKey = task.getTaskDefinitionKey();
    dto.caseDefinitionId = task.getCaseDefinitionId();
    dto.caseExecutionId = task.getCaseExecutionId();
    dto.caseInstanceId = task.getCaseInstanceId();
    dto.suspended = task.isSuspended();
    dto.tenantId = task.getTenantId();
    dto.taskState = task.getTaskState();

    try {
      dto.formKey = task.getFormKey();
      dto.operatonFormRef = task.getOperatonFormRef();
    }
    catch (BadUserRequestException e) {
      // ignore (initializeFormKeys was not called)
    }
    return dto;
  }

  public void updateTask(Task task) {
    task.setName(getName());
    task.setDescription(getDescription());
    task.setPriority(getPriority());
    task.setAssignee(getAssignee());
    task.setOwner(getOwner());

    DelegationState state = null;
    if (getDelegationState() != null) {
      DelegationStateConverter converter = new DelegationStateConverter();
      state = converter.convertQueryParameterToType(getDelegationState());
    }
    task.setDelegationState(state);

    task.setDueDate(getDue());
    task.setFollowUpDate(getFollowUp());
    task.setParentTaskId(getParentTaskId());
    task.setCaseInstanceId(getCaseInstanceId());
    task.setTenantId(getTenantId());
  }

}

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
package org.operaton.bpm.engine.rest.helper;

import org.operaton.bpm.engine.form.OperatonFormRef;
import org.operaton.bpm.engine.impl.form.OperatonFormRefImpl;
import org.operaton.bpm.engine.task.DelegationState;
import org.operaton.bpm.engine.task.Task;

import java.util.Date;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockTaskBuilder {

  private String id;
  private String name;
  private String assignee;
  private Date createTime;
  private Date lastUpdated;
  private Date dueDate;
  private Date followUpDate;
  private DelegationState delegationState;
  private String description;
  private String executionId;
  private String owner;
  private String parentTaskId;
  private int priority;
  private String processDefinitionId;
  private String processInstanceId;
  private String taskDefinitionKey;
  private String caseDefinitionId;
  private String caseInstanceId;
  private String caseExecutionId;
  private String formKey;
  private OperatonFormRef operatonFormRef;
  private String tenantId;
  private boolean hasAttachment;

  private boolean hasComment;



  private String taskState;

  public MockTaskBuilder id(String id) {
    this.id = id;
    return this;
  }

  public MockTaskBuilder name(String name) {
    this.name = name;
    return this;
  }

  public MockTaskBuilder assignee(String assignee) {
    this.assignee = assignee;
    return this;
  }

  public MockTaskBuilder createTime(Date createTime) {
    this.createTime = createTime;
    return this;
  }

  public MockTaskBuilder lastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
    return this;
  }

  public MockTaskBuilder dueDate(Date dueDate) {
    this.dueDate = dueDate;
    return this;
  }

  public MockTaskBuilder followUpDate(Date followUpDate) {
    this.followUpDate = followUpDate;
    return this;
  }

  public MockTaskBuilder delegationState(DelegationState delegationState) {
    this.delegationState = delegationState;
    return this;
  }

  public MockTaskBuilder description(String description) {
    this.description = description;
    return this;
  }

  public MockTaskBuilder executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  public MockTaskBuilder owner(String owner) {
    this.owner = owner;
    return this;
  }

  public MockTaskBuilder parentTaskId(String parentTaskId) {
    this.parentTaskId = parentTaskId;
    return this;
  }

  public MockTaskBuilder priority(int priority) {
    this.priority = priority;
    return this;
  }

  public MockTaskBuilder processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public MockTaskBuilder processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  public MockTaskBuilder taskDefinitionKey(String taskDefinitionKey) {
    this.taskDefinitionKey = taskDefinitionKey;
    return this;
  }

  public MockTaskBuilder caseDefinitionId(String caseDefinitionId) {
    this.caseDefinitionId = caseDefinitionId;
    return this;
  }

  public MockTaskBuilder caseInstanceId(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
    return this;
  }

  public MockTaskBuilder caseExecutionId(String caseExecutionId) {
    this.caseExecutionId = caseExecutionId;
    return this;
  }

  public MockTaskBuilder formKey(String exampleFormKey) {
    this.formKey = exampleFormKey;
    return this;
  }

  public MockTaskBuilder operatonFormRef(String key, String binding, Integer version) {
    OperatonFormRefImpl formRef = new OperatonFormRefImpl(key, binding);
    formRef.setVersion(version);
    this.operatonFormRef = formRef;
    return this;
  }

  public MockTaskBuilder tenantId(String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  public MockTaskBuilder taskState(String taskState) {
    this.taskState = taskState;
    return this;
  }

  public MockTaskBuilder hasAttachment(boolean hasAttachment) {
    this.hasAttachment = hasAttachment;
    return this;
  }

  public MockTaskBuilder hasComment(boolean hasComment) {
    this.hasComment = hasComment;
    return this;
  }

  public Task build() {
    Task mockTask = mock(Task.class);
    when(mockTask.getId()).thenReturn(id);
    when(mockTask.getName()).thenReturn(name);
    when(mockTask.getAssignee()).thenReturn(assignee);
    when(mockTask.getCreateTime()).thenReturn(createTime);
    when(mockTask.getLastUpdated()).thenReturn(lastUpdated);
    when(mockTask.getDueDate()).thenReturn(dueDate);
    when(mockTask.getFollowUpDate()).thenReturn(followUpDate);
    when(mockTask.getDelegationState()).thenReturn(delegationState);
    when(mockTask.getDescription()).thenReturn(description);
    when(mockTask.getExecutionId()).thenReturn(executionId);
    when(mockTask.getOwner()).thenReturn(owner);
    when(mockTask.getParentTaskId()).thenReturn(parentTaskId);
    when(mockTask.getPriority()).thenReturn(priority);
    when(mockTask.getProcessDefinitionId()).thenReturn(processDefinitionId);
    when(mockTask.getProcessInstanceId()).thenReturn(processInstanceId);
    when(mockTask.getTaskDefinitionKey()).thenReturn(taskDefinitionKey);
    when(mockTask.getCaseDefinitionId()).thenReturn(caseDefinitionId);
    when(mockTask.getCaseInstanceId()).thenReturn(caseInstanceId);
    when(mockTask.getCaseExecutionId()).thenReturn(caseExecutionId);
    when(mockTask.getFormKey()).thenReturn(formKey);
    when(mockTask.getOperatonFormRef()).thenReturn(operatonFormRef);
    when(mockTask.getTenantId()).thenReturn(tenantId);
    when(mockTask.getTaskState()).thenReturn(taskState);
    when(mockTask.hasAttachment()).thenReturn(hasAttachment);
    when(mockTask.hasComment()).thenReturn(hasComment);
    return mockTask;
  }

}

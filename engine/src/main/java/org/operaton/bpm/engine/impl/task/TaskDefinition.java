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
package org.operaton.bpm.engine.impl.task;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.operaton.bpm.engine.delegate.Expression;
import org.operaton.bpm.engine.delegate.TaskListener;
import org.operaton.bpm.engine.impl.form.FormDefinition;
import org.operaton.bpm.engine.impl.form.handler.TaskFormHandler;
import org.operaton.bpm.engine.impl.util.CollectionUtil;

/**
 * Container for task definition information gathered at parsing time.
 *
 * @author Joram Barrez
 */

public class TaskDefinition {
  protected String key;

  // assignment fields
  protected Expression nameExpression;
  protected Expression descriptionExpression;
  protected Expression assigneeExpression;
  protected Set<Expression> candidateUserIdExpressions = new HashSet<>();
  protected Set<Expression> candidateGroupIdExpressions = new HashSet<>();
  protected Expression dueDateExpression;
  protected Expression followUpDateExpression;
  protected Expression priorityExpression;

  // form fields
  protected TaskFormHandler taskFormHandler;
  protected FormDefinition formDefinition = new FormDefinition();

  // task listeners
  protected Map<String, List<TaskListener>> taskListeners = new HashMap<>();
  protected Map<String, List<TaskListener>> builtinTaskListeners = new HashMap<>();
  protected Map<String, List<TaskListener>> allTaskListeners = new HashMap<>();
  protected Map<String, TaskListener> timeoutTaskListeners = new HashMap<>();

  public TaskDefinition(TaskFormHandler taskFormHandler) {
    this.taskFormHandler = taskFormHandler;
  }

  // getters and setters //////////////////////////////////////////////////////

  public Expression getNameExpression() {
    return nameExpression;
  }

  public void setNameExpression(Expression nameExpression) {
    this.nameExpression = nameExpression;
  }

  public Expression getDescriptionExpression() {
    return descriptionExpression;
  }

  public void setDescriptionExpression(Expression descriptionExpression) {
    this.descriptionExpression = descriptionExpression;
  }

  public Expression getAssigneeExpression() {
    return assigneeExpression;
  }

  public void setAssigneeExpression(Expression assigneeExpression) {
    this.assigneeExpression = assigneeExpression;
  }

  public Set<Expression> getCandidateUserIdExpressions() {
    return candidateUserIdExpressions;
  }

  public void addCandidateUserIdExpression(Expression userId) {
    candidateUserIdExpressions.add(userId);
  }

  public Set<Expression> getCandidateGroupIdExpressions() {
    return candidateGroupIdExpressions;
  }

  public void addCandidateGroupIdExpression(Expression groupId) {
    candidateGroupIdExpressions.add(groupId);
  }

  public Expression getPriorityExpression() {
    return priorityExpression;
  }

  public void setPriorityExpression(Expression priorityExpression) {
    this.priorityExpression = priorityExpression;
  }

  public TaskFormHandler getTaskFormHandler() {
    return taskFormHandler;
  }

  public void setTaskFormHandler(TaskFormHandler taskFormHandler) {
    this.taskFormHandler = taskFormHandler;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public Expression getDueDateExpression() {
    return dueDateExpression;
  }

  public void setDueDateExpression(Expression dueDateExpression) {
    this.dueDateExpression = dueDateExpression;
  }

  public Expression getFollowUpDateExpression() {
    return followUpDateExpression;
  }

  public void setFollowUpDateExpression(Expression followUpDateExpression) {
    this.followUpDateExpression = followUpDateExpression;
  }

  public Map<String, List<TaskListener>> getTaskListeners() {
    return taskListeners;
  }

  public Map<String, List<TaskListener>> getBuiltinTaskListeners() {
    return builtinTaskListeners;
  }

  public void setTaskListeners(Map<String, List<TaskListener>> taskListeners) {
    this.taskListeners = taskListeners;
  }

  public List<TaskListener> getTaskListenersForEvent(String eventName) {
    return taskListeners.get(eventName);
  }

  public List<TaskListener> getBuiltinTaskListenersForEvent(String eventName) {
    return builtinTaskListeners.get(eventName);
  }

  public List<TaskListener> getAllTaskListenersForEvent(String eventName) {
    return allTaskListeners.get(eventName);
  }

  public TaskListener getTimeoutTaskListener(String timeoutId) {
    return timeoutTaskListeners.get(timeoutId);
  }

  public void addTaskListener(String eventName, TaskListener taskListener) {
    CollectionUtil.addToMapOfLists(taskListeners, eventName, taskListener);
    // re-calculate combined map to ensure order of listener execution
    populateAllTaskListeners();
  }

  public void addBuiltInTaskListener(String eventName, TaskListener taskListener) {
    CollectionUtil.addToMapOfLists(builtinTaskListeners, eventName, taskListener);
    // re-calculate combined map to ensure order of listener execution
    populateAllTaskListeners();
  }

  public void addTimeoutTaskListener(String timeoutId, TaskListener taskListener) {
    timeoutTaskListeners.put(timeoutId, taskListener);
  }

  public FormDefinition getFormDefinition() {
    return formDefinition;
  }

  public void setFormDefinition(FormDefinition formDefinition) {
    this.formDefinition = formDefinition;
  }

  public Expression getFormKey() {
    return formDefinition.getFormKey();
  }

  public void setFormKey(Expression formKey) {
    this.formDefinition.setFormKey(formKey);
  }

  public Expression getOperatonFormDefinitionKey() {
    return formDefinition.getOperatonFormDefinitionKey();
  }

  public String getOperatonFormDefinitionBinding() {
    return formDefinition.getOperatonFormDefinitionBinding();
  }

  public Expression getOperatonFormDefinitionVersion() {
    return formDefinition.getOperatonFormDefinitionVersion();
  }

  // helper methods ///////////////////////////////////////////////////////////

  protected void populateAllTaskListeners() {
    // reset allTaskListeners to build it from zero
    allTaskListeners = new HashMap<>();
    // ensure builtinTaskListeners are executed before regular taskListeners
    CollectionUtil.mergeMapsOfLists(allTaskListeners, builtinTaskListeners);
    CollectionUtil.mergeMapsOfLists(allTaskListeners, taskListeners);
  }
}

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
package org.operaton.bpm.engine.impl;

import static org.operaton.bpm.engine.impl.util.EnsureUtil.ensureNotContainsEmptyString;
import static org.operaton.bpm.engine.impl.util.EnsureUtil.ensureNotContainsNull;
import static org.operaton.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.operaton.bpm.engine.impl.util.EnsureUtil.ensureNotNull;
import static org.operaton.bpm.engine.impl.util.EnsureUtil.ensureNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.operaton.bpm.engine.BadUserRequestException;
import org.operaton.bpm.engine.ProcessEngineException;
import org.operaton.bpm.engine.history.HistoricProcessInstance;
import org.operaton.bpm.engine.history.HistoricProcessInstanceQuery;
import org.operaton.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.operaton.bpm.engine.impl.context.Context;
import org.operaton.bpm.engine.impl.interceptor.CommandContext;
import org.operaton.bpm.engine.impl.interceptor.CommandExecutor;
import org.operaton.bpm.engine.impl.util.ImmutablePair;
import org.operaton.bpm.engine.impl.util.CompareUtil;
import org.operaton.bpm.engine.impl.variable.serializer.VariableSerializers;

/**
 * @author Tom Baeyens
 * @author Falko Menge
 * @author Bernd Ruecker
 */
public class HistoricProcessInstanceQueryImpl extends AbstractVariableQueryImpl<HistoricProcessInstanceQuery, HistoricProcessInstance> implements HistoricProcessInstanceQuery {

  private static final long serialVersionUID = 1L;
  private static final String MSG_ALREADY_QUERYING = "Already querying for historic process instance with another state";
  protected String processInstanceId;
  protected String processDefinitionId;
  protected String processDefinitionName;
  protected String processDefinitionNameLike;
  protected String businessKey;
  protected String[] businessKeyIn;
  protected String businessKeyLike;
  protected boolean finished = false;
  protected boolean unfinished = false;
  protected boolean withJobsRetrying = false;
  protected boolean withIncidents = false;
  protected boolean withRootIncidents = false;
  protected String incidentType;
  protected String incidentStatus;
  protected String incidentMessage;
  protected String incidentMessageLike;
  protected String startedBy;
  protected boolean isRootProcessInstances;
  protected String superProcessInstanceId;
  protected String subProcessInstanceId;
  protected String superCaseInstanceId;
  protected String subCaseInstanceId;
  protected List<String> processKeyNotIn;
  protected Date startedBefore;
  protected Date startedAfter;
  protected Date finishedBefore;
  protected Date finishedAfter;
  protected Date executedActivityAfter;
  protected Date executedActivityBefore;
  protected Date executedJobAfter;
  protected Date executedJobBefore;
  protected String processDefinitionKey;
  protected String[] processDefinitionKeys;
  protected Set<String> processInstanceIds;
  protected String[] processInstanceIdNotIn;
  protected String[] tenantIds;
  protected boolean isTenantIdSet;
  protected String[] executedActivityIds;
  protected String[] activeActivityIds;
  protected String[] activityIds;
  protected String state;
  protected String[] incidentIds;

  protected String caseInstanceId;

  protected List<HistoricProcessInstanceQueryImpl> queries = new ArrayList<>(Collections.singletonList(this));
  protected boolean isOrQueryActive = false;

  protected Map<String, Set<QueryVariableValue>> queryVariableNameToValuesMap = new HashMap<>();

  public HistoricProcessInstanceQueryImpl() {
  }

  public HistoricProcessInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  @Override
  public HistoricProcessInstanceQueryImpl processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery processInstanceIds(Set<String> processInstanceIds) {
    ensureNotEmpty("Set of process instance ids", processInstanceIds);
    this.processInstanceIds = processInstanceIds;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery processInstanceIdNotIn(String... processInstanceIdNotIn){
    ensureNotNull("processInstanceIdNotIn", (Object[]) processInstanceIdNotIn);
    this.processInstanceIdNotIn = processInstanceIdNotIn;
    return this;
  }

  @Override
  public HistoricProcessInstanceQueryImpl processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery processDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery processDefinitionKeyIn(String... processDefinitionKeys) {
    ensureNotNull("processDefinitionKeys", (Object[]) processDefinitionKeys);
    this.processDefinitionKeys = processDefinitionKeys;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery processDefinitionName(String processDefinitionName) {
    this.processDefinitionName = processDefinitionName;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery processDefinitionNameLike(String nameLike) {
    this.processDefinitionNameLike = nameLike;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery processInstanceBusinessKey(String businessKey) {
    this.businessKey = businessKey;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery processInstanceBusinessKeyIn(String... businessKeyIn) {
    this.businessKeyIn = businessKeyIn;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery processInstanceBusinessKeyLike(String businessKeyLike) {
    this.businessKeyLike = businessKeyLike;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery finished() {
    this.finished = true;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery unfinished() {
    this.unfinished = true;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery withIncidents() {
    this.withIncidents = true;

    return this;
  }

  @Override
  public HistoricProcessInstanceQuery withRootIncidents() {
    this.withRootIncidents = true;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery incidentIdIn(String... incidentIds) {
    ensureNotNull("incidentIds", (Object[]) incidentIds);
    this.incidentIds = incidentIds;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery incidentType(String incidentType) {
    ensureNotNull("incident type", incidentType);
    this.incidentType = incidentType;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery incidentStatus(String status) {
    this.incidentStatus = status;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery incidentMessage(String incidentMessage) {
    ensureNotNull("incidentMessage", incidentMessage);
    this.incidentMessage = incidentMessage;

    return this;
  }

  @Override
  public HistoricProcessInstanceQuery incidentMessageLike(String incidentMessageLike) {
    ensureNotNull("incidentMessageLike", incidentMessageLike);
    this.incidentMessageLike = incidentMessageLike;

    return this;
  }

  @Override
  public HistoricProcessInstanceQuery withJobsRetrying(){
    this.withJobsRetrying = true;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery startedBy(String userId) {
    this.startedBy = userId;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery processDefinitionKeyNotIn(List<String> processDefinitionKeys) {
    ensureNotContainsNull("processDefinitionKeys", processDefinitionKeys);
    ensureNotContainsEmptyString("processDefinitionKeys", processDefinitionKeys);
    this.processKeyNotIn = processDefinitionKeys;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery startedAfter(Date date) {
    startedAfter = date;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery startedBefore(Date date) {
    startedBefore = date;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery finishedAfter(Date date) {
    finishedAfter = date;
    finished = true;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery finishedBefore(Date date) {
    finishedBefore = date;
    finished = true;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery rootProcessInstances() {
    if (superProcessInstanceId != null) {
      throw new BadUserRequestException("Invalid query usage: cannot set both rootProcessInstances and superProcessInstanceId");
    }
    if (superCaseInstanceId != null) {
      throw new BadUserRequestException("Invalid query usage: cannot set both rootProcessInstances and superCaseInstanceId");
    }
    isRootProcessInstances = true;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery superProcessInstanceId(String superProcessInstanceId) {
    if (isRootProcessInstances) {
      throw new BadUserRequestException("Invalid query usage: cannot set both rootProcessInstances and superProcessInstanceId");
    }
    this.superProcessInstanceId = superProcessInstanceId;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery subProcessInstanceId(String subProcessInstanceId) {
    this.subProcessInstanceId = subProcessInstanceId;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery superCaseInstanceId(String superCaseInstanceId) {
    if (isRootProcessInstances) {
      throw new BadUserRequestException("Invalid query usage: cannot set both rootProcessInstances and superCaseInstanceId");
    }
    this.superCaseInstanceId = superCaseInstanceId;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery subCaseInstanceId(String subCaseInstanceId) {
    this.subCaseInstanceId = subCaseInstanceId;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery caseInstanceId(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery tenantIdIn(String... tenantIds) {
    ensureNotNull("tenantIds", (Object[]) tenantIds);
    this.tenantIds = tenantIds;
    this.isTenantIdSet = true;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery withoutTenantId() {
    tenantIds = null;
    isTenantIdSet = true;
    return this;
  }

  @Override
  protected boolean hasExcludingConditions() {
    return super.hasExcludingConditions()
      || (finished && unfinished)
      || CompareUtil.areNotInAscendingOrder(startedAfter, startedBefore)
      || CompareUtil.areNotInAscendingOrder(finishedAfter, finishedBefore)
      || CompareUtil.elementIsContainedInList(processDefinitionKey, processKeyNotIn)
      || CompareUtil.elementIsNotContainedInList(processInstanceId, processInstanceIds)
      || CompareUtil.elementIsContainedInArray(processInstanceId, processInstanceIdNotIn)
      || CompareUtil.elementsAreContainedInArray(processInstanceIds, processInstanceIdNotIn);
  }

  @Override
  public HistoricProcessInstanceQuery orderByProcessInstanceBusinessKey() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByProcessInstanceBusinessKey() within 'or' query");
    }
    return orderBy(HistoricProcessInstanceQueryProperty.BUSINESS_KEY);
  }

  @Override
  public HistoricProcessInstanceQuery orderByProcessInstanceDuration() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByProcessInstanceDuration() within 'or' query");
    }
    return orderBy(HistoricProcessInstanceQueryProperty.DURATION);
  }

  @Override
  public HistoricProcessInstanceQuery orderByProcessInstanceStartTime() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByProcessInstanceStartTime() within 'or' query");
    }
    return orderBy(HistoricProcessInstanceQueryProperty.START_TIME);
  }

  @Override
  public HistoricProcessInstanceQuery orderByProcessInstanceEndTime() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByProcessInstanceEndTime() within 'or' query");
    }
    return orderBy(HistoricProcessInstanceQueryProperty.END_TIME);
  }

  @Override
  public HistoricProcessInstanceQuery orderByProcessDefinitionId() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByProcessDefinitionId() within 'or' query");
    }
    return orderBy(HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_ID);
  }

  @Override
  public HistoricProcessInstanceQuery orderByProcessDefinitionKey() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByProcessDefinitionKey() within 'or' query");
    }
    return orderBy(HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_KEY);
  }

  @Override
  public HistoricProcessInstanceQuery orderByProcessDefinitionName() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByProcessDefinitionName() within 'or' query");
    }
    return orderBy(HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_NAME);
  }

  @Override
  public HistoricProcessInstanceQuery orderByProcessDefinitionVersion() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByProcessDefinitionVersion() within 'or' query");
    }
    return orderBy(HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_VERSION);
  }

  @Override
  public HistoricProcessInstanceQuery orderByProcessInstanceId() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByProcessInstanceId() within 'or' query");
    }
    return orderBy(HistoricProcessInstanceQueryProperty.PROCESS_INSTANCE_ID_);
  }

  @Override
  public HistoricProcessInstanceQuery orderByTenantId() {
    if (isOrQueryActive) {
      throw new ProcessEngineException("Invalid query usage: cannot set orderByTenantId() within 'or' query");
    }
    return orderBy(HistoricProcessInstanceQueryProperty.TENANT_ID);
  }

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext
      .getHistoricProcessInstanceManager()
      .findHistoricProcessInstanceCountByQueryCriteria(this);
  }

  @Override
  public List<HistoricProcessInstance> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext
      .getHistoricProcessInstanceManager()
      .findHistoricProcessInstancesByQueryCriteria(this, page);
  }

  @Override
  public List<String> executeIdsList(CommandContext commandContext) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext
        .getHistoricProcessInstanceManager()
        .findHistoricProcessInstanceIds(this);
  }

  @Override
  public List<ImmutablePair<String, String>> executeDeploymentIdMappingsList(CommandContext commandContext) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext
        .getHistoricProcessInstanceManager()
        .findDeploymentIdMappingsByQueryCriteria(this);
  }

  @Override
  public List<QueryVariableValue> getQueryVariableValues() {
    return queryVariableNameToValuesMap.values()
        .stream()
        .flatMap(Set::stream)
        .toList();
  }

  public Map<String, Set<QueryVariableValue>> getQueryVariableNameToValuesMap() {
    return queryVariableNameToValuesMap;
  }

  @Override
  protected void ensureVariablesInitialized() {
    super.ensureVariablesInitialized();

    if (!queries.isEmpty()) {
      ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
      VariableSerializers variableSerializers = processEngineConfiguration.getVariableSerializers();
      String dbType = processEngineConfiguration.getDatabaseType();

      for (HistoricProcessInstanceQueryImpl orQuery: queries) {
        for (var variableValue : orQuery.getQueryVariableValues()) {
          variableValue.initialize(variableSerializers, dbType);
        }
      }
    }
  }

  @Override
  protected void addVariable(String name, Object value, QueryOperator operator, boolean processInstanceScope) {
    QueryVariableValue queryVariableValue = createQueryVariableValue(name, value, operator, processInstanceScope);

    Set<QueryVariableValue> queryVariableValues = queryVariableNameToValuesMap.get(name);
    if (queryVariableValues == null) {
      queryVariableNameToValuesMap.put(name, new HashSet<>(Collections.singletonList(queryVariableValue)));

    } else {
      queryVariableValues.add(queryVariableValue);

    }
  }

  public List<HistoricProcessInstanceQueryImpl> getQueries() {
    return queries;
  }

  public void addOrQuery(HistoricProcessInstanceQueryImpl orQuery) {
    orQuery.isOrQueryActive = true;
    this.queries.add(orQuery);
  }

  public void setOrQueryActive() {
    isOrQueryActive = true;
  }

  public boolean isOrQueryActive() {
    return isOrQueryActive;
  }

  public String[] getActiveActivityIds() {
    return activeActivityIds;
  }

  public String[] getActivityIds() {
    return activityIds;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public String[] getBusinessKeyIn() {
    return businessKeyIn;
  }

  public String getBusinessKeyLike() {
    return businessKeyLike;
  }

  public String[] getExecutedActivityIds() {
    return executedActivityIds;
  }

  public Date getExecutedActivityAfter() {
    return executedActivityAfter;
  }

  public Date getExecutedActivityBefore() {
    return executedActivityBefore;
  }

  public Date getExecutedJobAfter() {
    return executedJobAfter;
  }

  public Date getExecutedJobBefore() {
    return executedJobBefore;
  }

  public boolean isOpen() {
    return unfinished;
  }

  public boolean isUnfinished() {
    return unfinished;
  }

  public boolean isFinished() {
    return finished;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String[] getProcessDefinitionKeys() {
    return processDefinitionKeys;
  }

  public String getProcessDefinitionIdLike() {
    return processDefinitionKey + ":%:%";
  }

  public String getProcessDefinitionName() {
    return processDefinitionName;
  }

  public String getProcessDefinitionNameLike() {
    return processDefinitionNameLike;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public Set<String> getProcessInstanceIds() {
    return processInstanceIds;
  }

  public String[] getProcessInstanceIdNotIn() {
    return processInstanceIdNotIn;
  }

  public String getStartedBy() {
    return startedBy;
  }

  public String getSuperProcessInstanceId() {
    return superProcessInstanceId;
  }

  public void setSuperProcessInstanceId(String superProcessInstanceId) {
    this.superProcessInstanceId = superProcessInstanceId;
  }

  public List<String> getProcessKeyNotIn() {
    return processKeyNotIn;
  }

  public Date getStartedAfter() {
    return startedAfter;
  }

  public Date getStartedBefore() {
    return startedBefore;
  }

  public Date getFinishedAfter() {
    return finishedAfter;
  }

  public Date getFinishedBefore() {
    return finishedBefore;
  }

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public String getIncidentType() {
    return incidentType;
  }

  public String getIncidentMessage() {
    return this.incidentMessage;
  }

  public String getIncidentMessageLike() {
    return this.incidentMessageLike;
  }

  public String getIncidentStatus() {
    return incidentStatus;
  }

  public String getState() {
    return state;
  }

  public Date getFinishDateBy() {
    return finishDateBy;
  }

  public Date getStartDateBy() {
    return startDateBy;
  }

  public Date getStartDateOn() {
    return startDateOn;
  }

  public Date getStartDateOnBegin() {
    return startDateOnBegin;
  }

  public Date getStartDateOnEnd() {
    return startDateOnEnd;
  }

  public Date getFinishDateOn() {
    return finishDateOn;
  }

  public Date getFinishDateOnBegin() {
    return finishDateOnBegin;
  }

  public Date getFinishDateOnEnd() {
    return finishDateOnEnd;
  }

  public boolean isTenantIdSet() {
    return isTenantIdSet;
  }

  public boolean getIsTenantIdSet() {
    return isTenantIdSet;
  }

  public boolean isWithJobsRetrying(){
    return withJobsRetrying;
  }

  public boolean isWithIncidents() {
    return withIncidents;
  }

  public boolean isWithRootIncidents() {
    return withRootIncidents;
  }

  // below is deprecated and to be removed in 5.12

  protected Date startDateBy;
  protected Date startDateOn;
  protected Date finishDateBy;
  protected Date finishDateOn;
  protected Date startDateOnBegin;
  protected Date startDateOnEnd;
  protected Date finishDateOnBegin;
  protected Date finishDateOnEnd;

  @Deprecated(since = "1.0")
  @Override
  public HistoricProcessInstanceQuery startDateBy(Date date) {
    this.startDateBy = this.calculateMidnight(date);
    return this;
  }

  @Deprecated(since = "1.0")
  @Override
  public HistoricProcessInstanceQuery startDateOn(Date date) {
    this.startDateOn = date;
    this.startDateOnBegin = this.calculateMidnight(date);
    this.startDateOnEnd = this.calculateBeforeMidnight(date);
    return this;
  }

  @Deprecated(since = "1.0")
  @Override
  public HistoricProcessInstanceQuery finishDateBy(Date date) {
    this.finishDateBy = this.calculateBeforeMidnight(date);
    return this;
  }

  @Deprecated(since = "1.0")
  @Override
  public HistoricProcessInstanceQuery finishDateOn(Date date) {
    this.finishDateOn = date;
    this.finishDateOnBegin = this.calculateMidnight(date);
    this.finishDateOnEnd = this.calculateBeforeMidnight(date);
    return this;
  }

  @Deprecated(since = "1.0")
  private Date calculateBeforeMidnight(Date date){
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.add(Calendar.DAY_OF_MONTH, 1);
    cal.add(Calendar.SECOND, -1);
    return cal.getTime();
  }

  @Deprecated(since = "1.0")
  private Date calculateMidnight(Date date){
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.set(Calendar.MILLISECOND, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.HOUR, 0);
    return cal.getTime();
  }

  public boolean isRootProcessInstances() {
    return isRootProcessInstances;
  }

  public String getSubProcessInstanceId() {
    return subProcessInstanceId;
  }

  public String getSuperCaseInstanceId() {
    return superCaseInstanceId;
  }

  public String getSubCaseInstanceId() {
    return subCaseInstanceId;
  }

  public String[] getTenantIds() {
    return tenantIds;
  }

  public String[] getIncidentIds() {
    return incidentIds;
  }

  @Override
  public HistoricProcessInstanceQuery executedActivityAfter(Date date) {
    this.executedActivityAfter = date;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery executedActivityBefore(Date date) {
    this.executedActivityBefore = date;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery executedJobAfter(Date date) {
    this.executedJobAfter = date;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery executedJobBefore(Date date) {
    this.executedJobBefore = date;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery executedActivityIdIn(String... ids) {
    ensureNotNull(BadUserRequestException.class, "activity ids", (Object[]) ids);
    ensureNotContainsNull(BadUserRequestException.class, "activity ids", Arrays.asList(ids));
    this.executedActivityIds = ids;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery activeActivityIdIn(String... ids) {
    ensureNotNull(BadUserRequestException.class, "activity ids", (Object[]) ids);
    ensureNotContainsNull(BadUserRequestException.class, "activity ids", Arrays.asList(ids));
    this.activeActivityIds = ids;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery activityIdIn(String... ids) {
    ensureNotNull(BadUserRequestException.class, "activity ids", (Object[]) ids);
    ensureNotContainsNull(BadUserRequestException.class, "activity ids", Arrays.asList(ids));
    this.activityIds = ids;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery active() {
    ensureNull(BadUserRequestException.class, MSG_ALREADY_QUERYING, state, state);
    state = HistoricProcessInstance.STATE_ACTIVE;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery suspended() {
    ensureNull(BadUserRequestException.class, MSG_ALREADY_QUERYING, state, state);
    state = HistoricProcessInstance.STATE_SUSPENDED;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery completed() {
    ensureNull(BadUserRequestException.class, MSG_ALREADY_QUERYING, state, state);
    state = HistoricProcessInstance.STATE_COMPLETED;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery externallyTerminated() {
    ensureNull(BadUserRequestException.class, MSG_ALREADY_QUERYING, state, state);
    state = HistoricProcessInstance.STATE_EXTERNALLY_TERMINATED;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery internallyTerminated() {
    ensureNull(BadUserRequestException.class, MSG_ALREADY_QUERYING, state, state);
    state = HistoricProcessInstance.STATE_INTERNALLY_TERMINATED;
    return this;
  }

  @Override
  public HistoricProcessInstanceQuery or() {
    if (this != queries.get(0)) {
      throw new ProcessEngineException("Invalid query usage: cannot set or() within 'or' query");
    }

    HistoricProcessInstanceQueryImpl orQuery = new HistoricProcessInstanceQueryImpl();
    orQuery.isOrQueryActive = true;
    orQuery.queries = queries;
    queries.add(orQuery);
    return orQuery;
  }

  @Override
  public HistoricProcessInstanceQuery endOr() {
    if (!queries.isEmpty() && this != queries.get(queries.size()-1)) {
      throw new ProcessEngineException("Invalid query usage: cannot set endOr() before or()");
    }

    return queries.get(0);
  }

}

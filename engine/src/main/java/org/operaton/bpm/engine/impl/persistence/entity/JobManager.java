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
package org.operaton.bpm.engine.impl.persistence.entity;

import org.operaton.bpm.engine.impl.Direction;
import org.operaton.bpm.engine.impl.JobQueryImpl;
import org.operaton.bpm.engine.impl.JobQueryProperty;
import org.operaton.bpm.engine.impl.Page;
import org.operaton.bpm.engine.impl.QueryOrderingProperty;
import org.operaton.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.operaton.bpm.engine.impl.cfg.TransactionListener;
import org.operaton.bpm.engine.impl.cfg.TransactionState;
import org.operaton.bpm.engine.impl.context.Context;
import org.operaton.bpm.engine.impl.db.ListQueryParameterObject;
import org.operaton.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.operaton.bpm.engine.impl.jobexecutor.ExclusiveJobAddedNotification;
import org.operaton.bpm.engine.impl.jobexecutor.JobExecutor;
import org.operaton.bpm.engine.impl.jobexecutor.JobExecutorContext;
import org.operaton.bpm.engine.impl.jobexecutor.MessageAddedNotification;
import org.operaton.bpm.engine.impl.jobexecutor.TimerCatchIntermediateEventJobHandler;
import org.operaton.bpm.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.operaton.bpm.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.operaton.bpm.engine.impl.jobexecutor.TimerStartEventSubprocessJobHandler;
import org.operaton.bpm.engine.impl.persistence.AbstractManager;
import org.operaton.bpm.engine.impl.util.ClockUtil;
import org.operaton.bpm.engine.impl.util.CollectionUtil;
import org.operaton.bpm.engine.impl.util.ImmutablePair;
import org.operaton.bpm.engine.runtime.Job;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.operaton.bpm.engine.impl.jobexecutor.TimerEventJobHandler.JOB_HANDLER_CONFIG_PROPERTY_DELIMITER;
import static org.operaton.bpm.engine.impl.jobexecutor.TimerEventJobHandler.JOB_HANDLER_CONFIG_PROPERTY_FOLLOW_UP_JOB_CREATED;
import static org.operaton.bpm.engine.impl.util.EnsureUtil.ensureNotNull;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class JobManager extends AbstractManager {

  public static final QueryOrderingProperty JOB_PRIORITY_ORDERING_PROPERTY = new QueryOrderingProperty(null, JobQueryProperty.PRIORITY);
  public static final QueryOrderingProperty JOB_TYPE_ORDERING_PROPERTY = new QueryOrderingProperty(null, JobQueryProperty.TYPE);
  public static final QueryOrderingProperty JOB_DUEDATE_ORDERING_PROPERTY = new QueryOrderingProperty(null, JobQueryProperty.DUEDATE);
  private static final String JOB_ID = "jobId";
  private static final String HANDLER_TYPE = "handlerType";
  private static final String RETRIES = "retries";
  private static final String DUE_DATE = "dueDate";
  private static final String IS_DUE_DATE_SET = "isDueDateSet";
  private static final String PRIORITY = "priority";
  private static final String DEPLOYMENT_IDS = "deploymentIds";
  private static final String NOW = "now";
  private static final String ALWAYS_SET_DUE_DATE = "alwaysSetDueDate";
  private static final String DEPLOYMENT_AWARE = "deploymentAware";
  private static final String JOB_PRIORITY_MIN = "jobPriorityMin";
  private static final String JOB_PRIORITY_MAX = "jobPriorityMax";
  private static final String HISTORY_CLEANUP_ENABLED = "historyCleanupEnabled";

  static {
    JOB_PRIORITY_ORDERING_PROPERTY.setDirection(Direction.DESCENDING);
    JOB_TYPE_ORDERING_PROPERTY.setDirection(Direction.DESCENDING);
    JOB_DUEDATE_ORDERING_PROPERTY.setDirection(Direction.ASCENDING);
  }

  public void updateJob(JobEntity job) {
    getDbEntityManager().merge(job);
  }

  public void insertJob(JobEntity job) {
    job.setCreateTime(ClockUtil.getCurrentTime());

    getDbEntityManager().insert(job);
    getHistoricJobLogManager().fireJobCreatedEvent(job);
  }

  public void deleteJob(JobEntity job) {
    deleteJob(job, true);
  }

  public void deleteJob(JobEntity job, boolean fireDeleteEvent) {
    getDbEntityManager().delete(job);

    if (fireDeleteEvent) {
      getHistoricJobLogManager().fireJobDeletedEvent(job);
    }

  }

  public void insertAndHintJobExecutor(JobEntity jobEntity) {
    jobEntity.insert();
    if (Context.getProcessEngineConfiguration().isHintJobExecutor()) {
      hintJobExecutor(jobEntity);
    }
  }

  public void send(MessageEntity message) {
    message.insert();
    if (Context.getProcessEngineConfiguration().isHintJobExecutor()) {
      hintJobExecutor(message);
    }
  }

  public void schedule(TimerEntity timer) {
    Date duedate = timer.getDuedate();
    ensureNotNull("duedate", duedate);
    timer.insert();
    hintJobExecutorIfNeeded(timer, duedate);
  }

  public void reschedule(JobEntity jobEntity, Date newDuedate) {
    if (jobEntity instanceof EverLivingJobEntity || jobEntity instanceof MessageEntity) {
      jobEntity.init(Context.getCommandContext(), true, false);
      jobEntity.setSuspensionState(SuspensionState.ACTIVE.getStateCode());
      jobEntity.setDuedate(newDuedate);
      hintJobExecutorIfNeeded(jobEntity, newDuedate);
    }
  }

  private void hintJobExecutorIfNeeded(JobEntity jobEntity, Date duedate) {
    // Check if this timer fires before the next time the job executor will check for new timers to fire.
    // This is highly unlikely because normally waitTimeInMillis is 5000 (5 seconds)
    // and timers are usually set further in the future
    JobExecutor jobExecutor = Context.getProcessEngineConfiguration().getJobExecutor();
    int waitTimeInMillis = jobExecutor.getWaitTimeInMillis();
    if (duedate.getTime() < (ClockUtil.getCurrentTime().getTime() + waitTimeInMillis)) {
      hintJobExecutor(jobEntity);
    }
  }

  protected void hintJobExecutor(JobEntity job) {
    JobExecutor jobExecutor = Context.getProcessEngineConfiguration().getJobExecutor();
    if (!jobExecutor.isActive()) {
      return;
    }

    JobExecutorContext jobExecutorContext = Context.getJobExecutorContext();
    TransactionListener transactionListener = null;
    if (isJobPriorityInJobExecutorPriorityRange(job.getPriority())) {
      // add job to be executed in the current processor
      if (!job.isSuspended()
         && job.isExclusive()
         && isJobDue(job)
         && jobExecutorContext != null
         && jobExecutorContext.isExecutingExclusiveJob()
         && areInSameProcessInstance(job, jobExecutorContext.getCurrentJob())) {
        // lock job & add to the queue of the current processor
        Date currentTime = ClockUtil.getCurrentTime();
        job.setLockExpirationTime(new Date(currentTime.getTime() + jobExecutor.getLockTimeInMillis()));
        job.setLockOwner(jobExecutor.getLockOwner());
        transactionListener = new ExclusiveJobAddedNotification(job.getId(), jobExecutorContext);
      } else {
        // reset Acquisition strategy and notify the JobExecutor that
        // a new Job is available for execution on future runs
        transactionListener = new MessageAddedNotification(jobExecutor);
      }
      Context.getCommandContext()
      .getTransactionContext()
      .addTransactionListener(TransactionState.COMMITTED, transactionListener);
    }
  }

  protected boolean areInSameProcessInstance(JobEntity job1, JobEntity job2) {
    if (job1 == null || job2 == null) {
      return false;
    }

    String instance1 = job1.getProcessInstanceId();
    String instance2 = job2.getProcessInstanceId();

    return instance1 != null && instance1.equals(instance2);
  }

  protected boolean isJobPriorityInJobExecutorPriorityRange(long jobPriority) {
    ProcessEngineConfigurationImpl configuration = Context.getProcessEngineConfiguration();
    return (configuration.getJobExecutorPriorityRangeMin() <= jobPriority)
        && (configuration.getJobExecutorPriorityRangeMax() >= jobPriority);
  }

  public void cancelTimers(ExecutionEntity execution) {
    List<TimerEntity> timers = Context
      .getCommandContext()
      .getJobManager()
      .findTimersByExecutionId(execution.getId());

    for (TimerEntity timer: timers) {
      timer.delete();
    }
  }

  public JobEntity findJobById(String jobId) {
    return (JobEntity) getDbEntityManager().selectOne("selectJob", jobId);
  }

  @SuppressWarnings("unchecked")
  public List<AcquirableJobEntity> findNextJobsToExecute(Page page) {
    ProcessEngineConfigurationImpl engineConfiguration = Context.getProcessEngineConfiguration();

    Map<String,Object> params = new HashMap<>();
    Date now = ClockUtil.getCurrentTime();

    params.put(NOW, now);
    params.put(ALWAYS_SET_DUE_DATE, isEnsureJobDueDateNotNull());
    params.put(DEPLOYMENT_AWARE, engineConfiguration.isJobExecutorDeploymentAware());

    if (engineConfiguration.isJobExecutorDeploymentAware()) {
      Set<String> registeredDeployments = engineConfiguration.getRegisteredDeployments();
      if (!registeredDeployments.isEmpty()) {
        params.put(DEPLOYMENT_IDS, registeredDeployments);
      }
    }

    boolean jobExecutorAcquireByPriority = engineConfiguration.isJobExecutorAcquireByPriority();
    long jobExecutorPriorityRangeMin = engineConfiguration.getJobExecutorPriorityRangeMin();
    long jobExecutorPriorityRangeMax = engineConfiguration.getJobExecutorPriorityRangeMax();

    params.put(JOB_PRIORITY_MIN, jobExecutorAcquireByPriority && jobExecutorPriorityRangeMin != Long.MIN_VALUE ? jobExecutorPriorityRangeMin : null);
    params.put(JOB_PRIORITY_MAX, jobExecutorAcquireByPriority && jobExecutorPriorityRangeMax != Long.MAX_VALUE ? jobExecutorPriorityRangeMax : null);

    params.put(HISTORY_CLEANUP_ENABLED, engineConfiguration.isHistoryCleanupEnabled());

    List<QueryOrderingProperty> orderingProperties = new ArrayList<>();

    if (engineConfiguration.isJobExecutorAcquireByPriority()) {
      orderingProperties.add(JOB_PRIORITY_ORDERING_PROPERTY);
    }
    if (engineConfiguration.isJobExecutorPreferTimerJobs()) {
      orderingProperties.add(JOB_TYPE_ORDERING_PROPERTY);
    }
    if (engineConfiguration.isJobExecutorAcquireByDueDate()) {
      orderingProperties.add(JOB_DUEDATE_ORDERING_PROPERTY);
    }

    params.put("orderingProperties", orderingProperties);
    // don't apply default sorting
    params.put("applyOrdering", !orderingProperties.isEmpty());
    params.put("applyExclusiveOverProcessHierarchies", engineConfiguration.isJobExecutorAcquireExclusiveOverProcessHierarchies());

    return getDbEntityManager().selectList("selectNextJobsToExecute", params, page);
  }

  @SuppressWarnings("unchecked")
  public List<JobEntity> findJobsByExecutionId(String executionId) {
    return getDbEntityManager().selectList("selectJobsByExecutionId", executionId);
  }

  @SuppressWarnings("unchecked")
  public List<JobEntity> findJobsByProcessInstanceId(String processInstanceId) {
    return getDbEntityManager().selectList("selectJobsByProcessInstanceId", processInstanceId);
  }

  @SuppressWarnings("unchecked")
  public List<JobEntity> findJobsByJobDefinitionId(String jobDefinitionId) {
    return getDbEntityManager().selectList("selectJobsByJobDefinitionId", jobDefinitionId);
  }

  @SuppressWarnings("unchecked")
  public List<Job> findJobsByHandlerType(String handlerType) {
    return getDbEntityManager().selectList("selectJobsByHandlerType", handlerType);
  }

  @SuppressWarnings("unchecked")
  public List<TimerEntity> findUnlockedTimersByDuedate(Date duedate, Page page) {
    final String query = "selectUnlockedTimersByDuedate";
    return getDbEntityManager().selectList(query, duedate, page);
  }

  @SuppressWarnings("unchecked")
  public List<TimerEntity> findTimersByExecutionId(String executionId) {
    return getDbEntityManager().selectList("selectTimersByExecutionId", executionId);
  }

  @SuppressWarnings("unchecked")
  public List<Job> findJobsByQueryCriteria(JobQueryImpl jobQuery, Page page) {
    configureQuery(jobQuery);
    return getDbEntityManager().selectList("selectJobByQueryCriteria", jobQuery, page);
  }

  @SuppressWarnings("unchecked")
  public List<ImmutablePair<String, String>> findDeploymentIdMappingsByQueryCriteria(JobQueryImpl jobQuery) {
    configureQuery(jobQuery);
    Set<String> processInstanceIds = jobQuery.getProcessInstanceIds();
    if (processInstanceIds != null && !processInstanceIds.isEmpty()) {
      List<List<String>> partitions = CollectionUtil
          .partition(new ArrayList<>(processInstanceIds), DbSqlSessionFactory.MAXIMUM_NUMBER_PARAMS);
      List<ImmutablePair<String, String>> result = new ArrayList<>();
      partitions.stream().forEach(partition -> {
        jobQuery.processInstanceIds(new HashSet<>(partition));
        result.addAll(getDbEntityManager().selectList("selectJobDeploymentIdMappingsByQueryCriteria", jobQuery));
      });
      return result;
    } else {
      return getDbEntityManager().selectList("selectJobDeploymentIdMappingsByQueryCriteria", jobQuery);
    }
  }

  @SuppressWarnings("unchecked")
  public List<JobEntity> findJobsByConfiguration(String jobHandlerType, String jobHandlerConfiguration, String tenantId) {
    Map<String, String> params = new HashMap<>();
    params.put(HANDLER_TYPE, jobHandlerType);
    params.put("handlerConfiguration", jobHandlerConfiguration);
    params.put("tenantId", tenantId);

    if (TimerCatchIntermediateEventJobHandler.TYPE.equals(jobHandlerType)
      || TimerExecuteNestedActivityJobHandler.TYPE.equals(jobHandlerType)
      || TimerStartEventJobHandler.TYPE.equals(jobHandlerType)
      || TimerStartEventSubprocessJobHandler.TYPE.equals(jobHandlerType)) {

      String queryValue = jobHandlerConfiguration + JOB_HANDLER_CONFIG_PROPERTY_DELIMITER + JOB_HANDLER_CONFIG_PROPERTY_FOLLOW_UP_JOB_CREATED;
      params.put("handlerConfigurationWithFollowUpJobCreatedProperty", queryValue);
    }

    return getDbEntityManager().selectList("selectJobsByConfiguration", params);
  }

  public long findJobCountByQueryCriteria(JobQueryImpl jobQuery) {
    configureQuery(jobQuery);
    return (Long) getDbEntityManager().selectOne("selectJobCountByQueryCriteria", jobQuery);
  }

  public void updateJobSuspensionStateById(String jobId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put(JOB_ID, jobId);
    parameters.put(SUSPENSION_STATE, suspensionState.getStateCode());
    getDbEntityManager().update(JobEntity.class, "updateJobSuspensionStateByParameters", configureParameterizedQuery(parameters));
  }

  public void updateJobSuspensionStateByJobDefinitionId(String jobDefinitionId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put(JOB_DEFINITION_ID, jobDefinitionId);
    parameters.put(SUSPENSION_STATE, suspensionState.getStateCode());
    getDbEntityManager().update(JobEntity.class, "updateJobSuspensionStateByParameters", configureParameterizedQuery(parameters));
  }

  public void updateJobSuspensionStateByProcessInstanceId(String processInstanceId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put(PROCESS_INSTANCE_ID, processInstanceId);
    parameters.put(SUSPENSION_STATE, suspensionState.getStateCode());
    getDbEntityManager().update(JobEntity.class, "updateJobSuspensionStateByParameters", configureParameterizedQuery(parameters));
  }

  public void updateJobSuspensionStateByProcessDefinitionId(String processDefinitionId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put(PROCESS_DEFINITION_ID, processDefinitionId);
    parameters.put(SUSPENSION_STATE, suspensionState.getStateCode());
    getDbEntityManager().update(JobEntity.class, "updateJobSuspensionStateByParameters", configureParameterizedQuery(parameters));
  }

  public void updateStartTimerJobSuspensionStateByProcessDefinitionId(String processDefinitionId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put(PROCESS_DEFINITION_ID, processDefinitionId);
    parameters.put(SUSPENSION_STATE, suspensionState.getStateCode());
    parameters.put(HANDLER_TYPE, TimerStartEventJobHandler.TYPE);
    getDbEntityManager().update(JobEntity.class, "updateJobSuspensionStateByParameters", configureParameterizedQuery(parameters));
  }

  public void updateJobSuspensionStateByProcessDefinitionKey(String processDefinitionKey, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put(PROCESS_DEFINITION_KEY, processDefinitionKey);
    parameters.put(IS_PROCESS_DEFINITION_TENANT_ID_SET, false);
    parameters.put(SUSPENSION_STATE, suspensionState.getStateCode());
    getDbEntityManager().update(JobEntity.class, "updateJobSuspensionStateByParameters", configureParameterizedQuery(parameters));
  }

  public void updateJobSuspensionStateByProcessDefinitionKeyAndTenantId(String processDefinitionKey, String processDefinitionTenantId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put(PROCESS_DEFINITION_KEY, processDefinitionKey);
    parameters.put(IS_PROCESS_DEFINITION_TENANT_ID_SET, true);
    parameters.put(PROCESS_DEFINITION_TENANT_ID, processDefinitionTenantId);
    parameters.put(SUSPENSION_STATE, suspensionState.getStateCode());
    getDbEntityManager().update(JobEntity.class, "updateJobSuspensionStateByParameters", configureParameterizedQuery(parameters));
  }

  public void updateStartTimerJobSuspensionStateByProcessDefinitionKey(String processDefinitionKey, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put(PROCESS_DEFINITION_KEY, processDefinitionKey);
    parameters.put(IS_PROCESS_DEFINITION_TENANT_ID_SET, false);
    parameters.put(SUSPENSION_STATE, suspensionState.getStateCode());
    parameters.put(HANDLER_TYPE, TimerStartEventJobHandler.TYPE);
    getDbEntityManager().update(JobEntity.class, "updateJobSuspensionStateByParameters", configureParameterizedQuery(parameters));
  }

  public void updateStartTimerJobSuspensionStateByProcessDefinitionKeyAndTenantId(String processDefinitionKey, String processDefinitionTenantId, SuspensionState suspensionState) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put(PROCESS_DEFINITION_KEY, processDefinitionKey);
    parameters.put(IS_PROCESS_DEFINITION_TENANT_ID_SET, true);
    parameters.put(PROCESS_DEFINITION_TENANT_ID, processDefinitionTenantId);
    parameters.put(SUSPENSION_STATE, suspensionState.getStateCode());
    parameters.put(HANDLER_TYPE, TimerStartEventJobHandler.TYPE);
    getDbEntityManager().update(JobEntity.class, "updateJobSuspensionStateByParameters", configureParameterizedQuery(parameters));
  }

  public void updateFailedJobRetriesByJobDefinitionId(String jobDefinitionId, int retries, Date dueDate, boolean isDueDateSet) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put(JOB_DEFINITION_ID, jobDefinitionId);
    parameters.put(RETRIES, retries);
    parameters.put(DUE_DATE, dueDate);
    parameters.put(IS_DUE_DATE_SET, isDueDateSet);
    getDbEntityManager().update(JobEntity.class, "updateFailedJobRetriesByParameters", parameters);
  }

  public void updateJobPriorityByDefinitionId(String jobDefinitionId, long priority) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put(JOB_DEFINITION_ID, jobDefinitionId);
    parameters.put(PRIORITY, priority);
    getDbEntityManager().update(JobEntity.class, "updateJobPriorityByDefinitionId", parameters);
  }

  protected void configureQuery(JobQueryImpl query) {
    getAuthorizationManager().configureJobQuery(query);
    getTenantManager().configureQuery(query);
  }

  protected ListQueryParameterObject configureParameterizedQuery(Object parameter) {
    return getTenantManager().configureQuery(parameter);
  }

  protected boolean isEnsureJobDueDateNotNull() {
    return Context.getProcessEngineConfiguration().isEnsureJobDueDateNotNull();
  }

  /**
   * Sometimes we get a notification of a job that is not yet due, so we
   * should not execute it immediately
   */
  protected boolean isJobDue(JobEntity job) {
    Date duedate = job.getDuedate();
    Date now = ClockUtil.getCurrentTime();

    return duedate == null || duedate.getTime() <= now.getTime();
  }
}

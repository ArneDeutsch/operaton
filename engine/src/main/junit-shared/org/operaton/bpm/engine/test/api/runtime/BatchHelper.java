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
package org.operaton.bpm.engine.test.api.runtime;

import org.operaton.bpm.engine.*;
import org.operaton.bpm.engine.batch.Batch;
import org.operaton.bpm.engine.batch.history.HistoricBatch;
import org.operaton.bpm.engine.history.HistoricJobLog;
import org.operaton.bpm.engine.impl.batch.BatchMonitorJobHandler;
import org.operaton.bpm.engine.impl.batch.BatchSeedJobHandler;
import org.operaton.bpm.engine.impl.util.ClockUtil;
import org.operaton.bpm.engine.management.JobDefinition;
import org.operaton.bpm.engine.runtime.Job;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BatchHelper {

  private final ProcessEngineProvider processEngineProvider;

  public BatchHelper(ProcessEngineProvider processEngineProvider) {
    this.processEngineProvider = processEngineProvider;
  }

  public Job getJobForDefinition(JobDefinition jobDefinition) {
    if (jobDefinition != null) {
      return getManagementService()
        .createJobQuery().jobDefinitionId(jobDefinition.getId()).singleResult();
    }
    else {
      return null;
    }
  }

  public List<Job> getJobsForDefinition(JobDefinition jobDefinition) {
    return getManagementService()
      .createJobQuery().jobDefinitionId(jobDefinition.getId()).list();
  }

  public void executeJob(Job job) {
    assertThat(job).withFailMessage(() -> "Job to execute does not exist").isNotNull();
    try {
      getManagementService().executeJob(job.getId());
    }
    catch (BadUserRequestException e) {
      throw e;
    }
    catch (Exception e) {
      // ignore
    }
  }

  public JobDefinition getSeedJobDefinition(Batch batch) {
    return getManagementService()
      .createJobDefinitionQuery()
      .jobDefinitionId(batch.getSeedJobDefinitionId())
      .jobType(BatchSeedJobHandler.TYPE)
      .singleResult();
  }

  public Job getSeedJob(Batch batch) {
    return getJobForDefinition(getSeedJobDefinition(batch));
  }

  public void executeSeedJob(Batch batch) {
    executeJob(getSeedJob(batch));
  }

  public JobDefinition getMonitorJobDefinition(Batch batch) {
    return getManagementService()
      .createJobDefinitionQuery().jobDefinitionId(batch.getMonitorJobDefinitionId()).jobType(BatchMonitorJobHandler.TYPE).singleResult();
  }

  public Job getMonitorJob(Batch batch) {
    return getJobForDefinition(getMonitorJobDefinition(batch));
  }

  public void executeMonitorJob(Batch batch) {
    executeJob(getMonitorJob(batch));
  }

  public void completeMonitorJobs(Batch batch) {
    while (getMonitorJob(batch) != null) {
      executeMonitorJob(batch);
    }
  }

  public void completeSeedJobs(Batch batch) {
    while (getSeedJob(batch) != null) {
      executeSeedJob(batch);
    }
  }

  public JobDefinition getExecutionJobDefinition(Batch batch) {
    throw new AssertionError("This method is not implemented");
  }

  public JobDefinition getExecutionJobDefinition(Batch batch, String jobType) {
    return processEngineProvider.getProcessEngine().getManagementService()
        .createJobDefinitionQuery()
        .jobDefinitionId(batch.getBatchJobDefinitionId())
        .jobType(jobType)
        .singleResult();
  }

  public List<Job> getExecutionJobs(Batch batch) {
    return getJobsForDefinition(getExecutionJobDefinition(batch));
  }

  public List<Job> getExecutionJobs(Batch batch, String jobType) {
    return getJobsForDefinition(getExecutionJobDefinition(batch, jobType));
  }

  public void executeJobs(Batch batch) {
    for (Job job : getExecutionJobs(batch)) {
      executeJob(job);
    }
  }

  public void completeBatch(Batch batch) {
    completeSeedJobs(batch);
    completeExecutionJobs(batch);
    completeMonitorJobs(batch);
  }

  public void completeJobs(Batch batch, int count) {
    List<Job> jobs = getExecutionJobs(batch);
    assertThat(jobs).hasSizeGreaterThanOrEqualTo(count);
    for (int i = 0; i < count; i++) {
      executeJob(jobs.get(i));
    }
  }

  public void failExecutionJobs(Batch batch, int count) {
    setRetries(batch, count, 0);
  }

  public void setRetries(Batch batch, int count, int retries) {
    List<Job> jobs = getExecutionJobs(batch);
    assertThat(jobs).hasSizeGreaterThanOrEqualTo(count);

    ManagementService managementService = getManagementService();
    for (int i = 0; i < count; i++) {
      managementService.setJobRetries(jobs.get(i).getId(), retries);
    }

  }

  public void completeExecutionJobs(Batch batch) {
    while (!getExecutionJobs(batch).isEmpty()) {
      executeJobs(batch);
    }
  }

  public HistoricBatch getHistoricBatch(Batch batch) {
    return getHistoryService()
      .createHistoricBatchQuery()
      .batchId(batch.getId())
      .singleResult();
  }
  public List<HistoricJobLog> getHistoricSeedJobLog(Batch batch) {
    return getHistoryService()
      .createHistoricJobLogQuery()
      .jobDefinitionId(batch.getSeedJobDefinitionId())
      .orderPartiallyByOccurrence()
      .asc()
      .list();
  }

  public List<HistoricJobLog> getHistoricMonitorJobLog(Batch batch) {
    return getHistoryService()
      .createHistoricJobLogQuery()
      .jobDefinitionId(batch.getMonitorJobDefinitionId())
      .orderPartiallyByOccurrence()
      .asc()
      .list();
  }

  public List<HistoricJobLog> getHistoricMonitorJobLog(Batch batch, Job monitorJob) {
    return getHistoryService()
      .createHistoricJobLogQuery()
      .jobDefinitionId(batch.getMonitorJobDefinitionId())
      .jobId(monitorJob.getId())
      .orderPartiallyByOccurrence()
      .asc()
      .list();
  }

  public List<HistoricJobLog> getHistoricBatchJobLog(Batch batch) {
    return getHistoryService()
      .createHistoricJobLogQuery()
      .jobDefinitionId(batch.getBatchJobDefinitionId())
      .orderPartiallyByOccurrence()
      .asc()
      .list();
  }

  public Date addSeconds(Date date, int seconds) {
    return new Date(date.getTime() + seconds * 1000L);
  }

  public Date addSecondsToClock(int seconds) {
    Date newDate = addSeconds(ClockUtil.getCurrentTime(), seconds);
    ClockUtil.setCurrentTime(newDate);
    return newDate;
  }

  /**
   * Remove all batches and historic batches. Usually called in {@link org.junit.After} method.
   */
  public void removeAllRunningAndHistoricBatches() {
    HistoryService historyService = getHistoryService();
    ManagementService managementService = getManagementService();

    for (Batch batch : managementService.createBatchQuery().list()) {
      managementService.deleteBatch(batch.getId(), true);
    }

    // remove history of completed batches
    for (HistoricBatch historicBatch : historyService.createHistoricBatchQuery().list()) {
      historyService.deleteHistoricBatch(historicBatch.getId());
    }

  }

  protected ProcessEngine getProcessEngine () {
    return processEngineProvider.getProcessEngine();
  }

  protected RuntimeService getRuntimeService() {
    return getProcessEngine().getRuntimeService();
  }

  protected ManagementService getManagementService() {
    return getProcessEngine().getManagementService();
  }

  protected HistoryService getHistoryService() {
    return getProcessEngine().getHistoryService();
  }

  protected IdentityService getIdentityService () {
    return getProcessEngine().getIdentityService();
  }

}

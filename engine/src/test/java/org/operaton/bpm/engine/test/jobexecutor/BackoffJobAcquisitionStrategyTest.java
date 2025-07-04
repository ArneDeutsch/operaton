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
package org.operaton.bpm.engine.test.jobexecutor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.operaton.bpm.engine.impl.jobexecutor.AcquiredJobs;
import org.operaton.bpm.engine.impl.jobexecutor.BackoffJobAcquisitionStrategy;
import org.operaton.bpm.engine.impl.jobexecutor.JobAcquisitionContext;
import org.operaton.bpm.engine.impl.jobexecutor.JobAcquisitionStrategy;

/**
 * @author Thorben Lindhauer
 *
 */
class BackoffJobAcquisitionStrategyTest {

  // strategy configuration
  protected static final long BASE_IDLE_WAIT_TIME = 50;
  protected static final float IDLE_INCREASE_FACTOR = 1.5f;
  protected static final long MAX_IDLE_TIME = 500;

  protected static final long BASE_BACKOFF_WAIT_TIME = 80;
  protected static final float BACKOFF_INCREASE_FACTOR = 2.0f;
  protected static final long MAX_BACKOFF_TIME = 1000;

  protected static final int DECREASE_THRESHOLD = 3;
  protected static final int NUM_JOBS_TO_ACQUIRE = 10;

  // misc
  protected static final String ENGINE_NAME = "engine";

  protected JobAcquisitionStrategy strategy;

  @BeforeEach
  void setUp() {
    strategy = new BackoffJobAcquisitionStrategy(
        BASE_IDLE_WAIT_TIME,
        IDLE_INCREASE_FACTOR,
        MAX_IDLE_TIME,
        BASE_BACKOFF_WAIT_TIME,
        BACKOFF_INCREASE_FACTOR,
        MAX_BACKOFF_TIME,
        DECREASE_THRESHOLD,
        NUM_JOBS_TO_ACQUIRE);
  }

  @Test
  void testIdleWaitTime() {

    // given a job acquisition strategy and a job acquisition context
    // with no acquired jobs
    JobAcquisitionContext context = new JobAcquisitionContext();

    context.submitAcquiredJobs(ENGINE_NAME, buildAcquiredJobs(NUM_JOBS_TO_ACQUIRE, 0, 0));

    // when reconfiguring the strategy
    strategy.reconfigure(context);

    // then the job acquisition strategy returns the level 1 idle time
    assertThat(strategy.getWaitTime()).isEqualTo(BASE_IDLE_WAIT_TIME);

    // when resubmitting the same acquisition result
    for (int idleLevel = 1; idleLevel < 6; idleLevel++) {
      context.reset();
      context.submitAcquiredJobs(ENGINE_NAME, buildAcquiredJobs(NUM_JOBS_TO_ACQUIRE, 0, 0));

      strategy.reconfigure(context);
      assertThat(strategy.getWaitTime()).isEqualTo((long) (BASE_IDLE_WAIT_TIME * Math.pow(IDLE_INCREASE_FACTOR, idleLevel)));
    }

    // and the maximum idle level is finally reached
    context.reset();
    context.submitAcquiredJobs(ENGINE_NAME, buildAcquiredJobs(NUM_JOBS_TO_ACQUIRE, 0, 0));

    strategy.reconfigure(context);
    assertThat(strategy.getWaitTime()).isEqualTo(MAX_IDLE_TIME);
  }

  @Test
  void testAcquisitionAfterIdleWait() {

    // given a job acquisition strategy and a job acquisition context
    // with no acquired jobs
    JobAcquisitionContext context = new JobAcquisitionContext();

    context.submitAcquiredJobs(ENGINE_NAME, buildAcquiredJobs(NUM_JOBS_TO_ACQUIRE, 0, 0));
    strategy.reconfigure(context);
    assertThat(strategy.getWaitTime()).isEqualTo(BASE_IDLE_WAIT_TIME);

    // when receiving a successful acquisition result
    context.reset();
    context.submitAcquiredJobs(ENGINE_NAME, buildAcquiredJobs(NUM_JOBS_TO_ACQUIRE, NUM_JOBS_TO_ACQUIRE, 0));

    strategy.reconfigure(context);

    // then the idle wait time has been reset
    assertThat(strategy.getWaitTime()).isZero();
  }

  @Test
  void testAcquireLessJobsOnRejection() {
    // given a job acquisition strategy and a job acquisition context
    // with acquired jobs, some of which have been rejected for execution
    JobAcquisitionContext context = new JobAcquisitionContext();

    AcquiredJobs acquiredJobs = buildAcquiredJobs(NUM_JOBS_TO_ACQUIRE, NUM_JOBS_TO_ACQUIRE, 0);
    context.submitAcquiredJobs(ENGINE_NAME, acquiredJobs);

    // when half of the jobs are rejected
    int numJobsRejected = 5;
    for (int i = 0; i < numJobsRejected; i++) {
      context.submitRejectedBatch(ENGINE_NAME, acquiredJobs.getJobIdBatches().get(i));
    }

    // then the strategy only attempts to acquire the number of jobs that were successfully submitted
    strategy.reconfigure(context);

    assertThat(strategy.getNumJobsToAcquire(ENGINE_NAME)).isEqualTo(NUM_JOBS_TO_ACQUIRE - numJobsRejected);

    // without a timeout
    assertThat(strategy.getWaitTime()).isZero();
  }

  @Test
  void testWaitTimeOnFullRejection() {
    // given a job acquisition strategy and a job acquisition context
    // with acquired jobs all of which have been rejected for execution
    JobAcquisitionContext context = new JobAcquisitionContext();

    AcquiredJobs acquiredJobs = buildAcquiredJobs(NUM_JOBS_TO_ACQUIRE, NUM_JOBS_TO_ACQUIRE, 0);
    context.submitAcquiredJobs(ENGINE_NAME, acquiredJobs);

    for (int i = 0; i < NUM_JOBS_TO_ACQUIRE; i++) {
      context.submitRejectedBatch(ENGINE_NAME, acquiredJobs.getJobIdBatches().get(i));
    }

    // when reconfiguring the strategy
    strategy.reconfigure(context);

    // then there is a slight wait time to avoid constant spinning while
    // no execution resources are available
    assertThat(strategy.getWaitTime()).isEqualTo(BackoffJobAcquisitionStrategy.DEFAULT_EXECUTION_SATURATION_WAIT_TIME);
  }

  /**
   * numJobsToAcquire >= numJobsAcquired >= numJobsFailedToLock must hold
   */
  protected AcquiredJobs buildAcquiredJobs(int numJobsToAcquire, int numJobsAcquired, int numJobsFailedToLock) {
    AcquiredJobs acquiredJobs = new AcquiredJobs(numJobsToAcquire);
    for (int i = 0; i < numJobsAcquired; i++) {
      acquiredJobs.addJobIdBatch(List.of(Integer.toString(i)));
    }

    for (int i = 0; i < numJobsFailedToLock; i++) {
      acquiredJobs.removeJobId(Integer.toString(i));
    }

    return acquiredJobs;
  }

}

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

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.operaton.bpm.engine.impl.persistence.entity.AcquirableJobEntity;
import org.operaton.bpm.engine.test.Deployment;
import org.operaton.bpm.engine.test.util.ClockTestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class JobExecutorAcquireJobsDefaultTest extends AbstractJobExecutorAcquireJobsTest {

  @Parameterized.Parameter(0)
  public boolean ensureJobDueDateSet;

  @Parameterized.Parameter(1)
  public Date currentTime;

  @Parameterized.Parameters(name = "Job DueDate is set: {0}")
  public static Collection<Object[]> scenarios() {
    return Arrays.asList(new Object[][] {
      { false, null },
      { true, ClockTestUtil.setClockToDateWithoutMilliseconds() }
    });
  }

  @Before
  public void setUp() {
    rule.getProcessEngineConfiguration().setEnsureJobDueDateNotNull(ensureJobDueDateSet);
  }

  @Test
  public void testProcessEngineConfiguration() {
    assertThat(configuration.isJobExecutorPreferTimerJobs()).isFalse();
    assertThat(configuration.isJobExecutorAcquireByDueDate()).isFalse();
    assertThat(configuration.isEnsureJobDueDateNotNull()).isEqualTo(ensureJobDueDateSet);
  }

  @Test
  @Deployment(resources = "org/operaton/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml")
  public void testJobDueDateValue() {
    // when
    runtimeService.startProcessInstanceByKey("simpleAsyncProcess");
    List<AcquirableJobEntity> jobList = findAcquirableJobs();

    // then
    assertThat(jobList).hasSize(1);
    assertThat(jobList.get(0).getDuedate()).isEqualTo(currentTime);
  }
}

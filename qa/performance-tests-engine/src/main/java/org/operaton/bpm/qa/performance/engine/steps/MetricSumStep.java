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
package org.operaton.bpm.qa.performance.engine.steps;

import org.operaton.bpm.engine.ProcessEngine;
import org.operaton.bpm.engine.management.MetricsQuery;
import org.operaton.bpm.qa.performance.engine.framework.PerfTestRunContext;

import java.util.Date;

/**
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public class MetricSumStep extends ProcessEngineAwareStep {

  protected final String name;
  protected final Date startDate;
  protected final Date endDate;

  public MetricSumStep(String name, Date startDate, Date endDate, ProcessEngine processEngine) {
    super(processEngine);
    this.name = name;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  @Override
  public void execute(PerfTestRunContext context) {
    MetricsQuery metricsQuery = this.processEngine.getManagementService().createMetricsQuery();

    if (name != null) {
      metricsQuery.name(name);
    }

    if (startDate != null) {
      metricsQuery.startDate(startDate);
    }

    if (endDate != null) {
      metricsQuery.endDate(endDate);
    }

    metricsQuery.sum();
  }
}


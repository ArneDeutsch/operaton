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
package org.operaton.bpm.engine.impl.batch;

import static org.operaton.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Date;
import java.util.List;

import org.operaton.bpm.engine.batch.BatchStatistics;
import org.operaton.bpm.engine.batch.BatchStatisticsQuery;
import org.operaton.bpm.engine.impl.AbstractQuery;
import org.operaton.bpm.engine.impl.BatchQueryProperty;
import org.operaton.bpm.engine.impl.Page;
import org.operaton.bpm.engine.impl.interceptor.CommandContext;
import org.operaton.bpm.engine.impl.interceptor.CommandExecutor;
import org.operaton.bpm.engine.impl.persistence.entity.SuspensionState;

public class BatchStatisticsQueryImpl extends AbstractQuery<BatchStatisticsQuery, BatchStatistics> implements BatchStatisticsQuery {

  protected static final long serialVersionUID = 1L;

  protected String batchId;
  protected String type;
  protected boolean isTenantIdSet = false;
  protected String[] tenantIds;
  protected SuspensionState suspensionState;
  protected String userId;
  protected Date startedBefore;
  protected Date startedAfter;
  protected Boolean hasFailure;

  public BatchStatisticsQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  @Override
  public BatchStatisticsQuery batchId(String batchId) {
    ensureNotNull("Batch id", batchId);
    this.batchId = batchId;
    return this;
  }

  public String getBatchId() {
    return batchId;
  }

  @Override
  public BatchStatisticsQuery type(String type) {
    ensureNotNull("Type", type);
    this.type = type;
    return this;
  }

  public String getType() {
    return type;
  }

  @Override
  public BatchStatisticsQuery tenantIdIn(String... tenantIds) {
    ensureNotNull("tenantIds", (Object[]) tenantIds);
    this.tenantIds = tenantIds;
    isTenantIdSet = true;
    return this;
  }

  public String[] getTenantIds() {
    return tenantIds;
  }

  public boolean isTenantIdSet() {
    return isTenantIdSet;
  }

  @Override
  public BatchStatisticsQuery withoutTenantId() {
    this.tenantIds = null;
    isTenantIdSet = true;
    return this;
  }

  @Override
  public BatchStatisticsQuery active() {
    this.suspensionState = SuspensionState.ACTIVE;
    return this;
  }

  @Override
  public BatchStatisticsQuery suspended() {
    this.suspensionState = SuspensionState.SUSPENDED;
    return this;
  }

  @Override
  public BatchStatisticsQuery createdBy(final String userId) {
    this.userId = userId;
    return this;
  }

  @Override
  public BatchStatisticsQuery startedBefore(final Date date) {
    this.startedBefore = date;
    return this;
  }

  @Override
  public BatchStatisticsQuery startedAfter(final Date date) {
    this.startedAfter = date;
    return this;
  }

  @Override
  public BatchStatisticsQuery withFailures() {
    this.hasFailure = true;
    return this;
  }

  @Override
  public BatchStatisticsQuery withoutFailures() {
    this.hasFailure = false;
    return this;
  }

  public SuspensionState getSuspensionState() {
    return suspensionState;
  }

  @Override
  public BatchStatisticsQuery orderById() {
    return orderBy(BatchQueryProperty.ID);
  }

  @Override
  public BatchStatisticsQuery orderByTenantId() {
    return orderBy(BatchQueryProperty.TENANT_ID);
  }

  @Override
  public BatchStatisticsQuery orderByStartTime() {
    return orderBy(BatchQueryProperty.START_TIME);
  }

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getStatisticsManager()
      .getStatisticsCountGroupedByBatch(this);
  }

  @Override
  public List<BatchStatistics> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getStatisticsManager()
      .getStatisticsGroupedByBatch(this, page);
  }

}

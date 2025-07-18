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

import org.operaton.bpm.engine.history.HistoricActivityStatistics;

/**
 *
 * @author Roman Smirnov
 *
 */
public class HistoricActivityStatisticsImpl implements HistoricActivityStatistics {

  protected String id;
  protected long instances;
  protected long finished;
  protected long canceled;
  protected long completeScope;
  protected long openIncidents;
  protected long resolvedIncidents;
  protected long deletedIncidents;

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public long getInstances() {
    return instances;
  }

  public void setInstances(long instances) {
    this.instances = instances;
  }

  @Override
  public long getFinished() {
    return finished;
  }

  public void setFinished(long finished) {
    this.finished = finished;
  }

  @Override
  public long getCanceled() {
    return canceled;
  }

  public void setCanceled(long canceled) {
    this.canceled = canceled;
  }

  @Override
  public long getCompleteScope() {
    return completeScope;
  }

  public void setCompleteScope(long completeScope) {
    this.completeScope = completeScope;
  }

  @Override
  public long getOpenIncidents() {
    return openIncidents;
  }

  public void setOpenIncidents(long openIncidents) {
    this.openIncidents = openIncidents;
  }

  @Override
  public long getResolvedIncidents() {
    return resolvedIncidents;
  }

  public void setResolvedIncidents(long resolvedIncidents) {
    this.resolvedIncidents = resolvedIncidents;
  }

  @Override
  public long getDeletedIncidents() {
    return deletedIncidents;
  }

  public void setDeletedIncidents(long closedIncidents) {
    this.deletedIncidents = closedIncidents;
  }
}

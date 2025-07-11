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
package org.operaton.bpm.admin.impl.plugin.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.operaton.bpm.admin.impl.plugin.base.dto.MetricsAggregatedQueryDto;
import org.operaton.bpm.admin.impl.plugin.base.dto.MetricsAggregatedResultDto;
import org.operaton.bpm.admin.resource.AbstractAdminPluginResource;
import org.operaton.bpm.engine.impl.metrics.util.MetricsUtil;
import org.operaton.bpm.engine.management.Metrics;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

public class MetricsRestService extends AbstractAdminPluginResource {

  public static final String PATH = "/metrics";
  public static final ObjectMapper objectMapper = new ObjectMapper();

  public MetricsRestService(String engineName) {
    super(engineName);
  }

  @GET
  @Path("/aggregated")
  @Produces(MediaType.APPLICATION_JSON)
  public List<MetricsAggregatedResultDto> getAggregatedMetrics(@Context UriInfo uriInfo) {
    MetricsAggregatedQueryDto queryDto = new MetricsAggregatedQueryDto(objectMapper, uriInfo.getQueryParameters());
    queryDto.validateAndPrepareQuery();

    // TU metrics are fetched by a separate query (see below) and the list of metrics is only used by the first query
    // Remove TU metric from the list
    boolean queryTaskUsers = queryDto.getMetrics().remove(Metrics.UNIQUE_TASK_WORKERS);

    List<MetricsAggregatedResultDto> result = new ArrayList<>();
    if (!queryDto.getMetrics().isEmpty()) {
      result.addAll(getQueryService().executeQuery("selectMetricsAggregated", queryDto));
    }
    if (queryTaskUsers) {
      result.addAll(getQueryService().executeQuery("selectMetricsAggregatedTU", queryDto));
    }

    result.forEach(resultDto -> resultDto.setMetric(MetricsUtil.resolvePublicName(resultDto.getMetric())));
    return result;
  }

}

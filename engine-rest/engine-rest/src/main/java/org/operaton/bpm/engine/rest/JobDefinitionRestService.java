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
package org.operaton.bpm.engine.rest;

import org.operaton.bpm.engine.management.JobDefinitionQuery;
import org.operaton.bpm.engine.rest.dto.CountResultDto;
import org.operaton.bpm.engine.rest.dto.management.JobDefinitionDto;
import org.operaton.bpm.engine.rest.dto.management.JobDefinitionQueryDto;
import org.operaton.bpm.engine.rest.dto.management.JobDefinitionSuspensionStateDto;
import org.operaton.bpm.engine.rest.sub.management.JobDefinitionResource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;

/**
 * @author roman.smirnov
 */
@Produces(MediaType.APPLICATION_JSON)
public interface JobDefinitionRestService {

  String PATH = "/job-definition";

  @Path("/{id}")
  JobDefinitionResource getJobDefinition(@PathParam("id") String jobDefinitionId);

  /**
   * Exposes the {@link JobDefinitionQuery} interface as a REST service.
   * @param uriInfo
   * @param firstResult
   * @param maxResults
   * @return
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  List<JobDefinitionDto> getJobDefinitions(@Context UriInfo uriInfo,
      @QueryParam("firstResult") Integer firstResult, @QueryParam("maxResults") Integer maxResults);

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  List<JobDefinitionDto> queryJobDefinitions(JobDefinitionQueryDto queryDto,
      @QueryParam("firstResult") Integer firstResult,
      @QueryParam("maxResults") Integer maxResults);

  @GET
  @Path("/count")
  @Produces(MediaType.APPLICATION_JSON)
  CountResultDto getJobDefinitionsCount(@Context UriInfo uriInfo);

  @POST
  @Path("/count")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  CountResultDto queryJobDefinitionsCount(JobDefinitionQueryDto queryDto);

  @PUT
  @Path("/suspended")
  @Consumes(MediaType.APPLICATION_JSON)
  void updateSuspensionState(JobDefinitionSuspensionStateDto dto);

}

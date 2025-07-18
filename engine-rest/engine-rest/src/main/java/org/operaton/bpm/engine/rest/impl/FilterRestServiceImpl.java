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
package org.operaton.bpm.engine.rest.impl;

import static org.operaton.bpm.engine.authorization.Authorization.ANY;
import static org.operaton.bpm.engine.authorization.Permissions.CREATE;
import static org.operaton.bpm.engine.authorization.Resources.FILTER;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.operaton.bpm.engine.EntityTypes;
import org.operaton.bpm.engine.FilterService;
import org.operaton.bpm.engine.ProcessEngine;
import org.operaton.bpm.engine.exception.NotValidException;
import org.operaton.bpm.engine.filter.Filter;
import org.operaton.bpm.engine.filter.FilterQuery;
import org.operaton.bpm.engine.rest.FilterRestService;
import org.operaton.bpm.engine.rest.dto.CountResultDto;
import org.operaton.bpm.engine.rest.dto.ResourceOptionsDto;
import org.operaton.bpm.engine.rest.dto.runtime.FilterDto;
import org.operaton.bpm.engine.rest.dto.runtime.FilterQueryDto;
import org.operaton.bpm.engine.rest.exception.InvalidRequestException;
import org.operaton.bpm.engine.rest.sub.runtime.FilterResource;
import org.operaton.bpm.engine.rest.sub.runtime.impl.FilterResourceImpl;
import org.operaton.bpm.engine.rest.util.QueryUtil;


/**
 * @author Sebastian Menski
 */
public class FilterRestServiceImpl extends AbstractAuthorizedRestResource implements FilterRestService {

  public FilterRestServiceImpl(String engineName, ObjectMapper objectMapper) {
    super(engineName, FILTER, ANY, objectMapper);
  }

  @Override
  public FilterResource getFilter(String filterId) {
    return new FilterResourceImpl(getProcessEngine().getName(), getObjectMapper(), filterId, relativeRootResourcePath);
  }

  @Override
  public List<FilterDto> getFilters(UriInfo uriInfo, Boolean itemCount, Integer firstResult, Integer maxResults) {
    FilterService filterService = getProcessEngine().getFilterService();
    FilterQuery query = getQueryFromQueryParameters(uriInfo.getQueryParameters());

    List<Filter> matchingFilters = QueryUtil.list(query, firstResult, maxResults);

    List<FilterDto> filters = new ArrayList<>();
    for (Filter filter : matchingFilters) {
      FilterDto dto = FilterDto.fromFilter(filter);
      if (itemCount != null && itemCount) {
        dto.setItemCount(filterService.count(filter.getId()));
      }
      filters.add(dto);
    }

    return filters;
  }

  @Override
  public CountResultDto getFiltersCount(UriInfo uriInfo) {
    FilterQuery query = getQueryFromQueryParameters(uriInfo.getQueryParameters());
    return new CountResultDto(query.count());
  }

  @Override
  public FilterDto createFilter(FilterDto filterDto) {
    FilterService filterService = getProcessEngine().getFilterService();

    String resourceType = filterDto.getResourceType();

    Filter filter;

    if (EntityTypes.TASK.equals(resourceType)) {
      filter = filterService.newTaskFilter();
    }
    else {
      throw new InvalidRequestException(Response.Status.BAD_REQUEST, "Unable to create filter with invalid resource type '" + resourceType + "'");
    }

    try {
      filterDto.updateFilter(filter, getProcessEngine());
    }
    catch (NotValidException e) {
      throw new InvalidRequestException(Response.Status.BAD_REQUEST, e, "Unable to create filter with invalid content");
    }

    filterService.saveFilter(filter);

    return FilterDto.fromFilter(filter);
  }

  protected FilterQuery getQueryFromQueryParameters(MultivaluedMap<String, String> queryParameters) {
    ProcessEngine engine = getProcessEngine();
    FilterQueryDto queryDto = new FilterQueryDto(getObjectMapper(), queryParameters);
    return queryDto.toQuery(engine);
  }

  @Override
  public ResourceOptionsDto availableOperations(UriInfo context) {

    UriBuilder baseUriBuilder = context.getBaseUriBuilder()
      .path(relativeRootResourcePath)
      .path(FilterRestService.PATH);

    ResourceOptionsDto resourceOptionsDto = new ResourceOptionsDto();

    // GET /
    URI baseUri = baseUriBuilder.build();
    resourceOptionsDto.addReflexiveLink(baseUri, HttpMethod.GET, "list");

    // GET /count
    URI countUri = baseUriBuilder.clone().path("/count").build();
    resourceOptionsDto.addReflexiveLink(countUri, HttpMethod.GET, "count");

    // POST /create
    if (isAuthorized(CREATE)) {
      URI createUri = baseUriBuilder.clone().path("/create").build();
      resourceOptionsDto.addReflexiveLink(createUri, HttpMethod.POST, "create");
    }

    return resourceOptionsDto;
  }

}

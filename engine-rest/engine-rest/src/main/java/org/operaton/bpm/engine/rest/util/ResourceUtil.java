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
package org.operaton.bpm.engine.rest.util;

import org.operaton.bpm.engine.authorization.Resource;

public class ResourceUtil implements Resource {

  protected String resourceName;
  protected int resourceType;

  public ResourceUtil(String resourceName, int resourceType) {
    this.resourceName = resourceName;
    this.resourceType = resourceType;
  }

  @Override
  public String resourceName() {
    return resourceName;
  }

  @Override
  public int resourceType() {
    return resourceType;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((resourceName == null) ? 0 : resourceName.hashCode());
    result = prime * result + resourceType;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ResourceUtil other = (ResourceUtil) obj;
    if (resourceName == null) {
      if (other.resourceName != null)
        return false;
    } else if (!resourceName.equals(other.resourceName))
      return false;
    if (resourceType != other.resourceType)
      return false;
    return true;
  }
}

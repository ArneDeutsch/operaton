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
package org.operaton.bpm.engine.spring;

import jakarta.el.ELContext;
import org.operaton.bpm.engine.ProcessEngineException;
import jakarta.el.ELResolver;
import org.springframework.context.ApplicationContext;

/**
 * @author Tom Baeyens
 * @author Frederik Heremans
 */
public class ApplicationContextElResolver extends ELResolver {

  protected ApplicationContext applicationContext;

  public ApplicationContextElResolver(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Override
  public Object getValue(ELContext context, Object base, Object property) {
    if (base == null) {
      // according to javadoc, can only be a String
      String key = (String) property;

      if (applicationContext.containsBean(key)) {
        context.setPropertyResolved(true);
        return applicationContext.getBean(key);
      }
    }

    return null;
  }

  @Override
  public boolean isReadOnly(ELContext context, Object base, Object property) {
    return true;
  }

  @Override
  public void setValue(ELContext context, Object base, Object property, Object value) {
    if(base == null) {
      String key = (String) property;
      if (applicationContext.containsBean(key)) {
        throw new ProcessEngineException("Cannot set value of '" + property +
          "', it resolves to a bean defined in the Spring application-context.");
      }
    }
  }

  @Override
  public Class< ? > getCommonPropertyType(ELContext context, Object arg) {
    return Object.class;
  }

  @Override
  public Class< ? > getType(ELContext context, Object arg1, Object arg2) {
    return Object.class;
  }
}

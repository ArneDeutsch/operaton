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
package org.operaton.bpm.engine.impl.el;

import jakarta.el.ELContext;
import org.operaton.bpm.engine.ProcessEngineException;
import jakarta.el.ELResolver;

import java.util.Map;

/**
 * An {@link ELResolver} that exposed object values in the map, under the name of the entry's key.
 * The values in the map are only returned when requested property has no 'base', meaning
 * it's a root-object.
 *
 * @author Frederik Heremans
 */
public class ReadOnlyMapELResolver extends ELResolver {

  protected Map<Object, Object> wrappedMap;

  public ReadOnlyMapELResolver(Map<Object, Object> map) {
    this.wrappedMap = map;
  }

  public Object getValue(ELContext context, Object base, Object property) {
    if (base == null && wrappedMap.containsKey(property)) {
      context.setPropertyResolved(true);
      return wrappedMap.get(property);
    }
    return null;
  }

  public boolean isReadOnly(ELContext context, Object base, Object property) {
    return true;
  }

  public void setValue(ELContext context, Object base, Object property, Object value) {
    if(base == null && wrappedMap.containsKey(property)) {
      throw new ProcessEngineException("Cannot set value of '" + property + "', it's readonly!");
    }
  }

  public Class< ? > getCommonPropertyType(ELContext context, Object arg) {
    return Object.class;
  }

  public Class< ? > getType(ELContext context, Object arg1, Object arg2) {
    return Object.class;
  }
}

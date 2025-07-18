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
package org.operaton.bpm.engine.impl.cfg;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.operaton.bpm.engine.ProcessEngineException;
import org.springframework.beans.factory.BeanFactory;


/**
 * @author Tom Baeyens
 */
public class SpringBeanFactoryProxyMap implements Map<Object, Object> {

  protected BeanFactory beanFactory;

  public SpringBeanFactoryProxyMap(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  @Override
  public Object get(Object key) {
    if ( (key==null) || (!String.class.isAssignableFrom(key.getClass())) ) {
      return null;
    }
    return beanFactory.getBean((String) key);
  }

  @Override
  public boolean containsKey(Object key) {
    if ( (key==null) || (!String.class.isAssignableFrom(key.getClass())) ) {
      return false;
    }
    return beanFactory.containsBean((String) key);
  }

  @Override
  public Set<Object> keySet() {
    return Collections.emptySet();
  }

  @Override
  public void clear() {
    throw new ProcessEngineException("can't clear configuration beans");
  }

  @Override
  public boolean containsValue(Object value) {
    throw new ProcessEngineException("can't search values in configuration beans");
  }

  @Override
  public Set<java.util.Map.Entry<Object, Object>> entrySet() {
    throw new ProcessEngineException("unsupported operation on configuration beans");
  }

  @Override
  public boolean isEmpty() {
    throw new ProcessEngineException("unsupported operation on configuration beans");
  }

  @Override
  public Object put(Object key, Object value) {
    throw new ProcessEngineException("unsupported operation on configuration beans");
  }

  public void putAll(Map< ? extends Object, ? extends Object> m) {
    throw new ProcessEngineException("unsupported operation on configuration beans");
  }

  @Override
  public Object remove(Object key) {
    throw new ProcessEngineException("unsupported operation on configuration beans");
  }

  @Override
  public int size() {
    throw new ProcessEngineException("unsupported operation on configuration beans");
  }

  @Override
  public Collection<Object> values() {
    throw new ProcessEngineException("unsupported operation on configuration beans");
  }
}

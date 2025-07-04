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
package org.operaton.bpm.engine.cdi.impl;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;

import org.operaton.bpm.engine.cdi.annotation.BusinessProcessScoped;
import org.operaton.bpm.engine.cdi.impl.context.BusinessProcessContext;
import org.operaton.bpm.engine.cdi.impl.util.BeanManagerLookup;

/**
 *
 *
 * @author Daniel Meyer
 */
public class ProcessEngineExtension implements Extension {

  public void beforeBeanDiscovery(@Observes final BeforeBeanDiscovery event, BeanManager manager) {
    event.addScope(BusinessProcessScoped.class, true, true);

    BeanManagerLookup.localInstance = manager;
  }

  public void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager manager) {
    event.addContext(new BusinessProcessContext(manager));
  }

}

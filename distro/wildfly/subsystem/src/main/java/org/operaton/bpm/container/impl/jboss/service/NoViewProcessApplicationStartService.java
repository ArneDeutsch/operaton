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
package org.operaton.bpm.container.impl.jboss.service;

import java.util.function.Consumer;

import org.operaton.bpm.application.ProcessApplicationInterface;
import org.operaton.bpm.application.ProcessApplicationReference;
import org.operaton.bpm.application.ProcessApplicationUnavailableException;
import org.operaton.bpm.application.impl.ServletProcessApplication;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * <p>Start Service for process applications that do not expose an EE Component View
 * (like {@link ServletProcessApplication}</p>
 *
 * @author Daniel Meyer
 *
 */
public class NoViewProcessApplicationStartService implements Service<ProcessApplicationInterface> {

  protected ProcessApplicationReference reference;

  protected final Consumer<ProcessApplicationInterface> provider;

  public NoViewProcessApplicationStartService(ProcessApplicationReference reference, Consumer<ProcessApplicationInterface> provider) {
    this.reference = reference;
    this.provider = provider;
  }

  @Override
  public ProcessApplicationInterface getValue() throws IllegalStateException, IllegalArgumentException {
    try {
      return reference.getProcessApplication();

    } catch (ProcessApplicationUnavailableException e) {
      throw new IllegalStateException("Process application '" + reference.getName() + "' is not available.", e);
    }
  }

  @Override
  public void start(StartContext context) throws StartException {
    try {
      provider.accept(reference.getProcessApplication());
    } catch (ProcessApplicationUnavailableException e) {
      throw new IllegalStateException("Process application '" + reference.getName() + "' is not available during "
          + NoViewProcessApplicationStartService.class + " start.", e);
    }
  }

  @Override
  public void stop(StopContext context) {
    provider.accept(null);
  }

}

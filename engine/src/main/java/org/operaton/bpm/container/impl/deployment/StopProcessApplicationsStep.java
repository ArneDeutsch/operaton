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
package org.operaton.bpm.container.impl.deployment;

import java.util.List;

import org.operaton.bpm.application.ProcessApplicationInterface;
import org.operaton.bpm.application.ProcessApplicationReference;
import org.operaton.bpm.container.impl.ContainerIntegrationLogger;
import org.operaton.bpm.container.impl.jmx.services.JmxManagedProcessApplication;
import org.operaton.bpm.container.impl.spi.DeploymentOperation;
import org.operaton.bpm.container.impl.spi.DeploymentOperationStep;
import org.operaton.bpm.container.impl.spi.PlatformServiceContainer;
import org.operaton.bpm.container.impl.spi.ServiceTypes;
import org.operaton.bpm.engine.impl.ProcessEngineLogger;

/**
 * <p>Deployment operation step that is responsible for stopping (undeploying) all process applications</p>
 *
 * @author Daniel Meyer
 *
 */
public class StopProcessApplicationsStep extends DeploymentOperationStep {

  private static final ContainerIntegrationLogger LOG = ProcessEngineLogger.CONTAINER_INTEGRATION_LOGGER;

  @Override
  public String getName() {
    return "Stopping process applications";
  }

  @Override
  public void performOperationStep(DeploymentOperation operationContext) {

    final PlatformServiceContainer serviceContainer = operationContext.getServiceContainer();
    List<JmxManagedProcessApplication> processApplicationsReferences = serviceContainer.getServiceValuesByType(ServiceTypes.PROCESS_APPLICATION);

    for (JmxManagedProcessApplication processApplication : processApplicationsReferences) {
      stopProcessApplication(processApplication.getProcessApplicationReference());
    }

  }

  /**
   * <p> Stops a process application. Exceptions are logged but not re-thrown).
   *
   * @param processApplicationReference
   */
  protected void stopProcessApplication(ProcessApplicationReference processApplicationReference) {

    try {
      // unless the user has overridden the stop behavior,
      // this causes the process application to remove its services
      // (triggers nested undeployment operation)
      ProcessApplicationInterface processApplication = processApplicationReference.getProcessApplication();
      processApplication.undeploy();
    }
    catch(Throwable t) {
      LOG.exceptionWhileStopping("Process Application", processApplicationReference.getName(), t);
    }

  }

}

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
package org.operaton.bpm.container.impl.jmx.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.operaton.bpm.application.ProcessApplicationReference;
import org.operaton.bpm.application.impl.ProcessApplicationInfoImpl;
import org.operaton.bpm.application.impl.metadata.spi.ProcessesXml;
import org.operaton.bpm.container.impl.deployment.util.DeployedProcessArchive;
import org.operaton.bpm.container.impl.spi.PlatformService;
import org.operaton.bpm.container.impl.spi.PlatformServiceContainer;

/**
 *
 * @author Daniel Meyer
 *
 */
public class JmxManagedProcessApplication implements PlatformService<JmxManagedProcessApplication>, JmxManagedProcessApplicationMBean {

  protected final ProcessApplicationInfoImpl processApplicationInfo;
  protected final ProcessApplicationReference processApplicationReference;

  protected List<ProcessesXml> processesXmls;
  protected Map<String, DeployedProcessArchive> deploymentMap;

  public JmxManagedProcessApplication(ProcessApplicationInfoImpl processApplicationInfo, ProcessApplicationReference processApplicationReference) {
    this.processApplicationInfo = processApplicationInfo;
    this.processApplicationReference = processApplicationReference;
  }

  @Override
  public String getProcessApplicationName() {
		return processApplicationInfo.getName();
	}

  @Override
  public void start(PlatformServiceContainer mBeanServiceContainer) {
    // no-op
  }

  @Override
  public void stop(PlatformServiceContainer mBeanServiceContainer) {
    // no-op
  }

  @Override
  public JmxManagedProcessApplication getValue() {
		return this;
	}

  public void setProcessesXmls(List<ProcessesXml> processesXmls) {
    this.processesXmls = processesXmls;
  }

  public List<ProcessesXml> getProcessesXmls() {
    return processesXmls;
  }

  public void setDeploymentMap(Map<String, DeployedProcessArchive> processArchiveDeploymentMap) {
    this.deploymentMap = processArchiveDeploymentMap;
  }

  public Map<String, DeployedProcessArchive> getProcessArchiveDeploymentMap() {
    return deploymentMap;
  }

  @Override
  public List<String> getDeploymentIds() {
    List<String> deploymentIds = new ArrayList<>();
    for (DeployedProcessArchive registration : deploymentMap.values()) {
      deploymentIds.addAll(registration.getAllDeploymentIds());
    }
    return deploymentIds;
  }

  @Override
  public List<String> getDeploymentNames() {
    return new ArrayList<>(deploymentMap.keySet());
  }

  public ProcessApplicationInfoImpl getProcessApplicationInfo() {
    return processApplicationInfo;
  }

  public ProcessApplicationReference getProcessApplicationReference() {
    return processApplicationReference;
  }

}

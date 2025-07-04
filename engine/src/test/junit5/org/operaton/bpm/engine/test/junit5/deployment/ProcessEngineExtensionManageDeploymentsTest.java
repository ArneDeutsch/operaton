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
package org.operaton.bpm.engine.test.junit5.deployment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.operaton.bpm.engine.repository.Deployment;
import org.operaton.bpm.engine.test.junit5.ProcessEngineExtension;

public class ProcessEngineExtensionManageDeploymentsTest {

  public static final String SUB_PROCESS = "processes/subProcess.bpmn";

  @RegisterExtension
  static ProcessEngineExtension extension = ProcessEngineExtension.builder()
    // fail if DB is dirty after test
    .ensureCleanAfterTest(true)
    .build();

  @Test
  void shouldCleanUpManagedDeployments() {
    // given
    Deployment deployment = extension.getRepositoryService().createDeployment().addClasspathResource(SUB_PROCESS).deploy();

    // when
    extension.manageDeployment(deployment);

    // then
    // additional deployments should be cleaned up
    // DB should be clean. Extension would fail test if DB was dirty due to
    // ensureCleanAfterTest(true)
  }

}

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
package org.operaton.bpm.engine.test.api.authorization.externaltask;

import static org.assertj.core.api.Assertions.assertThat;

import org.operaton.bpm.engine.externaltask.ExternalTask;
import org.operaton.bpm.engine.externaltask.LockedExternalTask;
import org.operaton.bpm.engine.test.junit5.ParameterizedTestExtension.Parameterized;

/**
 * @author Thorben Lindhauer
 * @author Christopher Zell
 */
@Parameterized
public class HandleExternalTaskFailureAuthorizationTest extends HandleLockedExternalTaskAuthorizationTest {

  @Override
  public void testExternalTaskApi(LockedExternalTask task) {
    engineRule.getExternalTaskService().handleFailure(task.getId(), "workerId", "error", 5, 5000L);
  }

  @Override
  public void assertExternalTaskResults() {
    ExternalTask externalTask = engineRule.getExternalTaskService()
      .createExternalTaskQuery().singleResult();

    assertThat((int) externalTask.getRetries()).isEqualTo(5);
    assertThat(externalTask.getErrorMessage()).isEqualTo("error");
  }
}

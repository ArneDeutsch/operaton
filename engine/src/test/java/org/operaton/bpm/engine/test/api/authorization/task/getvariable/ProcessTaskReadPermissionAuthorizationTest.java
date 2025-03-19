/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.operaton.bpm.engine.test.api.authorization.task.getvariable;

import static org.operaton.bpm.engine.authorization.Permissions.READ;
import static org.operaton.bpm.engine.authorization.Permissions.READ_TASK;
import static org.operaton.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.operaton.bpm.engine.authorization.Resources.TASK;
import static org.operaton.bpm.engine.test.api.authorization.util.AuthorizationScenario.scenario;
import static org.operaton.bpm.engine.test.api.authorization.util.AuthorizationSpec.grant;

import java.util.Collection;

import org.operaton.bpm.engine.test.api.authorization.util.AuthorizationScenario;
import org.operaton.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.operaton.bpm.engine.test.junit5.ParameterizedTestExtension.Parameterized;
import org.operaton.bpm.engine.test.junit5.ParameterizedTestExtension.Parameters;

/**
 * @author Yana.Vasileva
 *
 */
@Parameterized
public class ProcessTaskReadPermissionAuthorizationTest extends ProcessTaskAuthorizationTest {

  @Parameters
  public static Collection<AuthorizationScenario[]> scenarios() {
    return AuthorizationTestRule.asParameters(
      scenario()
        .withoutAuthorizations()
        .failsDueToRequired(
          grant(TASK, "taskId", USER_ID, READ),
          grant(PROCESS_DEFINITION, PROCESS_KEY, USER_ID, READ_TASK)),
      scenario()
        .withAuthorizations(
          grant(TASK, "taskId", USER_ID, READ)),
      scenario()
        .withAuthorizations(
          grant(TASK, "*", USER_ID, READ)),
      scenario()
        .withAuthorizations(
          grant(PROCESS_DEFINITION, PROCESS_KEY, USER_ID, READ_TASK)),
      scenario()
        .withAuthorizations(
          grant(PROCESS_DEFINITION, "*", USER_ID, READ_TASK))
        .succeeds()
      );
  }

}

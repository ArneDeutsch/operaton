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
package org.operaton.bpm.qa.rolling.update.timestamp;

import org.operaton.bpm.engine.ExternalTaskService;
import org.operaton.bpm.engine.HistoryService;
import org.operaton.bpm.engine.IdentityService;
import org.operaton.bpm.engine.ManagementService;
import org.operaton.bpm.engine.RepositoryService;
import org.operaton.bpm.engine.RuntimeService;
import org.operaton.bpm.engine.TaskService;
import org.operaton.bpm.engine.test.ProcessEngineRule;
import org.operaton.bpm.qa.rolling.update.AbstractRollingUpdateTestCase;
import org.junit.Before;
import org.junit.Rule;

import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Nikola Koevski
 */
public abstract class AbstractTimestampUpdateTest extends AbstractRollingUpdateTestCase {

  protected static final long TIME = 1548082136000L;
  protected static final Date TIMESTAMP = new Date(TIME);

  protected RuntimeService runtimeService;
  protected ManagementService managementService;

  @Before
  public void setUp() {
    runtimeService = rule.getRuntimeService();
    managementService = rule.getManagementService();
  }

  protected void assertNotNull(Object object) {
    assertThat(object, is(notNullValue()));
  }
}

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
package org.operaton.bpm.engine.test.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.operaton.bpm.engine.impl.cmd.DeployCmd;
import org.operaton.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.operaton.bpm.engine.impl.interceptor.CommandContext;
import org.operaton.bpm.engine.impl.repository.DeploymentBuilderImpl;
import org.operaton.bpm.engine.impl.test.RequiredDatabase;
import org.operaton.bpm.engine.repository.Deployment;
import org.operaton.bpm.engine.repository.DeploymentBuilder;
import org.operaton.bpm.engine.repository.DeploymentQuery;
import org.operaton.bpm.engine.repository.ProcessDefinition;
import org.operaton.bpm.model.bpmn.Bpmn;
import org.operaton.bpm.model.bpmn.BpmnModelInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * <p>Tests the deployment from two threads simultaneously.</p>
 *
 * <p><b>Note:</b> the tests are not execute on H2 because it doesn't support the
 * exclusive lock on the deployment table.</p>
 *
 * @author Daniel Meyer
 */
@RequiredDatabase(excludes = DbSqlSessionFactory.H2)
class ConcurrentDeploymentTest extends ConcurrencyTestCase {

  private static String processResource;

  static {
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess().startEvent().done();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Bpmn.writeModelToStream(outputStream, modelInstance);
    processResource = new String(outputStream.toByteArray());
  }

  protected ThreadControl thread1;
  protected ThreadControl thread2;

  /**
   * @see <a href="https://app.camunda.com/jira/browse/CAM-2128">https://app.camunda.com/jira/browse/CAM-2128</a>
   */
  @Test
  void testDuplicateFiltering() throws InterruptedException {

    deployOnTwoConcurrentThreads(
        createDeploymentBuilder().enableDuplicateFiltering(false),
        createDeploymentBuilder().enableDuplicateFiltering(false));

    // ensure that although both transactions were run concurrently, only one deployment was constructed.
    assertThat(thread1.getException()).isNull();
    DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();
    assertThat(deploymentQuery.count()).isEqualTo(1L);
  }

  @Test
  void testVersioning() throws InterruptedException {

    deployOnTwoConcurrentThreads(
        createDeploymentBuilder(),
        createDeploymentBuilder()
        );

    // ensure that although both transactions were run concurrently, the process definitions have different versions
    List<ProcessDefinition> processDefinitions = repositoryService
        .createProcessDefinitionQuery()
        .orderByProcessDefinitionVersion()
        .asc()
        .list();

    assertThat(processDefinitions).hasSize(2);
    assertThat(processDefinitions.get(0).getVersion()).isEqualTo(1);
    assertThat(processDefinitions.get(1).getVersion()).isEqualTo(2);
  }

  protected DeploymentBuilder createDeploymentBuilder() {
    return new DeploymentBuilderImpl(null)
        .name("some-deployment-name")
        .addString("foo.bpmn", processResource);
  }

  protected void deployOnTwoConcurrentThreads(DeploymentBuilder deploymentOne, DeploymentBuilder deploymentTwo) throws InterruptedException {
    assertThat(deploymentOne)
        .as("you can not use the same deployment builder for both deployments")
        .isNotEqualTo(deploymentTwo);

    // STEP 1: bring two threads to a point where they have
    // 1) started a new transaction
    // 2) are ready to deploy
    thread1 = executeControllableCommand(new ControllableDeployCommand(deploymentOne));
    thread1.reportInterrupts();
    thread1.waitForSync();

    thread2 = executeControllableCommand(new ControllableDeployCommand(deploymentTwo));
    thread2.reportInterrupts();
    thread2.waitForSync();

    // STEP 2: make Thread 1 proceed and wait until it has deployed but not yet committed
    // -> will still hold the exclusive lock
    thread1.makeContinue();
    thread1.waitForSync();

    // STEP 3: make Thread 2 continue
    // -> it will attempt to acquire the exclusive lock and block on the lock
    thread2.makeContinue();

    // wait for 2 seconds (Thread 2 is blocked on the lock)
    Thread.sleep(2000);

    // STEP 4: allow Thread 1 to terminate
    // -> Thread 1 will commit and release the lock
    thread1.waitUntilDone();

    // STEP 5: wait for Thread 2 to terminate
    thread2.waitForSync();
    thread2.waitUntilDone();
  }

  @AfterEach
  void tearDown() {

    for(Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }

    processEngineConfiguration.getDeploymentCache().purgeCache();
  }

  protected static class ControllableDeployCommand extends ControllableCommand<Void> {

    private final DeploymentBuilder deploymentBuilder;

    public ControllableDeployCommand(DeploymentBuilder deploymentBuilder) {
      this.deploymentBuilder = deploymentBuilder;
    }

    @Override
    public Void execute(CommandContext commandContext) {
      monitor.sync();  // thread will block here until makeContinue() is called from main thread

      new DeployCmd((DeploymentBuilderImpl) deploymentBuilder).execute(commandContext);

      monitor.sync();  // thread will block here until waitUntilDone() is called form main thread

      return null;
    }

  }

}

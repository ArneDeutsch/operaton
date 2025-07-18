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
package org.operaton.bpm.engine.impl.jobexecutor.historycleanup;

import org.operaton.bpm.engine.impl.cfg.TransactionState;
import org.operaton.bpm.engine.impl.interceptor.CommandContext;
import org.operaton.bpm.engine.impl.interceptor.CommandExecutor;
import org.operaton.bpm.engine.impl.jobexecutor.JobHandler;
import org.operaton.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.operaton.bpm.engine.impl.util.ClockUtil;
import org.operaton.bpm.engine.impl.util.JsonUtil;
import com.google.gson.JsonObject;

import static org.operaton.bpm.engine.ProcessEngineConfiguration.HISTORY_CLEANUP_STRATEGY_REMOVAL_TIME_BASED;

/**
 * Job handler for history cleanup job.
 * @author Svetlana Dorokhova
 */
public class HistoryCleanupJobHandler implements JobHandler<HistoryCleanupJobHandlerConfiguration> {

  public static final String TYPE = "history-cleanup";

  @Override
  public void execute(HistoryCleanupJobHandlerConfiguration configuration, ExecutionEntity execution, CommandContext commandContext, String tenantId) {

    HistoryCleanupHandler cleanupHandler = initCleanupHandler(configuration, commandContext);

    if (configuration.isImmediatelyDue() || isWithinBatchWindow(commandContext) ) {
      cleanupHandler.performCleanup();
    }

    commandContext.getTransactionContext()
      .addTransactionListener(TransactionState.COMMITTED, cleanupHandler);

  }

  protected HistoryCleanupRemovalTime getTimeBasedHandler() {
    return new HistoryCleanupRemovalTime();
  }

  protected HistoryCleanupHandler initCleanupHandler(HistoryCleanupJobHandlerConfiguration configuration, CommandContext commandContext) {
    HistoryCleanupHandler cleanupHandler;

    if (isHistoryCleanupStrategyRemovalTimeBased(commandContext)) {
      cleanupHandler = getTimeBasedHandler();
    } else {
      cleanupHandler = new HistoryCleanupBatch();
    }

    CommandExecutor commandExecutor = commandContext.getProcessEngineConfiguration()
      .getCommandExecutorTxRequiresNew();

    String jobId = commandContext.getCurrentJob().getId();

    return cleanupHandler
      .setConfiguration(configuration)
      .setCommandExecutor(commandExecutor)
      .setJobId(jobId);
  }

  protected boolean isHistoryCleanupStrategyRemovalTimeBased(CommandContext commandContext) {
    String historyRemovalTimeStrategy = commandContext.getProcessEngineConfiguration()
      .getHistoryCleanupStrategy();

    return HISTORY_CLEANUP_STRATEGY_REMOVAL_TIME_BASED.equals(historyRemovalTimeStrategy);
  }

  protected boolean isWithinBatchWindow(CommandContext commandContext) {
    return HistoryCleanupHelper.isWithinBatchWindow(ClockUtil.getCurrentTime(), commandContext.getProcessEngineConfiguration());
  }

  @Override
  public HistoryCleanupJobHandlerConfiguration newConfiguration(String canonicalString) {
    JsonObject jsonObject = JsonUtil.asObject(canonicalString);
    return HistoryCleanupJobHandlerConfiguration.fromJson(jsonObject);
  }

  @Override
  public String getType() {
    return TYPE;
  }

}

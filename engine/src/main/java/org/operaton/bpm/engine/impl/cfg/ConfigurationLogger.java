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
package org.operaton.bpm.engine.impl.cfg;

import javax.naming.NamingException;
import org.operaton.bpm.engine.ProcessEngineException;
import org.operaton.bpm.engine.exception.NotValidException;
import org.operaton.bpm.engine.impl.ProcessEngineLogger;

/**
 * @author Daniel Meyer
 *
 */
public class ConfigurationLogger extends ProcessEngineLogger {

  public ProcessEngineException invalidConfigTransactionManagerIsNull() {
    return new ProcessEngineException(exceptionMessage(
        "001",
        "Property 'transactionManager' is null and 'transactionManagerJndiName' is not set. "
        + "Please set either the 'transactionManager' property or the 'transactionManagerJndiName' property."));
  }

  public ProcessEngineException invalidConfigCannotFindTransactionManger(String jndiName, NamingException e) {
    return new ProcessEngineException(exceptionMessage(
        "002",
        "Cannot lookup instance of Jta Transaction manager in JNDI using name '{}'", jndiName), e);
  }

  public void pluginActivated(String pluginName, String processEngineName) {
    logInfo(
        "003", "Plugin '{}' activated on process engine '{}'", pluginName, processEngineName);
  }

  public void debugDatabaseproductName(String databaseProductName) {
    logDebug(
        "004", "Database product name {}", databaseProductName);
  }

  public void debugDatabaseType(String databaseType) {
    logDebug(
        "005", "Database type {}", databaseType);
  }

  public void usingDeprecatedHistoryLevelVariable() {
    logWarn(
        "006", "Using deprecated history level 'variable'. " +
            "This history level is deprecated and replaced by 'activity'. " +
            "Consider using 'ACTIVITY' instead.");
  }

  public ProcessEngineException invalidConfigDefaultUserPermissionNameForTask(String defaultUserPermissionNameForTask, String[] validPermissionNames) {
    return new ProcessEngineException(exceptionMessage(
        "007",
        "Invalid value '{}' for configuration property 'defaultUserPermissionNameForTask'. Valid values are: '{}'", defaultUserPermissionNameForTask, validPermissionNames));
  }

  public ProcessEngineException invalidPropertyValue(String propertyName, String propertyValue) {
    return new ProcessEngineException(exceptionMessage(
        "008",
        "Invalid value '{}' for configuration property '{}'.", propertyValue, propertyName));
  }

  public ProcessEngineException invalidPropertyValue(String propertyName, String propertyValue, String reason) {
    return new ProcessEngineException(exceptionMessage(
      "009",
      "Invalid value '{}' for configuration property '{}': {}.", propertyValue, propertyName, reason));
  }

  public void invalidBatchOperation(String operation, String historyTimeToLive) {
    logWarn(
      "010", "Invalid batch operation name '{}' with history time to live set to'{}'" , operation, historyTimeToLive);
  }

  public ProcessEngineException invalidPropertyValue(String propertyName, String propertyValue, Exception e) {
    return new ProcessEngineException(exceptionMessage(
      "011",
      "Invalid value '{}' for configuration property '{}'.", propertyValue, propertyName), e);
  }

  public ProcessEngineException databaseConnectionAccessException(Exception cause) {
    return new ProcessEngineException(exceptionMessage(
      "012",
      "Exception on accessing the database connection: {}", cause.getMessage()), cause);
  }

  public void databaseConnectionCloseException(Exception cause) {
    logError(
      "013",
      "Exception on closing the database connection: {}", cause.getMessage());
  }

  public void invalidBatchTypeForInvocationsPerBatchJob(String batchType) {
    logWarn(
        "014", "The configuration property 'invocationsPerJobByBatchType' " +
            "contains an invalid batch type '{}' which is neither a custom nor a built-in " +
            "batch type", batchType);
  }

  public void invalidPropertyValue(Exception e) {
    logError("015", "Exception while reading configuration property: {}", e.getMessage());
  }

  /**
   * Method for logging message when model TTL longer than global TTL.
   *
   * @param definitionKey the correlated definition key with which history TTL is related to.
   *                      For processes related to httl, that is the processDefinitionKey, for cases the case definition key
   *                      whereas for decisions is the decision definition key.
   */
  public void logModelHTTLLongerThanGlobalConfiguration(String definitionKey) {
    logWarn(
        "017", "definitionKey: {}; "
            + "The specified Time To Live (TTL) in the model is longer than the global TTL configuration. "
            + "The historic data related to this model will be cleaned up at later point comparing to the other processes.",
            definitionKey);
  }

  public NotValidException logErrorNoTTLConfigured() {
    return new NotValidException(exceptionMessage("018", """
        History Time To Live (TTL) cannot be null. \
        TTL is necessary for the History Cleanup to work. The following options are possible:
        * Set historyTimeToLive in the model
        * Set a default historyTimeToLive as a global process engine configuration
        * (Not recommended) Deactivate the enforceTTL config to disable this check"""));
  }

  public ProcessEngineException invalidTransactionIsolationLevel(String transactionIsolationLevel) {
    return new ProcessEngineException(exceptionMessage("019",
        "The transaction isolation level set for the database is '{}' which differs from the recommended value. "
            + "Please change the isolation level to 'READ_COMMITTED' or set property 'skipIsolationLevelCheck' to true. "
            + "Please keep in mind that some levels are known to cause deadlocks and other unexpected behaviours.",
        transactionIsolationLevel));

  }

  public void logSkippedIsolationLevelCheck(String transactionIsolationLevel) {
    logWarn("020", "The transaction isolation level set for the database is '{}' which differs from the recommended value "
            + "and property skipIsolationLevelCheck is enabled. "
            + "Please keep in mind that levels different from 'READ_COMMITTED' are known to cause deadlocks and other unexpected behaviours.",
        transactionIsolationLevel);
  }
}

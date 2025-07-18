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
package org.operaton.bpm.spring.boot.starter.event;

import org.operaton.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.operaton.bpm.spring.boot.starter.property.OperatonBpmProperties;
import org.operaton.bpm.spring.boot.starter.property.EventingProperty;
import org.operaton.bpm.spring.boot.starter.util.SpringBootProcessEnginePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Engine Plugin forwarding Operaton task, execution and history events as Spring Events.
 */
public class EventPublisherPlugin extends SpringBootProcessEnginePlugin {

  private static final Logger logger = LoggerFactory.getLogger(EventPublisherPlugin.class);

  private final EventingProperty property;
  private final ApplicationEventPublisher publisher;

  public EventPublisherPlugin(final OperatonBpmProperties properties, final ApplicationEventPublisher publisher) {
    this(properties.getEventing(), publisher);
  }

  public EventPublisherPlugin(final EventingProperty property, final ApplicationEventPublisher publisher) {
    this.property = property;
    this.publisher = publisher;
  }

  @Override
  public void preInit(SpringProcessEngineConfiguration processEngineConfiguration) {

    if (!property.isTask() && !property.isExecution() && !property.isHistory()) {
      logger.info("EVENTING-002: Operaton Spring Boot Eventing Plugin is found, but disabled via property.");
      return;
    }

    if (property.isTask() || property.isExecution()) {

      logger.info("EVENTING-001: Initialized Operaton Spring Boot Eventing Engine Plugin.");
      if (property.isTask()) {
        logger.info("EVENTING-003: Task events will be published as Spring Events.");
      } else {
        logger.info("EVENTING-004: Task eventing is disabled via property.");
      }

      if (property.isExecution()) {
        logger.info("EVENTING-005: Execution events will be published as Spring Events.");
      } else {
        logger.info("EVENTING-006: Execution eventing is disabled via property.");
      }
      if (property.isSkippable()) {
        logger.info("EVENTING-009: Listeners will not be invoked if a skipCustomListeners API parameter is set to true by user.");
      } else {
        logger.info("EVENTING-009: Listeners will always be invoked regardless of skipCustomListeners API parameters.");
      }
      // register parse listener
      processEngineConfiguration.getCustomPostBPMNParseListeners().add(new PublishDelegateParseListener(this.publisher, property));
    }

    if (property.isHistory()) {
      logger.info("EVENTING-007: History events will be published as Spring events.");
      // register composite DB event handler.
      processEngineConfiguration.getCustomHistoryEventHandlers()
          .add(new PublishHistoryEventHandler(this.publisher));
    } else {
      logger.info("EVENTING-008: History eventing is disabled via property.");
    }

  }

}

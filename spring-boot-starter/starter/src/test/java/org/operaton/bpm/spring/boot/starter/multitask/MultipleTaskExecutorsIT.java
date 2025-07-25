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
package org.operaton.bpm.spring.boot.starter.multitask;

import org.operaton.bpm.spring.boot.starter.AbstractOperatonAutoConfigurationIT;
import org.operaton.bpm.spring.boot.starter.configuration.impl.DefaultJobConfiguration.JobConfiguration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @see <a href=
 *      "https://github.com/operaton/operaton-bpm-spring-boot-starter/issues/209">#209</a>
 */
@SpringBootTest(classes = {MultipleTaskExecutorsIT.MultipleTaskExecutorsConfig.class})
class MultipleTaskExecutorsIT extends AbstractOperatonAutoConfigurationIT {

  private final TaskExecutor[] taskExecutors;
  private final TaskExecutor operatonTaskExecutor;

  @Autowired
  public MultipleTaskExecutorsIT(TaskExecutor[] taskExecutors, @Qualifier(JobConfiguration.CAMUNDA_TASK_EXECUTOR_QUALIFIER) TaskExecutor operatonTaskExecutor) {
      this.taskExecutors = taskExecutors;
      this.operatonTaskExecutor = operatonTaskExecutor;
  }

  @Test
  void startWithMultipleTaskExecutorsTest() {
    assertThat(taskExecutors)
        .hasSizeGreaterThan(1)
        .contains(operatonTaskExecutor);
  }

  @SpringBootApplication
  static class MultipleTaskExecutorsConfig {

    @Configuration
    @EnableWebSocketMessageBroker
    @SuppressWarnings("unused")
    public static class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

      @Override
      public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue", "/user");
        config.setApplicationDestinationPrefixes("/app");
      }

      @Override
      public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/gs-guide-websocket").withSockJS();
      }

    }
  }
}

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
package org.operaton.bpm.webapp.impl.security;

import org.operaton.bpm.cockpit.Cockpit;
import org.operaton.bpm.engine.IdentityService;
import org.operaton.bpm.engine.ProcessEngine;
import org.operaton.bpm.webapp.impl.security.auth.Authentication;
import org.operaton.bpm.webapp.impl.security.auth.Authentications;
import org.operaton.bpm.webapp.impl.security.auth.UserAuthentication;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;

/**
 * @author Daniel Meyer
 *
 */
public class SecurityActions {

  private SecurityActions() {
  }

  public static <T> T runWithAuthentications(SecurityAction<T> action, Authentications authentications) throws IOException, ServletException {

    List<UserAuthentication> currentAuthentications = authentications.getAuthentications();
    try {
      for (Authentication authentication : currentAuthentications) {
        authenticateProcessEngine(authentication);
      }

      return action.execute();

    } finally {
      for (Authentication authentication : currentAuthentications) {
        clearAuthentication(authentication);
      }
    }
  }

  private static void clearAuthentication(Authentication authentication) {
    ProcessEngine processEngine = Cockpit.getProcessEngine(authentication.getProcessEngineName());
    if(processEngine != null) {
      processEngine.getIdentityService().clearAuthentication();
    }
  }

  private static void authenticateProcessEngine(Authentication authentication) {

    ProcessEngine processEngine = Cockpit.getProcessEngine(authentication.getProcessEngineName());
    if (processEngine != null) {

      String userId = authentication.getIdentityId();
      List<String> groupIds = null;
      List<String> tenantIds = null;

      if (authentication instanceof UserAuthentication userAuthentication) {
        groupIds = userAuthentication.getGroupIds();
        tenantIds = userAuthentication.getTenantIds();
      }

      processEngine.getIdentityService().setAuthentication(userId, groupIds, tenantIds);
    }
  }

  public static <T> T runWithoutAuthentication(SecurityAction<T> action, ProcessEngine processEngine) throws IOException, ServletException {

    final IdentityService identityService = processEngine.getIdentityService();
    org.operaton.bpm.engine.impl.identity.Authentication currentAuth = identityService.getCurrentAuthentication();

    try {
      identityService.clearAuthentication();
      return action.execute();

    } finally {
      identityService.setAuthentication(currentAuth);

    }

  }
  public interface SecurityAction<T> {
    T execute() throws IOException, ServletException;

  }
}

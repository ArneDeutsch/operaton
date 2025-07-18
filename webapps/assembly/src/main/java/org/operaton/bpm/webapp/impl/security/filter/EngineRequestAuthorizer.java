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
package org.operaton.bpm.webapp.impl.security.filter;

import java.util.Map;

import org.operaton.bpm.webapp.impl.security.auth.Authentication;
import org.operaton.bpm.webapp.impl.security.auth.Authentications;

/**
 * <p>This is a {@link RequestAuthorizer} which authorizes all process engine api
 * requests based on the current authentication</p>
 *
 * @author Daniel Meyer
 * @author nico.rehwaldt
 */
public class EngineRequestAuthorizer implements RequestAuthorizer {

  @Override
  public Authorization authorize(Map<String, String> parameters) {

    Authentications authentications = Authentications.getCurrent();
    if (authentications == null) {
      // no authentications --> reject request to app
      return Authorization.denied(Authentication.ANONYMOUS);
    } else {
      String engineName = parameters.get("engine");

      Authentication engineAuth = authentications.getAuthenticationForProcessEngine(engineName);

      return Authorization.grantedUnlessNull(engineAuth);
    }
  }
}

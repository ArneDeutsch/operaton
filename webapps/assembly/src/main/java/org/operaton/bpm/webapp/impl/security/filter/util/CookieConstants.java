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
package org.operaton.bpm.webapp.impl.security.filter.util;

import java.util.regex.Pattern;

/**
 * @author Nikola Koevski
 */
public final class CookieConstants {

  public static final String SET_COOKIE_HEADER_NAME = "Set-Cookie";

  public static final String SAME_SITE_FIELD_NAME = ";SameSite=";
  public static final Pattern SAME_SITE_FIELD_NAME_REGEX = Pattern.compile(";\\w*SameSite\\w*=");

  public static final String SECURE_FLAG_NAME = ";Secure";
  public static final Pattern SECURE_FLAG_NAME_REGEX = Pattern.compile(";\\w*Secure");

  public static final String JSESSION_ID = "JSESSIONID";

  private CookieConstants() {
  }
}

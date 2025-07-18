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
package org.operaton.bpm.engine.test.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.operaton.bpm.engine.impl.util.ClockUtil;

public final class ClockTestUtil {

  private ClockTestUtil() {
  }

  /**
   * Increments the current time by the given seconds.
   *
   * @param seconds the seconds to add to the clock
   * @return the new current time
   */
  public static Date incrementClock(long seconds) {
    long time = ClockUtil.getCurrentTime().getTime();
    ClockUtil.setCurrentTime(new Date(time + seconds * 1000));
    return ClockUtil.getCurrentTime();
  }

  /**
   * Sets the clock to a date without milliseconds. Older mysql
   * versions do not support milliseconds. Test which test timestamp
   * should avoid timestamps with milliseconds.
   *
   * @return the new current time
   */
  public static Date setClockToDateWithoutMilliseconds() {
    ClockUtil.setCurrentTime(new GregorianCalendar(2023, Calendar.AUGUST, 18, 8, 0, 0).getTime());
    return ClockUtil.getCurrentTime();
  }
}

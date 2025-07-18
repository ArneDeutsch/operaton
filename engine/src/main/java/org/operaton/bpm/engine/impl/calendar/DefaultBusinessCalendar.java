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
package org.operaton.bpm.engine.impl.calendar;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.operaton.bpm.engine.ProcessEngineException;
import org.operaton.bpm.engine.impl.util.ClockUtil;
import org.operaton.bpm.engine.task.Task;


/**
 * @author Tom Baeyens
 */
public class DefaultBusinessCalendar implements BusinessCalendar {

  private static final Map<String, Integer> units = new HashMap<>();
  static {
    units.put("millis", Calendar.MILLISECOND);
    units.put("seconds", Calendar.SECOND);
    units.put("second", Calendar.SECOND);
    units.put("minute", Calendar.MINUTE);
    units.put("minutes", Calendar.MINUTE);
    units.put("hour", Calendar.HOUR);
    units.put("hours", Calendar.HOUR);
    units.put("day", Calendar.DAY_OF_YEAR);
    units.put("days", Calendar.DAY_OF_YEAR);
    units.put("week", Calendar.WEEK_OF_YEAR);
    units.put("weeks", Calendar.WEEK_OF_YEAR);
    units.put("month", Calendar.MONTH);
    units.put("months", Calendar.MONTH);
    units.put("year", Calendar.YEAR);
    units.put("years", Calendar.YEAR);
  }

  @Override
  public Date resolveDuedate(String duedate, Task task) {
    return resolveDuedate(duedate);
  }

  @Override
  public Date resolveDuedate(String duedate) {
    return resolveDuedate(duedate, (Date)null);
  }

  @Override
  public Date resolveDuedate(String duedate, Date startDate) {
    Date resolvedDuedate = startDate == null ? ClockUtil.getCurrentTime() : startDate;

    String[] tokens = duedate.split(" and ");
    for (String token : tokens) {
      resolvedDuedate = addSingleUnitQuantity(resolvedDuedate, token);
    }

    return resolvedDuedate;
  }

  protected Date addSingleUnitQuantity(Date startDate, String singleUnitQuantity) {
    int spaceIndex = singleUnitQuantity.indexOf(" ");
    if (spaceIndex==-1 || singleUnitQuantity.length() < spaceIndex+1) {
      throw new ProcessEngineException("invalid duedate format: "+singleUnitQuantity);
    }

    String quantityText = singleUnitQuantity.substring(0, spaceIndex);
    Integer quantity = Integer.valueOf(quantityText);

    String unitText = singleUnitQuantity
      .substring(spaceIndex+1)
      .trim()
      .toLowerCase();

    int unit = units.get(unitText);

    GregorianCalendar calendar = new GregorianCalendar();
    calendar.setTime(startDate);
    calendar.add(unit, quantity);

    return calendar.getTime();
  }
}

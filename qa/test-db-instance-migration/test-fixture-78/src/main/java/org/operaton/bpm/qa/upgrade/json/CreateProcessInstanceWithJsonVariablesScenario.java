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
package org.operaton.bpm.qa.upgrade.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.operaton.bpm.engine.ProcessEngine;
import org.operaton.bpm.engine.runtime.Execution;
import org.operaton.bpm.engine.runtime.ProcessInstance;
import org.operaton.bpm.engine.test.Deployment;
import org.operaton.bpm.engine.variable.value.builder.SerializedObjectValueBuilder;
import org.operaton.bpm.qa.upgrade.DescribesScenario;
import org.operaton.bpm.qa.upgrade.ScenarioSetup;
import org.operaton.bpm.qa.upgrade.json.beans.ObjectList;
import org.operaton.bpm.qa.upgrade.json.beans.Order;
import org.operaton.bpm.qa.upgrade.json.beans.OrderDetails;
import org.operaton.bpm.qa.upgrade.json.beans.RegularCustomer;
import static org.operaton.bpm.engine.variable.Variables.serializedObjectValue;

public class CreateProcessInstanceWithJsonVariablesScenario {

  private CreateProcessInstanceWithJsonVariablesScenario() {
  }

  @Deployment
  public static String deployProcess() {
    return "org/operaton/bpm/qa/upgrade/json/simpleProcess.bpmn20.xml";
  }

  @DescribesScenario("initProcessInstanceWithDifferentVariables")
  public static ScenarioSetup initProcessInstance() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {
        // given
        ProcessInstance processInstance = engine.getRuntimeService().startProcessInstanceByKey("Process", "processWithJsonVariables");
        // when
        Execution execution = engine.getRuntimeService().createExecutionQuery().processInstanceId(processInstance.getId()).singleResult();
        engine.getRuntimeService().setVariable(execution.getId(), "objectVariable", createObjectVariable());
        engine.getRuntimeService().setVariable(execution.getId(), "plainTypeArrayVariable", createPlainTypeArray());
        engine.getRuntimeService().setVariable(execution.getId(), "notGenericObjectListVariable", createNotGenericObjectList());
        engine.getRuntimeService().setVariable(execution.getId(), "serializedMapVariable", createSerializedMap());
      }
    };
  }

  public static Object createObjectVariable() {
    Order order = new Order();
    order.setId(1234567890987654321L);
    order.setOrder("order1");
    order.setDueUntil(20150112);
    order.setActive(true);

    OrderDetails orderDetails = new OrderDetails();
    orderDetails.setArticle("operatonBPM");
    orderDetails.setPrice(32000.45);
    orderDetails.setRoundedPrice(32000);

    List<String> currencies = new ArrayList<>();
    currencies.add("euro");
    currencies.add("dollar");
    orderDetails.setCurrencies(currencies);

    order.setOrderDetails(orderDetails);

    List<RegularCustomer> customers = new ArrayList<>();

    customers.add(new RegularCustomer("Kermit", 1354539722));
    customers.add(new RegularCustomer("Waldo", 1320325322));
    customers.add(new RegularCustomer("Johnny", 1286110922));

    order.setCustomers(customers);

    return order;
  }

  public static int[] createPlainTypeArray() {
    return new int[]{5, 10};
  }

  public static ObjectList createNotGenericObjectList() {
    ObjectList customers = new ObjectList();
    customers.add(new RegularCustomer("someCustomer", 5));
    customers.add(new RegularCustomer("secondCustomer", 666));
    return customers;
  }

  public static SerializedObjectValueBuilder createSerializedMap(){
    return serializedObjectValue("{\"foo\": \"bar\"}").serializationDataFormat("application/json").objectTypeName(HashMap.class.getName());
  }
}

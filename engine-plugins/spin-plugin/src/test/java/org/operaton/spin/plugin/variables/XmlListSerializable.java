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
package org.operaton.spin.plugin.variables;

import java.util.ArrayList;
import java.util.List;

public class XmlListSerializable<T> {

  private List<T> listProperty;

  public XmlListSerializable() {
    this.listProperty = new ArrayList<>();
  }

  public void addElement(T element) {
    this.listProperty.add(element);
  }

  public List<T> getListProperty() {
    return listProperty;
  }

  public void setListProperty(List<T> listProperty) {
    this.listProperty = listProperty;
  }

  public String toExpectedXmlString() {
    StringBuilder jsonBuilder = new StringBuilder();

    jsonBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><xmlSerializable>");
    for (int i = 0; i < listProperty.size(); i++) {
      jsonBuilder.append("<listProperty>" + listProperty.get(i) + "</listProperty>");
    }
    jsonBuilder.append("</xmlSerializable>");

    return jsonBuilder.toString();
  }

  @Override
  public String toString() {
    return toExpectedXmlString();
  }

}

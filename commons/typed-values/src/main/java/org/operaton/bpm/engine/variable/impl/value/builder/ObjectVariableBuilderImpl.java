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
package org.operaton.bpm.engine.variable.impl.value.builder;

import org.operaton.bpm.engine.variable.impl.value.ObjectValueImpl;
import org.operaton.bpm.engine.variable.value.ObjectValue;
import org.operaton.bpm.engine.variable.value.SerializationDataFormat;
import org.operaton.bpm.engine.variable.value.builder.ObjectValueBuilder;
import org.operaton.bpm.engine.variable.value.builder.TypedValueBuilder;

/**
 * @author Daniel Meyer
 *
 */
public class ObjectVariableBuilderImpl implements ObjectValueBuilder {

  protected ObjectValueImpl variableValue;

  public ObjectVariableBuilderImpl(Object value) {
    variableValue = new ObjectValueImpl(value);
  }

  public ObjectVariableBuilderImpl(ObjectValue value) {
    variableValue = (ObjectValueImpl) value;
  }

  @Override
  public ObjectValue create() {
    return variableValue;
  }

  @Override
  public ObjectValueBuilder serializationDataFormat(String dataFormatName) {
    variableValue.setSerializationDataFormat(dataFormatName);
    return this;
  }

  @Override
  public ObjectValueBuilder serializationDataFormat(SerializationDataFormat dataFormat) {
    return serializationDataFormat(dataFormat.getName());
  }

  @Override
  public TypedValueBuilder<ObjectValue> setTransient(boolean isTransient) {
    variableValue.setTransient(isTransient);
    return this;
  }

}

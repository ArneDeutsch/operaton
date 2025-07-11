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
package org.operaton.bpm.dmn.engine.impl.type;

import org.operaton.bpm.dmn.engine.impl.spi.type.DmnTypeDefinition;
import org.operaton.bpm.engine.variable.Variables;
import org.operaton.bpm.engine.variable.value.TypedValue;

/**
 * @author Philipp Ossler
 */
public class DefaultTypeDefinition implements DmnTypeDefinition {

  @Override
  public TypedValue transform(Object value) throws IllegalArgumentException {
    return Variables.untypedValue(value);
  }

  @Override
  public String getTypeName() {
    return "untyped";
  }

  @Override
  public String toString() {
    return "DefaultTypeDefinition []";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    return getClass() == obj.getClass();
  }


}

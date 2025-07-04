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

import java.util.Base64;
import org.operaton.bpm.engine.variable.Variables;
import org.operaton.bpm.engine.variable.type.ValueType;
import org.operaton.bpm.engine.variable.value.ObjectValue;
import org.operaton.bpm.engine.variable.value.TypedValue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Daniel Meyer
 *
 */
public class TypedValueAssert {

  public static void assertObjectValueDeserializedNull(ObjectValue typedValue) {
    assertNotNull(typedValue);
    assertTrue(typedValue.isDeserialized());
    assertNotNull(typedValue.getSerializationDataFormat());
    assertNull(typedValue.getValue());
    assertNull(typedValue.getValueSerialized());
    assertNull(typedValue.getObjectType());
    assertNull(typedValue.getObjectTypeName());
  }

  public static void assertObjectValueSerializedNull(ObjectValue typedValue) {
    assertNotNull(typedValue);
    assertFalse(typedValue.isDeserialized());
    assertNotNull(typedValue.getSerializationDataFormat());
    assertNull(typedValue.getValueSerialized());
    assertNull(typedValue.getObjectTypeName());
  }

  public static void assertObjectValueDeserialized(ObjectValue typedValue, Object value) {
    Class<? extends Object> expectedObjectType = value.getClass();
    assertTrue(typedValue.isDeserialized());

    assertEquals(ValueType.OBJECT, typedValue.getType());

    assertEquals(value, typedValue.getValue());
    assertEquals(value, typedValue.getValue(expectedObjectType));

    assertEquals(expectedObjectType, typedValue.getObjectType());
    assertEquals(expectedObjectType.getName(), typedValue.getObjectTypeName());
  }

  public static void assertObjectValueSerializedJava(ObjectValue typedValue, Object value) {
    assertEquals(Variables.SerializationDataFormats.JAVA.getName(), typedValue.getSerializationDataFormat());

    try {
      // validate this is the base 64 encoded string representation of the serialized value of the java object
      String valueSerialized = typedValue.getValueSerialized();
      byte[] decodedObject = Base64.getDecoder().decode(valueSerialized.getBytes(StandardCharsets.UTF_8));
      ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(decodedObject));
      assertEquals(value, objectInputStream.readObject());
    }
    catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public static void assertUntypedNullValue(TypedValue nullValue) {
    assertNotNull(nullValue);
    assertNull(nullValue.getValue());
    assertEquals(ValueType.NULL, nullValue.getType());
  }


}

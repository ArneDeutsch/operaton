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
package org.operaton.bpm.model.bpmn.impl.instance;

import org.operaton.bpm.model.bpmn.instance.Import;
import org.operaton.bpm.model.xml.ModelBuilder;
import org.operaton.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.operaton.bpm.model.xml.type.ModelElementTypeBuilder;
import org.operaton.bpm.model.xml.type.attribute.Attribute;

import static org.operaton.bpm.model.bpmn.impl.BpmnModelConstants.*;

/**
 * The BPMN import element
 *
 * @author Daniel Meyer
 * @author Sebastian Menski
 */
public class ImportImpl extends BpmnModelElementInstanceImpl implements Import {

  protected static Attribute<String> namespaceAttribute;
  protected static Attribute<String> locationAttribute;
  protected static Attribute<String> importTypeAttribute;

  public static void registerType(ModelBuilder bpmnModelBuilder) {
    ModelElementTypeBuilder typeBuilder = bpmnModelBuilder.defineType(Import.class, BPMN_ELEMENT_IMPORT)
      .namespaceUri(BPMN20_NS)
      .instanceProvider(ImportImpl::new);

    namespaceAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_NAMESPACE)
      .required()
      .build();

    locationAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_LOCATION)
      .required()
      .build();

    importTypeAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_IMPORT_TYPE)
      .required()
      .build();

    typeBuilder.build();
  }

  public ImportImpl(ModelTypeInstanceContext context) {
    super(context);
  }

  @Override
  public String getNamespace() {
    return namespaceAttribute.getValue(this);
  }

  @Override
  public void setNamespace(String namespace) {
    namespaceAttribute.setValue(this, namespace);
  }

  @Override
  public String getLocation() {
    return locationAttribute.getValue(this);
  }

  @Override
  public void setLocation(String location) {
    locationAttribute.setValue(this, location);
  }

  @Override
  public String getImportType() {
    return importTypeAttribute.getValue(this);
  }

  @Override
  public void setImportType(String importType) {
    importTypeAttribute.setValue(this, importType);
  }

}

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
package org.operaton.bpm.model.dmn.impl.instance;

import static org.operaton.bpm.model.dmn.impl.DmnModelConstants.LATEST_DMN_NS;
import static org.operaton.bpm.model.dmn.impl.DmnModelConstants.DMN_ATTRIBUTE_EXPRESSION_LANGUAGE;
import static org.operaton.bpm.model.dmn.impl.DmnModelConstants.DMN_ELEMENT_IMPORTED_VALUES;

import org.operaton.bpm.model.dmn.instance.Import;
import org.operaton.bpm.model.dmn.instance.ImportedElement;
import org.operaton.bpm.model.dmn.instance.ImportedValues;
import org.operaton.bpm.model.xml.ModelBuilder;
import org.operaton.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.operaton.bpm.model.xml.type.ModelElementTypeBuilder;
import org.operaton.bpm.model.xml.type.attribute.Attribute;
import org.operaton.bpm.model.xml.type.child.ChildElement;
import org.operaton.bpm.model.xml.type.child.SequenceBuilder;

public class ImportedValuesImpl extends ImportImpl implements ImportedValues {

  protected static Attribute<String> expressionLanguageAttribute;

  protected static ChildElement<ImportedElement> importedElementChild;

  public ImportedValuesImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  @Override
  public String getExpressionLanguage() {
    return expressionLanguageAttribute.getValue(this);
  }

  @Override
  public void setExpressionLanguage(String expressionLanguage) {
    expressionLanguageAttribute.setValue(this, expressionLanguage);
  }

  @Override
  public ImportedElement getImportedElement() {
    return importedElementChild.getChild(this);
  }

  @Override
  public void setImportedElement(ImportedElement importedElement) {
    importedElementChild.setChild(this, importedElement);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(ImportedValues.class, DMN_ELEMENT_IMPORTED_VALUES)
      .namespaceUri(LATEST_DMN_NS)
      .extendsType(Import.class)
      .instanceProvider(ImportedValuesImpl::new);

    expressionLanguageAttribute = typeBuilder.stringAttribute(DMN_ATTRIBUTE_EXPRESSION_LANGUAGE)
      .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    importedElementChild = sequenceBuilder.element(ImportedElement.class)
      .required()
      .build();

    typeBuilder.build();
  }

}

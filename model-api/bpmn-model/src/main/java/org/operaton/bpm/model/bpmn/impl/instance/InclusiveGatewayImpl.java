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

import org.operaton.bpm.model.bpmn.BpmnModelInstance;
import org.operaton.bpm.model.bpmn.builder.InclusiveGatewayBuilder;
import org.operaton.bpm.model.bpmn.instance.Gateway;
import org.operaton.bpm.model.bpmn.instance.InclusiveGateway;
import org.operaton.bpm.model.bpmn.instance.SequenceFlow;
import org.operaton.bpm.model.xml.ModelBuilder;
import org.operaton.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.operaton.bpm.model.xml.type.ModelElementTypeBuilder;
import org.operaton.bpm.model.xml.type.reference.AttributeReference;

import static org.operaton.bpm.model.bpmn.impl.BpmnModelConstants.*;

/**
 * The BPMN inclusiveGateway element
 *
 * @author Sebastian Menski
 */
public class InclusiveGatewayImpl extends GatewayImpl implements InclusiveGateway {

  protected static AttributeReference<SequenceFlow> defaultAttribute;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(InclusiveGateway.class, BPMN_ELEMENT_INCLUSIVE_GATEWAY)
      .namespaceUri(BPMN20_NS)
      .extendsType(Gateway.class)
      .instanceProvider(InclusiveGatewayImpl::new);

    defaultAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_DEFAULT)
      .idAttributeReference(SequenceFlow.class)
      .build();

    typeBuilder.build();
  }

  public InclusiveGatewayImpl(ModelTypeInstanceContext context) {
    super(context);
  }

  @Override
  public InclusiveGatewayBuilder builder() {
    return new InclusiveGatewayBuilder((BpmnModelInstance) modelInstance, this);
  }

  @Override
  public SequenceFlow getDefault() {
    return defaultAttribute.getReferenceTargetElement(this);
  }

  @Override
  public void setDefault(SequenceFlow defaultFlow) {
    defaultAttribute.setReferenceTargetElement(this, defaultFlow);
  }
}

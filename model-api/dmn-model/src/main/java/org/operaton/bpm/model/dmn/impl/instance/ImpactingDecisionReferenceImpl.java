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
import static org.operaton.bpm.model.dmn.impl.DmnModelConstants.DMN_ELEMENT_IMPACTING_DECISION;

import org.operaton.bpm.model.dmn.instance.DmnElementReference;
import org.operaton.bpm.model.dmn.instance.ImpactingDecisionReference;
import org.operaton.bpm.model.xml.ModelBuilder;
import org.operaton.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.operaton.bpm.model.xml.type.ModelElementTypeBuilder;

public class ImpactingDecisionReferenceImpl extends DmnElementReferenceImpl implements ImpactingDecisionReference {

  public ImpactingDecisionReferenceImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(ImpactingDecisionReference.class, DMN_ELEMENT_IMPACTING_DECISION)
      .namespaceUri(LATEST_DMN_NS)
      .extendsType(DmnElementReference.class)
      .instanceProvider(ImpactingDecisionReferenceImpl::new);

    typeBuilder.build();
  }

}

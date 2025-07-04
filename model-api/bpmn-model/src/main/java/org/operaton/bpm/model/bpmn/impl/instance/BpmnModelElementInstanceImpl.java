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

import org.operaton.bpm.model.bpmn.BpmnModelException;
import org.operaton.bpm.model.bpmn.builder.AbstractBaseElementBuilder;
import org.operaton.bpm.model.bpmn.instance.BpmnModelElementInstance;
import org.operaton.bpm.model.bpmn.instance.SubProcess;
import org.operaton.bpm.model.xml.impl.instance.ModelElementInstanceImpl;
import org.operaton.bpm.model.xml.impl.instance.ModelTypeInstanceContext;

/**
 * Shared base class for all BPMN Model Elements. Provides implementation
 * of the {@link BpmnModelElementInstance} interface.
 *
 * @author Daniel Meyer
 */
public abstract class BpmnModelElementInstanceImpl extends ModelElementInstanceImpl implements BpmnModelElementInstance {

  protected BpmnModelElementInstanceImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  @Override
  @SuppressWarnings("rawtypes")
  public AbstractBaseElementBuilder builder() {
    throw new BpmnModelException("No builder implemented for " + this);
  }

  @Override
  public boolean isScope() {
    return this instanceof org.operaton.bpm.model.bpmn.instance.Process || this instanceof SubProcess;
  }

  @Override
  public BpmnModelElementInstance getScope() {
    BpmnModelElementInstance parentElement = (BpmnModelElementInstance) getParentElement();
    if (parentElement != null) {
      if (parentElement.isScope()) {
        return parentElement;
      }
      else {
        return parentElement.getScope();
      }
    }
    else {
      return null;
    }
  }
}

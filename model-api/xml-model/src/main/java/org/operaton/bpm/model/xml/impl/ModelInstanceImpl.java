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
package org.operaton.bpm.model.xml.impl;

import org.operaton.bpm.model.xml.Model;
import org.operaton.bpm.model.xml.ModelBuilder;
import org.operaton.bpm.model.xml.ModelException;
import org.operaton.bpm.model.xml.ModelInstance;
import org.operaton.bpm.model.xml.impl.instance.ModelElementInstanceImpl;
import org.operaton.bpm.model.xml.impl.util.ModelUtil;
import org.operaton.bpm.model.xml.impl.validation.ModelInstanceValidator;
import org.operaton.bpm.model.xml.instance.DomDocument;
import org.operaton.bpm.model.xml.instance.DomElement;
import org.operaton.bpm.model.xml.instance.ModelElementInstance;
import org.operaton.bpm.model.xml.type.ModelElementType;
import org.operaton.bpm.model.xml.validation.ModelElementValidator;
import org.operaton.bpm.model.xml.validation.ValidationResults;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An instance of a model
 *
 * @author Daniel Meyer
 * @author Sebastian Menski
 *
 */
public class ModelInstanceImpl implements ModelInstance {

  protected final DomDocument document;
  protected ModelImpl model;
  protected final ModelBuilder modelBuilder;

  public ModelInstanceImpl(ModelImpl model, ModelBuilder modelBuilder, DomDocument document) {
    this.model = model;
    this.modelBuilder = modelBuilder;
    this.document = document;
  }

  @Override
  public DomDocument getDocument() {
    return document;
  }

  @Override
  public ModelElementInstance getDocumentElement() {
    DomElement rootElement = document.getRootElement();
    if(rootElement != null) {
      return ModelUtil.getModelElement(rootElement, this);
    } else {
      return null;
    }
  }

  @Override
  public void setDocumentElement(ModelElementInstance modelElement) {
    ModelUtil.ensureInstanceOf(modelElement, ModelElementInstanceImpl.class);
    DomElement domElement = modelElement.getDomElement();
    document.setRootElement(domElement);
  }

  @Override
  public <T extends ModelElementInstance> T newInstance(Class<T> type) {
    return newInstance(type, null);
  }

  @Override
  public <T extends ModelElementInstance> T newInstance(Class<T> type, String id) {
    ModelElementType modelElementType = model.getType(type);
    if(modelElementType != null) {
      return newInstance(modelElementType, id);
    } else {
      throw new ModelException("Cannot create instance of ModelType "+type+": no such type registered.");
    }
  }

  @Override
  public <T extends ModelElementInstance> T newInstance(ModelElementType type) {
    return newInstance(type, null);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends ModelElementInstance> T newInstance(ModelElementType type, String id) {
    ModelElementInstance modelElementInstance = type.newInstance(this);
    if (id != null && !id.isEmpty()) {
      ModelUtil.setNewIdentifier(type, modelElementInstance, id, false);
    } else {
      ModelUtil.setGeneratedUniqueIdentifier(type, modelElementInstance, false);
    }
    return (T) modelElementInstance;
  }

  @Override
  public Model getModel() {
    return model;
  }

  public ModelElementType registerGenericType(String namespaceUri, String localName) {
    ModelElementType elementType = model.getTypeForName(namespaceUri, localName);
    if (elementType == null) {
      elementType = modelBuilder.defineGenericType(localName, namespaceUri);
      model = (ModelImpl) modelBuilder.build();
    }
    return elementType;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends ModelElementInstance> T getModelElementById(String id) {
    if (id == null) {
      return null;
    }

    DomElement element = document.getElementById(id);
    if(element != null) {
      return (T) ModelUtil.getModelElement(element, this);
    } else {
      return null;
    }
  }

  @Override
  public Collection<ModelElementInstance> getModelElementsByType(ModelElementType type) {
    Collection<ModelElementType> extendingTypes = type.getAllExtendingTypes();

    List<ModelElementInstance> instances = new ArrayList<>();
    for (ModelElementType modelElementType : extendingTypes) {
      if(!modelElementType.isAbstract()) {
        instances.addAll(modelElementType.getInstances(this));
      }
    }
    return instances;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends ModelElementInstance> Collection<T> getModelElementsByType(Class<T> referencingClass) {
    return (Collection<T>) getModelElementsByType(getModel().getType(referencingClass));
  }

  @Override
  public ModelInstance clone() {
      return new ModelInstanceImpl(model, modelBuilder, document.clone());
  }

  @Override
  public ValidationResults validate(Collection<ModelElementValidator<?>> validators) {
    return new ModelInstanceValidator(this, validators).validate();
  }

}

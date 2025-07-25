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
package org.operaton.bpm.model.xml.testmodel.instance;

import org.operaton.bpm.model.xml.ModelBuilder;
import org.operaton.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.operaton.bpm.model.xml.testmodel.TestModelConstants;
import org.operaton.bpm.model.xml.type.ModelElementTypeBuilder;
import org.operaton.bpm.model.xml.type.attribute.Attribute;
import org.operaton.bpm.model.xml.type.child.ChildElement;
import org.operaton.bpm.model.xml.type.child.ChildElementCollection;
import org.operaton.bpm.model.xml.type.child.SequenceBuilder;
import org.operaton.bpm.model.xml.type.reference.ElementReference;
import org.operaton.bpm.model.xml.type.reference.ElementReferenceCollection;

import java.util.Collection;

import static org.operaton.bpm.model.xml.testmodel.TestModelConstants.ELEMENT_NAME_BIRD;
import static org.operaton.bpm.model.xml.testmodel.TestModelConstants.MODEL_NAMESPACE;

/**
 * @author Daniel Meyer
 *
 */
public class Bird extends FlyingAnimal {

  protected static ChildElementCollection<Egg> eggColl;
  protected static ElementReference<Bird, SpouseRef> spouseRefsColl;
  protected static ElementReferenceCollection<Egg, GuardEgg> guardEggRefCollection;
  protected static Attribute<Boolean> canHazExtendedWings;
  protected static ChildElement<Wings> wings;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Bird.class, ELEMENT_NAME_BIRD)
      .namespaceUri(MODEL_NAMESPACE)
      .extendsType(FlyingAnimal.class)
      .instanceProvider(Bird::new);

    SequenceBuilder sequence = typeBuilder.sequence();

    eggColl = sequence.elementCollection(Egg.class)
      .minOccurs(0)
      .maxOccurs(6)
      .build();

    spouseRefsColl = sequence.element(SpouseRef.class)
      .qNameElementReference(Bird.class)
      .build();

    guardEggRefCollection = sequence.elementCollection(GuardEgg.class)
      .idsElementReferenceCollection(Egg.class)
      .build();

    canHazExtendedWings = typeBuilder.booleanAttribute("canHazExtendedWings")
    .namespace(TestModelConstants.NEWER_NAMESPACE)
    .build();

    wings = sequence.element(Wings.class)
        .build();

    typeBuilder.build();

  }

  public Bird(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public Collection<Egg> getEggs() {
    return eggColl.get(this);
  }

  public Bird getSpouse() {
    return spouseRefsColl.getReferenceTargetElement(this);
  }

  public void setSpouse(Bird bird) {
    spouseRefsColl.setReferenceTargetElement(this, bird);
  }

  public void removeSpouse() {
    spouseRefsColl.clearReferenceTargetElement(this);
  }

  public SpouseRef getSpouseRef() {
    return spouseRefsColl.getReferenceSource(this);
  }

  public Collection<Egg> getGuardedEggs() {
    return guardEggRefCollection.getReferenceTargetElements(this);
  }

  public Collection<GuardEgg> getGuardedEggRefs() {
    return guardEggRefCollection.getReferenceSourceCollection().get(this);
  }

  public Boolean canHazExtendedWings(){
    return canHazExtendedWings.getValue(this);
  }

  public void setCanHazExtendedWings(boolean b){
    canHazExtendedWings.setValue(this, b);
  }

  public Wings getWings(){
    return wings.getChild(this);
  }

  public void setWings(Wings w){
    wings.setChild(this, w);
  }

}

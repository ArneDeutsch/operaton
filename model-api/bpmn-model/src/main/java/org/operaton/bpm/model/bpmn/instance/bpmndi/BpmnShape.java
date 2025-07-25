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
package org.operaton.bpm.model.bpmn.instance.bpmndi;

import org.operaton.bpm.model.bpmn.instance.BaseElement;
import org.operaton.bpm.model.bpmn.instance.di.LabeledShape;

/**
 * The BPMNDI BPMNShape element
 *
 * @author Sebastian Menski
 */
public interface BpmnShape extends LabeledShape {

  BaseElement getBpmnElement();

  void setBpmnElement(BaseElement bpmnElement);

  boolean isHorizontal();

  void setHorizontal(boolean isHorizontal);

  boolean isExpanded();

  void setExpanded(boolean isExpanded);

  boolean isMarkerVisible();

  void setMarkerVisible(boolean isMarkerVisible);

  boolean isMessageVisible();

  void setMessageVisible(boolean isMessageVisible);

  ParticipantBandKind getParticipantBandKind();

  void setParticipantBandKind(ParticipantBandKind participantBandKind);

  BpmnShape getChoreographyActivityShape();

  void setChoreographyActivityShape(BpmnShape choreographyActivityShape);

  BpmnLabel getBpmnLabel();

  void setBpmnLabel(BpmnLabel bpmnLabel);

}

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
package org.operaton.bpm.engine.impl.bpmn.parser;

import java.util.List;

import org.operaton.bpm.engine.impl.core.variable.mapping.IoMapping;
import org.operaton.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.operaton.bpm.engine.impl.pvm.process.ActivityImpl;
import org.operaton.bpm.engine.impl.pvm.process.ScopeImpl;
import org.operaton.bpm.engine.impl.pvm.process.TransitionImpl;
import org.operaton.bpm.engine.impl.util.xml.Element;
import org.operaton.bpm.engine.impl.variable.VariableDeclaration;

/**
 * Listener which can be registered within the engine to receive events during parsing (and
 * maybe influence it). Instead of implementing this interface you might consider to extend
 * the {@link AbstractBpmnParseListener}, which contains an empty implementation for all methods
 * and makes your implementation easier and more robust to future changes.
 *
 * @author Tom Baeyens
 * @author Falko Menge
 * @author Joram Barrez
 */
public interface BpmnParseListener {

  default void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition) {
  }
  default void parseStartEvent(Element startEventElement, ScopeImpl scope, ActivityImpl startEventActivity) {
  }
  default void parseExclusiveGateway(Element exclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
  }
  default void parseInclusiveGateway(Element inclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
  }
  default void parseParallelGateway(Element parallelGwElement, ScopeImpl scope, ActivityImpl activity) {
  }
  default void parseScriptTask(Element scriptTaskElement, ScopeImpl scope, ActivityImpl activity) {
  }
  default void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
  }
  default void parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope, ActivityImpl activity) {
  }
  default void parseTask(Element taskElement, ScopeImpl scope, ActivityImpl activity) {
  }
  default void parseManualTask(Element manualTaskElement, ScopeImpl scope, ActivityImpl activity) {
  }
  default void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
  }
  default void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity) {
  }
  default void parseBoundaryTimerEventDefinition(Element timerEventDefinition, boolean interrupting, ActivityImpl timerActivity) {
  }
  default void parseBoundaryErrorEventDefinition(Element errorEventDefinition, boolean interrupting, ActivityImpl activity, ActivityImpl nestedErrorEventActivity) {
  }
  default void parseSubProcess(Element subProcessElement, ScopeImpl scope, ActivityImpl activity) {
  }
  default void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity) {
  }
  default void parseProperty(Element propertyElement, VariableDeclaration variableDeclaration, ActivityImpl activity) {
  }
  default void parseSequenceFlow(Element sequenceFlowElement, ScopeImpl scopeElement, TransitionImpl transition) {
  }
  default void parseSendTask(Element sendTaskElement, ScopeImpl scope, ActivityImpl activity) {
  }
  default void parseMultiInstanceLoopCharacteristics(Element activityElement, Element multiInstanceLoopCharacteristicsElement, ActivityImpl activity) {
  }
  default void parseIntermediateTimerEventDefinition(Element timerEventDefinition, ActivityImpl timerActivity) {
  }
  default void parseRootElement(Element rootElement, List<ProcessDefinitionEntity> processDefinitions) {
  }
  default void parseReceiveTask(Element receiveTaskElement, ScopeImpl scope, ActivityImpl activity) {
  }
  default void parseIntermediateSignalCatchEventDefinition(Element signalEventDefinition, ActivityImpl signalActivity) {
  }
  default void parseIntermediateMessageCatchEventDefinition(Element messageEventDefinition, ActivityImpl nestedActivity) {
  }
  default void parseBoundarySignalEventDefinition(Element signalEventDefinition, boolean interrupting, ActivityImpl signalActivity) {
  }
  default void parseEventBasedGateway(Element eventBasedGwElement, ScopeImpl scope, ActivityImpl activity) {
  }
  default void parseTransaction(Element transactionElement, ScopeImpl scope, ActivityImpl activity) {
  }
  default void parseCompensateEventDefinition(Element compensateEventDefinition, ActivityImpl compensationActivity) {
  }
  default void parseIntermediateThrowEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
  }
  default void parseIntermediateCatchEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
  }
  default void parseBoundaryEvent(Element boundaryEventElement, ScopeImpl scopeElement, ActivityImpl nestedActivity) {
  }
  default void parseBoundaryMessageEventDefinition(Element element, boolean interrupting, ActivityImpl messageActivity) {
  }
  default void parseBoundaryEscalationEventDefinition(Element escalationEventDefinition, boolean interrupting, ActivityImpl boundaryEventActivity) {
  }
  default void parseBoundaryConditionalEventDefinition(Element element, boolean interrupting, ActivityImpl conditionalActivity) {
  }
  default void parseIntermediateConditionalEventDefinition(Element conditionalEventDefinition, ActivityImpl conditionalActivity) {
  }
  default void parseConditionalStartEventForEventSubprocess(Element element, ActivityImpl conditionalActivity, boolean interrupting) {
  }
  default void parseIoMapping(Element extensionElements, ActivityImpl activity, IoMapping inputOutput) {
  }
}

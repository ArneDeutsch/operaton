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
package org.operaton.bpm.engine.impl.migration.validation.instruction;

import org.operaton.bpm.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior;
import org.operaton.bpm.engine.impl.pvm.process.ActivityImpl;
import org.operaton.bpm.engine.impl.pvm.process.ScopeImpl;

/**
 * Validates that the target process definition cannot add a new inner activity to a migrating multi-instance body.
 *
 * @author Thorben Lindhauer
 */
public class CannotAddMultiInstanceInnerActivityValidator implements MigrationInstructionValidator {

  @Override
  public void validate(ValidatingMigrationInstruction instruction, ValidatingMigrationInstructions instructions,
      MigrationInstructionValidationReportImpl report) {
    ActivityImpl targetActivity = instruction.getTargetActivity();

    if (isMultiInstance(targetActivity)) {
      ActivityImpl innerActivity = getInnerActivity(targetActivity);

      if (instructions.getInstructionsByTargetScope(innerActivity).isEmpty()) {
        report.addFailure("Must map the inner activity of a multi-instance body when the body is mapped");
      }
    }
  }

  protected boolean isMultiInstance(ScopeImpl scope) {
    return scope.getActivityBehavior() instanceof MultiInstanceActivityBehavior;
  }

  protected ActivityImpl getInnerActivity(ActivityImpl multiInstanceBody) {
    MultiInstanceActivityBehavior activityBehavior = (MultiInstanceActivityBehavior) multiInstanceBody.getActivityBehavior();
    return activityBehavior.getInnerActivity(multiInstanceBody);
  }

}

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
package org.operaton.bpm.engine.impl.migration.validation.instance;

import org.operaton.bpm.engine.impl.migration.instance.MigratingActivityInstance;
import org.operaton.bpm.engine.impl.migration.instance.MigratingProcessInstance;
import org.operaton.bpm.engine.impl.migration.validation.activity.SupportedActivityValidator;
import org.operaton.bpm.engine.impl.pvm.process.ActivityImpl;
import org.operaton.bpm.engine.impl.pvm.process.ScopeImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public class SupportedActivityInstanceValidator implements MigratingActivityInstanceValidator {

  @Override
  public void validate(MigratingActivityInstance migratingInstance, MigratingProcessInstance migratingProcessInstance,
      MigratingActivityInstanceValidationReportImpl instanceReport) {

    ScopeImpl sourceScope = migratingInstance.getSourceScope();

    if (sourceScope != sourceScope.getProcessDefinition()) {
      ActivityImpl sourceActivity = (ActivityImpl) migratingInstance.getSourceScope();

      if (!SupportedActivityValidator.INSTANCE.isSupportedActivity(sourceActivity)) {
        instanceReport.addFailure("The type of the source activity is not supported for activity instance migration");
      }
    }
  }

}

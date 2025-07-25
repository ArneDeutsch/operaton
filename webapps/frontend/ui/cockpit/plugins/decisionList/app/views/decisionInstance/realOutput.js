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

'use strict';

var angular = require('operaton-commons-ui/vendor/angular');

module.exports = [
  'ViewsProvider',
  function(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.decisionInstance.table', {
      id: 'realOutput',
      initialize: function(data) {
        var outputCell, selector, realOutput;

        data.decisionInstance.outputs.map(function(output) {
          selector =
            '.output-cell[data-col-id=' +
            output.clauseId +
            '][data-row-id=' +
            output.ruleId +
            ']';
          outputCell = angular.element(selector)[0];
          realOutput = document.createElement('span');
          if (
            output.type !== 'Object' &&
            output.type !== 'Bytes' &&
            output.type !== 'File'
          ) {
            realOutput.className = 'dmn-output';
            realOutput.textContent = ' = ' + output.value;
          } else {
            realOutput.className = 'dmn-output-object';
            realOutput.setAttribute(
              'title',
              'Variable value of type ' + output.type + ' is not shown'
            );
            realOutput.textContent = ' = [' + output.type + ']';
          }
          outputCell.appendChild(realOutput);
        });
      }
    });
  }
];

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

var angular = require('operaton-commons-ui/vendor/angular');
var createCamApiMock = require('./create-cam-api-mock');
var ViewsProvider = require('./views-provider-mock');
var $routeProvider = require('./route-provider-mock');
var localConfMock = require('./local-conf-mock');

var ngModule = angular.module('common-tests-module', []);

ngModule.value('camAPI', createCamApiMock());
ngModule.provider('Views', function() {
  return ViewsProvider;
});

ngModule.provider('$route', function() {
  return $routeProvider;
});

ngModule.factory('localConf', function() {
  return localConfMock;
});

module.exports = ngModule;

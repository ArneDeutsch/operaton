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
package org.operaton.bpm.engine.impl.bpmn.behavior;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.operaton.bpm.engine.delegate.DelegateExecution;
import org.operaton.bpm.engine.delegate.Expression;
import org.operaton.bpm.engine.impl.ProcessEngineLogger;
import org.operaton.bpm.engine.impl.pvm.delegate.ActivityExecution;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ShellActivityBehavior extends AbstractBpmnActivityBehavior {

  protected static final BpmnBehaviorLogger LOG = ProcessEngineLogger.BPMN_BEHAVIOR_LOGGER;

  protected Expression command;
  protected Expression wait;
  protected Expression arg1;
  protected Expression arg2;
  protected Expression arg3;
  protected Expression arg4;
  protected Expression arg5;
  protected Expression outputVariable;
  protected Expression errorCodeVariable;
  protected Expression redirectError;
  protected Expression cleanEnv;
  protected Expression directory;

  String commandStr;
  String arg1Str;
  String arg2Str;
  String arg3Str;
  String arg4Str;
  String arg5Str;
  String waitStr;
  String resultVariableStr;
  String errorCodeVariableStr;
  Boolean waitFlag;
  Boolean redirectErrorFlag;
  Boolean cleanEnvBoolan;
  String directoryStr;

  private void readFields(ActivityExecution execution) {
    commandStr = getStringFromField(command, execution);
    arg1Str = getStringFromField(arg1, execution);
    arg2Str = getStringFromField(arg2, execution);
    arg3Str = getStringFromField(arg3, execution);
    arg4Str = getStringFromField(arg4, execution);
    arg5Str = getStringFromField(arg5, execution);
    waitStr = getStringFromField(wait, execution);
    resultVariableStr = getStringFromField(outputVariable, execution);
    errorCodeVariableStr = getStringFromField(errorCodeVariable, execution);

    String redirectErrorStr = getStringFromField(redirectError, execution);
    String cleanEnvStr = getStringFromField(cleanEnv, execution);

    waitFlag = waitStr == null || waitStr.equals("true");
    redirectErrorFlag = redirectErrorStr != null && redirectErrorStr.equals("true");
    cleanEnvBoolan = cleanEnvStr != null && cleanEnvStr.equals("true");
    directoryStr = getStringFromField(directory, execution);

  }

  @Override
  public void execute(ActivityExecution execution) {

    readFields(execution);

    List<String> argList = new ArrayList<>();
    argList.add(commandStr);

    if (arg1Str != null)
      argList.add(arg1Str);
    if (arg2Str != null)
      argList.add(arg2Str);
    if (arg3Str != null)
      argList.add(arg3Str);
    if (arg4Str != null)
      argList.add(arg4Str);
    if (arg5Str != null)
      argList.add(arg5Str);

    ProcessBuilder processBuilder = new ProcessBuilder(argList);

    try {
      processBuilder.redirectErrorStream(redirectErrorFlag);
      if (cleanEnvBoolan) {
        Map<String, String> env = processBuilder.environment();
        env.clear();
      }
      if (directoryStr != null && !directoryStr.isEmpty())
        processBuilder.directory(new File(directoryStr));

      Process process = processBuilder.start();

      if (waitFlag) {
        int errorCode = process.waitFor();

        if (resultVariableStr != null) {
          String result = convertStreamToStr(process.getInputStream());
          execution.setVariable(resultVariableStr, result);
        }

        if (errorCodeVariableStr != null) {
          execution.setVariable(errorCodeVariableStr, Integer.toString(errorCode));
        }

      }
    } catch (Exception e) {
      throw LOG.shellExecutionException(e);
    }

    leave(execution);
  }

  public static String convertStreamToStr(InputStream is) throws IOException {

    if (is != null) {
      Writer writer = new StringWriter();

      char[] buffer = new char[1024];
      try (is) {
        Reader reader = new BufferedReader(new InputStreamReader(is, UTF_8));
        int n;
        while ((n = reader.read(buffer)) != -1) {
          writer.write(buffer, 0, n);
        }
      }
      return writer.toString();
    } else {
      return "";
    }
  }

  protected String getStringFromField(Expression expression, DelegateExecution execution) {
    if (expression != null) {
      Object value = expression.getValue(execution);
      if (value != null) {
        return value.toString();
      }
    }
    return null;
  }

}

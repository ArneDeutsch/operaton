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
package org.operaton.bpm.engine.impl.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;


/**
 * @author Tom Baeyens
 */
public class LaunchTask extends Task {

  private static final String FILESEPARATOR = System.getProperty("file.separator");

  File dir;
  String script;
  String msg;
  String args;

  @Override
  public void execute() throws BuildException {
    if (dir==null) {
      throw new BuildException("dir attribute is required with the launch task");
    }
    if (script==null) {
      throw new BuildException("script attribute is required with the launch task");
    }

    String[] cmd = null;
    String executable = getExecutable();
    if (args!=null) {
      List<String> pieces = new ArrayList<>();
      pieces.add(executable);
      StringTokenizer tokenizer = new StringTokenizer("args", " ");
      while (tokenizer.hasMoreTokens()) {
        pieces.add(tokenizer.nextToken());
      }
      cmd = pieces.toArray(new String[pieces.size()]);

    } else {
      cmd = new String[]{executable};
    }

    LaunchThread.launch(this,cmd,dir,msg);
  }

  public String getExecutable() {
    String os = System.getProperty("os.name").toLowerCase();
    String dirPath = dir.getAbsolutePath();
    String base = dirPath+FILESEPARATOR+script;
    if (exists(base)) {
      return base;
    }

    if (os.contains("windows")) {
      if (exists(base+".exe")) {
        return base+".exe";
      }
      if (exists(base+".bat")) {
        return base+".bat";
      }
    }

    if ((os.contains("linux") || os.contains("mac")) && exists(base+".sh")) {
        return base+".sh";
    }

    throw new BuildException("couldn't find executable for script "+base);
  }

  public boolean exists(String path) {
    File file = new File(path);
    return (file.exists());
  }

  public void setDir(File dir) {
    this.dir = dir;
  }

  public void setScript(String script) {
    this.script = script;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public void setArgs(String args) {
    this.args = args;
  }
}

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
package org.operaton.commons.utils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author Sebastian Menski
 */
public class IoUtil {
  private static final IoUtilLogger LOG = UtilsLogger.IO_UTIL_LOGGER;
  public static final Charset ENCODING_CHARSET = StandardCharsets.UTF_8;

  protected IoUtil() {
  }

  /**
   * Returns the input stream as String.
   *
   * @param inputStream the input stream
   * @return the input stream as String.
   */
  public static String inputStreamAsString(InputStream inputStream) {
    return new String(inputStreamAsByteArray(inputStream), ENCODING_CHARSET);
  }

  /**
   * Returns the input stream as byte[].
   *
   * @param inputStream the input stream
   * @return the input stream as byte[].
   */
  public static byte[] inputStreamAsByteArray(InputStream inputStream) {
    try (var os = new ByteArrayOutputStream(); inputStream) {
      byte[] buffer = new byte[16 * 1024];
      int read;
      while ((read = inputStream.read(buffer)) > 0) {
        os.write(buffer, 0, read);
      }
      return os.toByteArray();
    } catch (IOException e) {
      throw LOG.unableToReadInputStream(e);
    }
  }

  /**
   * Returns the Reader content as String.
   *
   * @param reader the Reader
   * @return the Reader content as String
   */
  public static String readerAsString(Reader reader) {
    StringBuilder buffer = new StringBuilder();
    char[] chars = new char[16 * 1024];
    int numCharsRead;
    try {
      while ((numCharsRead = reader.read(chars, 0, chars.length)) != -1) {
        buffer.append(chars, 0, numCharsRead);
      }
      return buffer.toString();
    } catch (IOException e) {
      throw LOG.unableToReadFromReader(e);
    }
    finally {
      closeSilently(reader);
    }
  }

  /**
   * Returns the String as InputStream.
   *
   * @param string the String to convert
   * @return the InputStream containing the String
   */
  public static InputStream stringAsInputStream(String string) {
    return new ByteArrayInputStream(string.getBytes(ENCODING_CHARSET));
  }

  /**
   * Close a closable ignoring any IO exception.
   *
   * @param closeable the closable to close
   */
  public static void closeSilently(Closeable closeable) {
    try {
      if (closeable != null) {
        closeable.close();
      }
    } catch (IOException e) {
      // ignore
    }
  }

  /**
   * Returns the content of a file with specified filename
   *
   * @param filename name of the file to load
   * @return Content of the file as String
   */
  public static String fileAsString(String filename) {
    File classpathFile = getClasspathFile(filename);
    return fileAsString(classpathFile);
  }

  /**
   * Returns the content of a File.
   *
   * @param file the file to load
   * @return Content of the file as String
   */
  public static String fileAsString(File file) {
    try {
      return inputStreamAsString(new FileInputStream(file));
    } catch(FileNotFoundException e) {
      throw LOG.fileNotFoundException(file.getAbsolutePath(), e);
    }
  }

  /**
   * Returns the content of a File.
   *
   * @param file the file to load
   * @return Content of the file as String
   */
  public static byte[] fileAsByteArray(File file) {
    try {
      return inputStreamAsByteArray(new FileInputStream(file));
    } catch(FileNotFoundException e) {
      throw LOG.fileNotFoundException(file.getAbsolutePath(), e);
    }
  }


  /**
   * Returns the input stream of a file with specified filename
   *
   * @param filename the name of a File to load
   * @return the file content as input stream
   * @throws IoUtilException if the file cannot be loaded
   */
  public static InputStream fileAsStream(String filename) {
    File classpathFile = getClasspathFile(filename);
    return fileAsStream(classpathFile);
  }

  /**
   * Returns the input stream of a file.
   *
   * @param file the File to load
   * @return the file content as input stream
   * @throws IoUtilException if the file cannot be loaded
   */
  public static InputStream fileAsStream(File file) {
    try {
      return new BufferedInputStream(new FileInputStream(file));
    } catch(FileNotFoundException e) {
      throw LOG.fileNotFoundException(file.getAbsolutePath(), e);
    }
  }

  /**
   * Returns the File for a filename.
   *
   * @param filename the filename to load
   * @return the file object
   */
  public static File getClasspathFile(String filename) {
    if(filename == null) {
      throw LOG.nullParameter("filename");
    }

    return getClasspathFile(filename, null);
  }

  /**
   * Returns the File for a filename.
   *
   * @param filename the filename to load
   * @param classLoader the classLoader to load file with, if null falls back to TCCL and then this class's classloader
   * @return the file object
   * @throws IoUtilException if the file cannot be loaded
   */
  public static File getClasspathFile(String filename, ClassLoader classLoader) {
    if(filename == null) {
      throw LOG.nullParameter("filename");
    }

    URL fileUrl = null;

    if (classLoader != null) {
      fileUrl = classLoader.getResource(filename);
    }
    if (fileUrl == null) {
      // Try the current Thread context classloader
      classLoader = Thread.currentThread().getContextClassLoader();
      fileUrl = classLoader.getResource(filename);

      if (fileUrl == null) {
        // Finally, try the classloader for this class
        classLoader = IoUtil.class.getClassLoader();
        fileUrl = classLoader.getResource(filename);
      }
    }

    if(fileUrl == null) {
      throw LOG.fileNotFoundException(filename);
    }

    try {
      return new File(fileUrl.toURI());
    } catch(URISyntaxException e) {
      throw LOG.fileNotFoundException(filename, e);
    }
  }
}

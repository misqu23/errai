/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.tools.source.server;

import com.google.inject.Inject;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.server.annotations.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: May 18, 2010
 */
@Service
public class SourceViewService implements MessageCallback {

  @Inject
  RequestDispatcher dispatcher;

  public void callback(Message message) {
    String sourceClassName = message.get(String.class, "className");

    ClassLoader loader = message.getResource(ClassLoader.class, "errai.experimental.classLoader");
    if(loader==null)
      loader = Thread.currentThread().getContextClassLoader();

    String rawSource = sourceAsString(sourceClassName, loader);
    String html = rawSource != null ? JavaToHTML.format(rawSource) : "<h2>Source not available</h2>";

    MessageBuilder.createConversation(message)
        .subjectProvided()
        .with("source", html)
        .noErrorHandling().sendNowWith(dispatcher);
  }

  private String sourceAsString(String sourceClassName, ClassLoader loader) {
    String source = null;

    InputStream in = loader.getResourceAsStream(sourceClassName);
    if (in != null)
      source = convertStreamToString(in);

    return source;
  }

  public String convertStreamToString(InputStream is) {
    try {
      StringBuilder sb = new StringBuilder();
      String line;

      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        while ((line = reader.readLine()) != null) {
          sb.append(line).append("\n");
        }
      } finally {
        is.close();
      }
      return sb.toString();
    }
    catch (IOException e) {
      throw new RuntimeException("Failed to read stream", e);
    }

  }
}

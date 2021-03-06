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

package org.jboss.errai.bus.client.api.base;

import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.Message;

import static org.jboss.errai.bus.client.api.base.MessageBuilder.createConversation;

public class DefaultErrorCallback implements ErrorCallback {
    public static final DefaultErrorCallback INSTANCE = new DefaultErrorCallback();

    public boolean error(Message message, Throwable e) {
        e.printStackTrace();

        if (e != null) {
            StringBuilder a = new StringBuilder("<br/>").append(e.getClass().getName()).append(": ").append(e.getMessage()).append("<br/>");

            // Let's build-up the stacktrace.
            boolean first = true;
            for (StackTraceElement sel : e.getStackTrace()) {
                a.append(first ? "" : "&nbsp;&nbsp;").append(sel.toString()).append("<br/>");
                first = false;
            }

            // And add the entire causal chain.
            while ((e = e.getCause()) != null) {
                first = true;
                a.append("Caused by:<br/>");
                for (StackTraceElement sel : e.getStackTrace()) {
                    a.append(first ? "" : "&nbsp;&nbsp;").append(sel.toString()).append("<br/>");
                    first = false;
                }
            }

            createConversation(message)
                    .toSubject("ClientBusErrors")
                    .with("ErrorMessage", e.getMessage())
                    .with("AdditionalDetails", a.toString())
                    .noErrorHandling().reply();


        } else {
            createConversation(message)
                    .toSubject("ClientBusErrors")
                    .with("ErrorMessage", e.getMessage())
                    .with("AdditionalDetails", "No additional details")
                    .noErrorHandling().reply();
        }

        return false;
    }
}

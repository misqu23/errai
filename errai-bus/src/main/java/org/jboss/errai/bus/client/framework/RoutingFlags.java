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

package org.jboss.errai.bus.client.framework;

/**
 * Enumeration of flags that can be used when sending messages, to specify how they should be sent
 */
public enum RoutingFlags {
    NonGlobalRouting {
        @Override
        public int flag() {
            return 1;
        }},
    PriorityProcessing {
        @Override
        public int flag() {
            return 1 << 1;
        }},

    Conversational {
        @Override
        public int flag() {
            return 1 << 2;
        }},

    FromRemote {
        @Override
        public int flag() {
            return 1 << 3;
        }},

    Committed {
        @Override
        public int flag() {
            return 1 << 4;
        }},

    HasModelAdapter {
        @Override
        public int flag() {
            return 1 << 5;
        }};

    /**
     * Returns the integer representing the flag
     *
     * @return integer representation of the flag
     */
    public abstract int flag();
}

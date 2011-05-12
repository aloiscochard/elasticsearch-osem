/*
 * Licensed to Elastic Search and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Elastic Search licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.elasticsearch.osem.core;

/**
 * 
 * @author alois.cochard
 *
 */
public class ObjectContextSerializationException extends ObjectContextException {

    private static final long serialVersionUID = 1L;

    public ObjectContextSerializationException(Class<?> clazz, Throwable cause) {
        super(String.format("Unable to serialize object of type [%s]", clazz), cause);
    }
}

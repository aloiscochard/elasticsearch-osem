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

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.SearchHit;

/**
 * 
 * @author alois.cochard
 *
 */
public abstract class AbstractObjectContext implements ObjectContext {

    protected ObjectContextMapper mapper;

    protected ObjectContextWriter writer;

    protected ObjectContextReader reader;

    private Set<Class<?>> types = new CopyOnWriteArraySet<Class<?>>();

    @Override
    public XContentBuilder getMapping(Class<?> clazz) throws ObjectContextMappingException {
        check(clazz);
        return mapper.getMapping(clazz);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T read(SearchHit hit) throws ObjectContextDeserializationException {
        return (T) reader.read(hit);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T read(GetResponse response) {
        return (T) reader.read(response);
    }

    @Override
    public String getId(Object object) throws ObjectContextMappingException {
        check(object.getClass());
        return mapper.getId(object);
    }

    @Override
    public void setId(Object object, String id) throws ObjectContextMappingException {
        check(object.getClass());
        mapper.setId(object, id);
    }

    @Override
    public String getType(Class<?> clazz) {
        check(clazz);
        return mapper.getType(clazz);
    }

    @Override
    public Class<?>[] getTypes() {
        return types.toArray(new Class<?>[types.size()]);
    }

    @Override
    public XContentBuilder write(Object object) throws ObjectContextSerializationException {
        check(object.getClass());
        return writer.write(object);
    }

    @Override
    public boolean isRegistred(Class<?> clazz) {
        return types.contains(clazz);
    }

    @Override
    public ObjectContext add(Class<?> clazz) {
        types.add(clazz);
        mapper.add(clazz);
        return this;
    }

    private void check(Class<?> clazz) {
        if (!isRegistred(clazz)) {
            throw new ObjectContextTypeException(clazz);
        }
    }
}

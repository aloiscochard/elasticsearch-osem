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
package org.elasticsearch.osem.core.impl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.osem.annotations.AttributeSource;
import org.elasticsearch.osem.annotations.SerializableAttribute;
import org.elasticsearch.osem.core.ObjectContextDeserializationException;
import org.elasticsearch.osem.core.ObjectContextReader;
import org.elasticsearch.osem.property.PropertySignature;
import org.elasticsearch.osem.property.PropertySignatureSource;
import org.elasticsearch.osem.property.PropertyType;
import org.elasticsearch.search.SearchHit;

/**
 * 
 * @author alois.cochard
 *
 */
public class ObjectContextReaderImpl implements ObjectContextReader {

    private AttributeSource attributes;

    private PropertySignatureSource signatures;

    public ObjectContextReaderImpl(AttributeSource attributes, PropertySignatureSource signatures) {
        this.attributes = attributes;
        this.signatures = signatures;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T read(SearchHit hit) throws ObjectContextDeserializationException {
        Exception exception = null;
        try {
            Map<String, Object> values = hit.sourceAsMap();
            values.put("_id", hit.getId());
            T object = (T) parseObject(values, null);
            return object;
        } catch (InstantiationException e) {
            exception = e;
        } catch (IllegalAccessException e) {
            exception = e;
        } catch (IllegalArgumentException e) {
            exception = e;
        } catch (InvocationTargetException e) {
            exception = e;
        } catch (ClassNotFoundException e) {
            exception = e;
        }
        throw new ObjectContextDeserializationException(exception);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T read(GetResponse response) throws ObjectContextDeserializationException {
        Exception exception = null;
        try {
            Map<String, Object> values = response.getSource();
            values.put("_id", response.getId());
            T object = (T) parseObject(values, null);
            return object;
        } catch (InstantiationException e) {
            exception = e;
        } catch (IllegalAccessException e) {
            exception = e;
        } catch (IllegalArgumentException e) {
            exception = e;
        } catch (InvocationTargetException e) {
            exception = e;
        } catch (ClassNotFoundException e) {
            exception = e;
        }
        throw new ObjectContextDeserializationException(exception);
    }

    @SuppressWarnings("unchecked")
    private Object parseObject(Map<String, Object> source, Class<?> clazz) throws InstantiationException, IllegalAccessException,
            ClassNotFoundException, IllegalArgumentException, InvocationTargetException {
        // Instantiate object
        Object object = null;
        String className = (String) source.get(ObjectContextImpl.CLASS_FIELD_NAME);
        if (className != null && !className.isEmpty()) {
            clazz = Class.forName(className);
        }
        object = clazz.newInstance();
        // Populate properties
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            if (name.equals(ObjectContextImpl.CLASS_FIELD_NAME)) {
                continue;
            }
            PropertyDescriptor property = attributes.getProperty(clazz, name);
            // TODO [alois.cochard] Handling of serialization attribute, perhaps better to rewrite reader/writer in a more OOP way.
            SerializableAttribute serializable = attributes.getSerializableProperties(clazz).get(property);
            if (property != null) {
                PropertySignature signature = signatures.get(property);
                if (value != null) {
                    if (signature.getComposite() == null) {
                        value = signature.getType().getAdapter().read(serializable, (String) value);
                    } else {
                        switch (signature.getType()) {
                            case Array:
                            case Collection:
                                Collection<Object> input = (Collection<Object>) value;
                                Collection<Object> output = (Collection<Object>) signature.getTypeClass().newInstance();
                                for (Object o : input) {
                                    if (signature.getComposite().getType().getAdapter() != null) {
                                        output.add(signature.getComposite().getType().getAdapter().read(serializable, (String) o));
                                    } else {
                                        output.add(parseObject((Map<String, Object>) o, signature.getComposite().getTypeClass()));
                                    }
                                }
                                value = output;
                                if (signature.getType().equals(PropertyType.Array)) {
                                    value = output.toArray((Object[]) Array.newInstance(signature.getComposite().getTypeClass(), output.size()));
                                }
                                break;
                            case Object:
                                value = parseObject((Map<String, Object>) value, signature.getComposite().getTypeClass());
                                break;
                        }
                    }
                }
                property.getWriteMethod().invoke(object, value);
            }
        }

        return object;
    }
}

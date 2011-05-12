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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.osem.annotations.AttributeSource;
import org.elasticsearch.osem.annotations.IndexableAttribute;
import org.elasticsearch.osem.annotations.SerializableAttribute;
import org.elasticsearch.osem.core.ObjectContextSerializationException;
import org.elasticsearch.osem.core.ObjectContextWriter;
import org.elasticsearch.osem.property.PropertySignature;
import org.elasticsearch.osem.property.PropertySignatureSource;

/**
 * 
 * @author alois.cochard
 *
 */
public class ObjectContextWriterImpl implements ObjectContextWriter {

    private AttributeSource attributes;

    private PropertySignatureSource signatures;

    public ObjectContextWriterImpl(AttributeSource attributes, PropertySignatureSource signatures) {
        this.attributes = attributes;
        this.signatures = signatures;
    }

    @Override
    public XContentBuilder write(Object object) throws ObjectContextSerializationException {
        Exception exception = null;
        try {
            XContentBuilder builder = JsonXContent.contentBuilder();
            builder.startObject();
            write(builder, object);
            builder.endObject();
            return builder;
        } catch (IllegalArgumentException e) {
            exception = e;
        } catch (IOException e) {
            exception = e;
        } catch (IllegalAccessException e) {
            exception = e;
        } catch (InvocationTargetException e) {
            exception = e;
        }
        throw new ObjectContextSerializationException(object.getClass(), exception);
    }

    private void write(XContentBuilder builder, Object object) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
            IOException {
        for (Map.Entry<PropertyDescriptor, SerializableAttribute> entry : attributes.getSerializableProperties(object.getClass()).entrySet()) {
            PropertyDescriptor property = entry.getKey();
            // TODO [alois.cochard] Handle serialization attribute
            SerializableAttribute serializable = entry.getValue();
            IndexableAttribute indexable = attributes.getIndexableProperties(object.getClass()).get(property);
            String name = indexable != null && indexable.getIndexName() != null ? indexable.getIndexName() : property.getName();
            write(builder, name, signatures.get(property), property.getReadMethod().invoke(object));
        }
        // Add _class field
        builder.field(ObjectContextImpl.CLASS_FIELD_NAME, object.getClass().getCanonicalName());
    }

    @SuppressWarnings("unchecked")
    private void write(XContentBuilder builder, String name, PropertySignature signature, Object value) throws IllegalArgumentException, IOException,
            IllegalAccessException, InvocationTargetException {
        if (name != null) {
            if (name.equals("_id") && value == null) {
                // Filtering "_id" field with null value, for automatic id generation
                return;
            }
            builder.field(name);
        }
        if (value == null) {
            builder.nullValue();
        } else {
            switch (signature.getType()) {
                case Array:
                case Collection:
                    Object[] a = value.getClass().isArray() ? (Object[]) value : ((Collection) value).toArray();
                    builder.startArray();
                    for (Object o : a) {
                        write(builder, null, signature.getComposite(), o);
                    }
                    builder.endArray();
                    break;
                case Object:
                    builder.startObject();
                    write(builder, value);
                    builder.endObject();
                    break;
                default:
                    // TODO [alois.cochard] add access to serializable attribute
                    value = value != null ? signature.getType().getAdapter().write(null, value) : value;
                    builder.value(value);
            }
        }
    }
}

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
package org.elasticsearch.osem.property.impl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.elasticsearch.osem.property.PropertySignature;
import org.elasticsearch.osem.property.PropertySignatureSource;
import org.elasticsearch.osem.property.PropertyType;

/**
 * 
 * @author alois.cochard
 *
 */
public class PropertySignatureSourceImpl implements PropertySignatureSource {

    private ConcurrentMap<Class<?>, PropertySignature> signatures = new ConcurrentHashMap<Class<?>, PropertySignature>();

    @Override
    public PropertySignature get(PropertyDescriptor property) {
        return get(property.getReadMethod().getGenericReturnType());
    }

    private PropertySignature get(Type type) {
        if (type instanceof Class) {
            Class<?> clazz = (Class<?>) type;
            if (!clazz.isArray()) {
                PropertySignature signature = signatures.get(clazz);
                if (signature == null) {
                    signature = create(type);
                    PropertySignature s = signatures.putIfAbsent(clazz, signature);
                    signature = s != null ? s : signature;
                }
                return signature;
            }
        }
        return create(type);

    }

    // TODO [alois.cochard] map handling
    // FIXME [alois.cochard] Add ignoring of Collection/Map not parameterized
    private PropertySignature create(Type type) {
        PropertySignatureImpl signature = new PropertySignatureImpl();
        ParameterizedType parameterizedType = null;
        Class<?> clazz = null;

        if (type instanceof ParameterizedType) {
            parameterizedType = (ParameterizedType) type;
            clazz = (Class<?>) parameterizedType.getRawType();
        } else if (type instanceof Class) {
            clazz = (Class<?>) type;
        } else {
            // TODO [alois.cochard] Unmanaged type exception
        }

        if (Collection.class.isAssignableFrom(clazz)) {
            if (parameterizedType != null) {
                // Parameterized Collection
                parameterizedType.getRawType();
                // FIXME [alois.cochard] if parameterized type has not annotation, composite is null (need to auto add attribute on POJO with no annotation)
                signature.setComposite(get(parameterizedType.getActualTypeArguments()[0]));
                signature.setType(PropertyType.Collection);
                if (Set.class.isAssignableFrom(clazz)) {
                    signature.setTypeClass(HashSet.class);
                } else {
                    signature.setTypeClass(ArrayList.class);
                }
            } else {
                // TODO [alois.cochard] Collection not parameterized are not supported exception
            }
        } else if (clazz.isArray()) {
            // Array
            signature.setComposite(create(clazz.getComponentType()));
            signature.setType(PropertyType.Array);
        } else {
            PropertyType fieldType = PropertyType.get(clazz);
            signature.setTypeClass(clazz);
            signature.setType(fieldType != null ? fieldType : PropertyType.Object);
        }
        return signature;
    }

}

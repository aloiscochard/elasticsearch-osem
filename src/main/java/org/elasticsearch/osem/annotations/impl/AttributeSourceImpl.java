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
package org.elasticsearch.osem.annotations.impl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.osem.annotations.AttributeSource;
import org.elasticsearch.osem.annotations.Exclude;
import org.elasticsearch.osem.annotations.Index;
import org.elasticsearch.osem.annotations.Indexable;
import org.elasticsearch.osem.annotations.IndexableAttribute;
import org.elasticsearch.osem.annotations.Searchable;
import org.elasticsearch.osem.annotations.SearchableAttribute;
import org.elasticsearch.osem.annotations.Serializable;
import org.elasticsearch.osem.annotations.SerializableAttribute;
import org.elasticsearch.osem.common.springframework.core.annotation.AnnotationUtils;
import org.elasticsearch.osem.core.ObjectContextException;

/**
 * 
 * @author alois.cochard
 */

public class AttributeSourceImpl implements AttributeSource {

    private static final ESLogger logger = Loggers.getLogger(AttributeSourceImpl.class);

    // FIXME [alois.cochard] set all inner map to unmodifiable, and all returned map/collection to unmodifiable in all projct !

    private ConcurrentMap<Class<?>, Map<String, PropertyDescriptor>> classProperties = new ConcurrentHashMap<Class<?>, Map<String, PropertyDescriptor>>();

    private ConcurrentMap<PropertyDescriptor, SerializableAttribute> serializableAttributes = new ConcurrentHashMap<PropertyDescriptor, SerializableAttribute>();

    private ConcurrentMap<PropertyDescriptor, IndexableAttribute> indexableAttributes = new ConcurrentHashMap<PropertyDescriptor, IndexableAttribute>();

    private ConcurrentMap<PropertyDescriptor, Boolean> excludedProperties = new ConcurrentHashMap<PropertyDescriptor, Boolean>();

    //    private ConcurrentMap<Class<?>, Boolean> searchableClass = new ConcurrentHashMap<Class<?>, Boolean>();
    private ConcurrentMap<Class<?>, SearchableAttribute> searchables = new ConcurrentHashMap<Class<?>, SearchableAttribute>();

    private ConcurrentMap<Class<?>, Map<PropertyDescriptor, IndexableAttribute>> indexables = new ConcurrentHashMap<Class<?>, Map<PropertyDescriptor, IndexableAttribute>>();

    private ConcurrentMap<Class<?>, Map<PropertyDescriptor, SerializableAttribute>> serializables = new ConcurrentHashMap<Class<?>, Map<PropertyDescriptor, SerializableAttribute>>();

    private static Collection<PropertyDescriptor> getPropertyDescriptors(Class<?> clazz) {
        Collection<PropertyDescriptor> descriptors = new HashSet<PropertyDescriptor>();
        try {
            BeanInfo bean;
            bean = Introspector.getBeanInfo(clazz);
            descriptors.addAll(Arrays.asList(bean.getPropertyDescriptors()));
        } catch (IntrospectionException e) {
            throw new ObjectContextException("Unable to introspect class '" + clazz.getName() + "'", e);
        }
        for (Class<?> i : clazz.getInterfaces()) {
            descriptors.addAll(getPropertyDescriptors(i));
        }
        if (clazz.getSuperclass() != null) {
            descriptors.addAll(getPropertyDescriptors(clazz.getSuperclass()));
        }
        return descriptors;
    }

    @Override
    public Collection<PropertyDescriptor> getProperties(Class<?> clazz) {
        return getClassProperties(clazz).values();
    }

    @Override
    public PropertyDescriptor getProperty(Class<?> clazz, String indexName) {
        return getClassProperties(clazz).get(indexName);
    }

    @Override
    public SearchableAttribute getSearchableAttribute(Class<?> clazz) {
        SearchableAttribute searchableAttr = null;
        if (!searchables.containsKey(clazz)) {
            Searchable searchable = AnnotationUtils.findAnnotation(clazz, Searchable.class);
            if (searchable != null) {
                searchableAttr = AttributeBuilder.build(searchable);
            }
            SearchableAttribute s = searchables.putIfAbsent(clazz, searchableAttr);
            searchableAttr = s != null ? s : searchableAttr;
        } else {
            searchableAttr = searchables.get(clazz);
        }
        return searchableAttr;
    }

    @Override
    public Map<PropertyDescriptor, IndexableAttribute> getIndexableProperties(Class<?> clazz) {
        Map<PropertyDescriptor, IndexableAttribute> indexableProperties = indexables.get(clazz);
        if (indexableProperties == null) {
            indexableProperties = new HashMap<PropertyDescriptor, IndexableAttribute>();
            boolean searchable = isSearchable(clazz);
            for (PropertyDescriptor property : getProperties(clazz)) {
                // Filtering excluded properties
                if (!isExcluded(property)) {
                    IndexableAttribute indexable = getIndexableAttribute(property);
                    if (indexable == null) {
                        if (searchable) {
                            // Searchable class properties are implicitly Indexable 
                            indexable = new IndexableAttributeImpl();
                        } else {
                            if (getSerializableAttribute(property) != null) {
                                // Serializable properties are implicitly Stored but not Indexed
                                // TODO [alois.cochard] is it correct ? or Serializable properties must be implicitly indexed too ?
                                indexable = new IndexableAttributeImpl().setStored(true).setIndex(Index.NA);
                            }
                        }
                        if (indexable != null) {
                            IndexableAttribute i = indexableAttributes.putIfAbsent(property, indexable);
                            indexable = i != null ? i : indexable;
                        }
                    }
                    if (indexable != null) {
                        indexableProperties.put(property, indexable);
                    }
                }
            }
            // Caching
            Map<PropertyDescriptor, IndexableAttribute> i = indexables.putIfAbsent(clazz, indexableProperties);
            indexableProperties = i != null ? i : indexableProperties;
        }
        return indexableProperties;
    }

    @Override
    public Map<PropertyDescriptor, SerializableAttribute> getSerializableProperties(Class<?> clazz) {
        Map<PropertyDescriptor, SerializableAttribute> serializableProperties = serializables.get(clazz);
        if (serializableProperties == null) {
            serializableProperties = new HashMap<PropertyDescriptor, SerializableAttribute>();
            boolean searchable = isSearchable(clazz);
            for (PropertyDescriptor property : getProperties(clazz)) {
                // Filtering excluded properties
                if (!isExcluded(property)) {
                    SerializableAttribute serializable = getSerializableAttribute(property);
                    if (serializable == null) {
                        if (searchable) {
                            // Searchable class properties are implicitly Serializable 
                            serializable = new SerializableAttributeImpl();
                        } else {
                            if (getIndexableAttribute(property) != null) {
                                // Indexable properties are implicitly Serializable
                                serializable = new SerializableAttributeImpl();
                            }
                        }
                        if (serializable != null) {
                            // Caching
                            SerializableAttribute s = serializableAttributes.putIfAbsent(property, serializable);
                            serializable = s != null ? s : serializable;
                        }
                    }
                    if (serializable == null) {
                        // If property isn't Serializable nor Indexable and class isn't Searchable, add property to excluded list
                        excludedProperties.putIfAbsent(property, true);
                    } else {
                        serializableProperties.put(property, serializable);
                    }
                }
            }
            // Caching
            Map<PropertyDescriptor, SerializableAttribute> s = serializables.putIfAbsent(clazz, serializableProperties);
            serializableProperties = s != null ? s : serializableProperties;
        }
        return serializableProperties;
    }

    private <A extends Annotation> A getAnnotation(PropertyDescriptor property, Class<A> annotationType) {
        // Look for annotation on setter only
        Method setter = property.getWriteMethod();
        if (setter != null) {
            return AnnotationUtils.findAnnotation(setter, annotationType);
        }
        return null;
    }

    private Map<String, PropertyDescriptor> getClassProperties(Class<?> clazz) {
        Map<String, PropertyDescriptor> properties = classProperties.get(clazz);
        if (properties == null) {
            properties = new HashMap<String, PropertyDescriptor>();
            for (PropertyDescriptor property : getPropertyDescriptors(clazz)) {
                String name = null;
                IndexableAttribute attribute = getIndexableAttribute(property);
                if (attribute != null && attribute.getIndexName() != null) {
                    name = attribute.getIndexName();
                }
                name = name == null ? property.getName() : name;
                properties.put(name, property);
            }
            Map<String, PropertyDescriptor> p = classProperties.putIfAbsent(clazz, properties);
            properties = p != null ? p : properties;
        }
        return properties;
    }

    private IndexableAttribute getIndexableAttribute(PropertyDescriptor property) {
        IndexableAttribute indexableAttr = indexableAttributes.get(property);
        if (indexableAttr == null) {
            Indexable indexable = getAnnotation(property, Indexable.class);
            if (indexable != null) {
                indexableAttr = AttributeBuilder.build(indexable);
                IndexableAttribute i = indexableAttributes.putIfAbsent(property, AttributeBuilder.build(indexable));
                indexableAttr = i != null ? i : indexableAttr;
            }
        }
        return indexableAttr;
    }

    private SerializableAttribute getSerializableAttribute(PropertyDescriptor property) {
        SerializableAttribute serializableAttr = serializableAttributes.get(property);
        if (serializableAttr == null) {
            Serializable serializable = getAnnotation(property, Serializable.class);
            if (serializable != null) {
                serializableAttr = AttributeBuilder.build(serializable);
                SerializableAttribute s = serializableAttributes.putIfAbsent(property, serializableAttr);
                serializableAttr = s != null ? s : serializableAttr;
            }
        }
        return serializableAttr;
    }

    private boolean isExcluded(PropertyDescriptor property) {
        Boolean excluded = excludedProperties.get(property);
        if (excluded == null) {
            if (property.getWriteMethod() == null) {
                // TODO [alois.cochard] warn about ignored properties
                excluded = true;
            } else {
                excluded = getAnnotation(property, Exclude.class) != null;
                if (excluded) {
                    // Warn if other annotation present
                    List<Class<?>> annotationTypes = new ArrayList<Class<?>>();
                    if (getAnnotation(property, Serializable.class) != null) {
                        annotationTypes.add(Serializable.class);
                    }
                    if (getAnnotation(property, Indexable.class) != null) {
                        annotationTypes.add(Indexable.class);
                    }
                    for (Class<?> annotationType : annotationTypes) {
                        logger.warn("The property '{}' of class '{}' have both @Exclude and @{}, @{} will be ignored", property.getName(),
                                property.getWriteMethod().getDeclaringClass().getName(), annotationType.getSimpleName(),
                                annotationType.getSimpleName());
                    }
                }
            }
            Boolean e = excludedProperties.putIfAbsent(property, excluded);
            excluded = e != null ? e : excluded;
        }
        return excluded;
    }

    private boolean isSearchable(Class<?> clazz) {
        return getSearchableAttribute(clazz) != null;
    }
}

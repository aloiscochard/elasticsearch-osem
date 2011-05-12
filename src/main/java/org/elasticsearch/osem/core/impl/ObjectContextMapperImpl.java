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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.mapper.xcontent.ContentPath;
import org.elasticsearch.index.mapper.xcontent.RootObjectMapper;
import org.elasticsearch.index.mapper.xcontent.StringFieldMapper;
import org.elasticsearch.index.mapper.xcontent.XContentMapper;
import org.elasticsearch.index.mapper.xcontent.XContentMapper.BuilderContext;
import org.elasticsearch.osem.annotations.AttributeSource;
import org.elasticsearch.osem.annotations.IndexableAttribute;
import org.elasticsearch.osem.annotations.SearchableAttribute;
import org.elasticsearch.osem.annotations.SerializableAttribute;
import org.elasticsearch.osem.core.ObjectContextException;
import org.elasticsearch.osem.core.ObjectContextMapper;
import org.elasticsearch.osem.core.ObjectContextMappingException;
import org.elasticsearch.osem.property.PropertySignature;
import org.elasticsearch.osem.property.PropertySignatureSource;

/**
 * 
 * @author alois.cochard
 */
public class ObjectContextMapperImpl implements ObjectContextMapper {

    private static final ESLogger logger = Loggers.getLogger(ObjectContextMapperImpl.class);

    private ConcurrentMap<Class<?>, Collection<XContentMapper.Builder<?, ?>>> buildersCache = new ConcurrentHashMap<Class<?>, Collection<XContentMapper.Builder<?, ?>>>();

    private AttributeSource attributes;;

    private PropertySignatureSource signatures;

    private Set<Class<?>> types = new CopyOnWriteArraySet<Class<?>>();

    public ObjectContextMapperImpl(AttributeSource attributes, PropertySignatureSource signatures) {
        this.attributes = attributes;
        this.signatures = signatures;
    }

    @Override
    public ObjectContextMapper add(Class<?> clazz) {
        if (!types.contains(clazz)) {
            for (PropertyDescriptor property : attributes.getSerializableProperties(clazz).keySet()) {
                PropertySignature composite = signatures.get(property).getComposite();
                if (composite != null) {
                    while (composite.getTypeClass() == null) {
                        if (composite.getComposite() == null) {
                            break;
                        }
                        composite = composite.getComposite();
                    }
                    if (composite.getTypeClass() != null && !composite.getType().isPrimitive()) {
                        add(composite.getTypeClass());
                    }
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Added type [{}]", clazz);
            }
            types.add(clazz);
        }
        return this;
    }

    @Override
    public String getId(Object object) {
        PropertyDescriptor property = attributes.getProperty(object.getClass(), "_id");
        if (property != null) {
            Exception exception = null;
            try {
                return (String) property.getReadMethod().invoke(object);
            } catch (IllegalArgumentException e) {
                exception = e;
            } catch (IllegalAccessException e) {
                exception = e;
            } catch (InvocationTargetException e) {
                exception = e;
            }
            throw new ObjectContextMappingException(object.getClass(), exception);
        }
        return null;
    }

    @Override
    public void setId(Object object, String id) throws ObjectContextMappingException {
        PropertyDescriptor property = attributes.getProperty(object.getClass(), "_id");
        if (property != null) {
            Exception exception = null;
            try {
                property.getWriteMethod().invoke(object, id);
                return;
            } catch (IllegalArgumentException e) {
                exception = e;
            } catch (IllegalAccessException e) {
                exception = e;
            } catch (InvocationTargetException e) {
                exception = e;
            }
            throw new ObjectContextMappingException(object.getClass(), exception);
        }
    }

    @Override
    public XContentBuilder getMapping(Class<?> clazz) {
        try {
            // FIXME [alois.cochard] ContentBuilder: cached or not cached ? that's the question ...
            XContentBuilder builder = JsonXContent.contentBuilder();
            BuilderContext context = new BuilderContext(new ContentPath());
            builder.startObject();
            build(clazz, getType(clazz)).build(context).toXContent(builder, null);
            builder.endObject();
            return builder;
        } catch (IOException exception) {
            // TODO [alois.cochard] create specific exception ?
            throw new ObjectContextException(String.format("Unable to generate mapping for [%s]", clazz), exception);
        }
    }

    @Override
    public String getType(Class<?> clazz) {
        // TODO [alois.cochard] Don't think this need to be cached (attribute are already), remove this comment if ok
        SearchableAttribute searchable = attributes.getSearchableAttribute(clazz);
        if (searchable != null && searchable.getAlias() != null) {
            return searchable.getAlias();
        }
        return clazz.getSimpleName().toLowerCase();
    }

    @Override
    public Class<?>[] getTypes() {
        return types.toArray(new Class<?>[types.size()]);
    }

    @Override
    public boolean isRegistred(Class<?> clazz) {
        return types.contains(clazz);
    }

    private RootObjectMapper.Builder build(Class<?> clazz, String name) {
        RootObjectMapper.Builder objectBuilder = new RootObjectMapper.Builder(name);
        Collection<XContentMapper.Builder<?, ?>> builders = buildersCache.get(clazz);
        if (builders == null) {
            // FIXME [alois.cochard] if clazz is an interface generate from merging registered clazz impl + interfaces
            builders = new ArrayList<XContentMapper.Builder<?, ?>>();
            Map<PropertyDescriptor, SerializableAttribute> serializables = attributes.getSerializableProperties(clazz);
            Map<PropertyDescriptor, IndexableAttribute> indexables = attributes.getIndexableProperties(clazz);
            // Browsing serializables properties since all managed properties must be serializable (an indexable property IS serializable)
            for (Map.Entry<PropertyDescriptor, SerializableAttribute> entry : serializables.entrySet()) {
                XContentMapper.Builder<?, ?> builder = build(entry.getKey(), entry.getValue(), indexables.get(entry.getKey()));
                if (builder != null) {
                    builders.add(builder);
                }
            }
            Collection<XContentMapper.Builder<?, ?>> b = buildersCache.putIfAbsent(clazz, builders);
            builders = b != null ? b : builders;
        }

        for (XContentMapper.Builder<?, ?> builder : builders) {
            objectBuilder.add(builder);
        }

        // Adding the '_class' attribute and configure to not be include in '_all'
        StringFieldMapper.Builder builder = new StringFieldMapper.Builder("_class");
        builder.includeInAll(false);
        objectBuilder.add(builder);

        return objectBuilder;
    }

    private XContentMapper.Builder<?, ?> build(PropertyDescriptor property, SerializableAttribute serializable, IndexableAttribute indexable) {
        PropertySignature signature = signatures.get(property);
        String name = indexable.getIndexName() == null ? property.getName() : indexable.getIndexName();
        switch (signature.getType()) {
            case Array:
            case Collection:
                // FIXME [alois.cochard] problem with array of array, or collection of collection... must disable this possibility ?
                if (signature.getComposite().getType().isPrimitive()) {
                    return signature.getComposite().getType().getAdapter().build(serializable, indexable, name);
                } else {
                    return build(signature.getComposite().getTypeClass(), name);
                }
            case Object:
                return build(signature.getTypeClass(), name);
            default:
                return signature.getType().getAdapter().build(serializable, indexable, name);
        }
    }

}

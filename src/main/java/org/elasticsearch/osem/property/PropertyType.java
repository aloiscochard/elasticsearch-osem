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
package org.elasticsearch.osem.property;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.elasticsearch.common.joda.time.format.ISODateTimeFormat;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import org.elasticsearch.index.mapper.xcontent.BooleanFieldMapper;
import org.elasticsearch.index.mapper.xcontent.DateFieldMapper;
import org.elasticsearch.index.mapper.xcontent.DoubleFieldMapper;
import org.elasticsearch.index.mapper.xcontent.FloatFieldMapper;
import org.elasticsearch.index.mapper.xcontent.IntegerFieldMapper;
import org.elasticsearch.index.mapper.xcontent.LongFieldMapper;
import org.elasticsearch.index.mapper.xcontent.ShortFieldMapper;
import org.elasticsearch.index.mapper.xcontent.StringFieldMapper;
import org.elasticsearch.index.mapper.xcontent.XContentMapper.Builder;
import org.elasticsearch.osem.annotations.Index;
import org.elasticsearch.osem.annotations.IndexableAttribute;
import org.elasticsearch.osem.annotations.SerializableAttribute;
import org.elasticsearch.osem.annotations.TermVector;

/**
 * 
 * @author alois.cochard
 *
 */
public enum PropertyType {
    // TODO [alois.cochard] Use adapter generic instead of static class ?
    Array,
    //Binary(BinaryFieldMapper.Builder.class, Byte.class), // FIXME [alois.cochard] handle binary correctly !
    Boolean(Boolean.class, new BooleanTypeAdapter()),
    Collection,
    Date(java.util.Date.class, new DateTypeAdapter()),
    Double(Double.class, new DoubleTypeAdapter()),
    Float(Float.class, new FloatTypeAdapter()),
    Integer(Integer.class, new IntegerTypeAdapter()),
    Long(Long.class, new LongTypeAdapter()),
    Object,
    Short(Short.class, new ShortTypeAdapter()),
    String(String.class, new StringTypeAdapter()),
    Uri(URI.class, new UriTypeAdapter());

    public static PropertyType get(Class<?> clazz) {
        for (PropertyType type : PropertyType.values()) {
            if (type.typeClass != null && type.typeClass.equals(clazz)) {
                return type;
            }
        }
        return null;
    }

    private Class<?> typeClass;

    private PropertyTypeAdapter<?> adapter;

    private PropertyType() {
    }

    private PropertyType(Class<?> typeClass, PropertyTypeAdapter<?> adapter) {
        this.typeClass = typeClass;
        this.adapter = adapter;
    }

    @SuppressWarnings("unchecked")
    public PropertyTypeAdapter<Object> getAdapter() {
        return (PropertyTypeAdapter<java.lang.Object>) adapter;
    }

    public boolean isPrimitive() {
        return adapter != null;
    }

}

//////////////////////////////
// PROPERTY TYPE CONVERTERS //
//////////////////////////////

abstract class AbstractTypeAdapter<T> implements PropertyTypeAdapter<T> {

    @SuppressWarnings("unchecked")
    @Override
    public T read(SerializableAttribute attribute, String value) {
        return (T) value;
    }

    @Override
    public String write(SerializableAttribute attribute, T value) {
        return value.toString();
    }

    protected Field.Index getIndex(Index index) {
        switch (index) {
            case ANALYZED:
                return Field.Index.ANALYZED;
            case NOT_ANALYZED:
                return Field.Index.NOT_ANALYZED;
            case NO:
                return Field.Index.NO;
        }
        return null;
    }

    protected Store getStore(boolean store) {
        if (store) {
            return Store.YES;
        }
        return Store.NO;
    }

    protected Field.TermVector getTermVector(TermVector termVector) {
        switch (termVector) {
            case YES:
                return Field.TermVector.YES;
            case NO:
                return Field.TermVector.NO;
            case WITH_OFFSETS:
                return Field.TermVector.WITH_OFFSETS;
            case WITH_POSITIONS:
                return Field.TermVector.WITH_POSITIONS;
            case WITH_POSITIONS_OFFSETS:
                return Field.TermVector.WITH_POSITIONS_OFFSETS;
        }
        return null;
    }

}

class BooleanTypeAdapter extends AbstractTypeAdapter<Boolean> {

    @Override
    public Builder<?, ?> build(SerializableAttribute serializable, IndexableAttribute indexable, String name) {
        BooleanFieldMapper.Builder builder = new BooleanFieldMapper.Builder(name);
        if (indexable.getBoost() != null) {
            builder.boost(indexable.getBoost());
        }
        if (indexable.getIndex() != Index.NA) {
            builder.index(getIndex(indexable.getIndex()));
        }
        // FIXME [alois.cochard] Does it make sense to define index name here ? already handled by mapper...
        //if (indexable.getIndexName() != null) { builder.indexName(indexable.getIndexName()); }
        if (serializable.getNullValue() != null) {
            builder.nullValue(Boolean.valueOf(serializable.getNullValue()));
        }
        if (indexable.isOmitTf() != null) {
            builder.omitTermFreqAndPositions(indexable.isOmitTf());
        }
        if (indexable.isStored() != null) {
            builder.store(getStore(indexable.isStored()));
        }
        if (indexable.getTermVector() != TermVector.NA) {
            builder.termVector(getTermVector(indexable.getTermVector()));
        }

        return builder;
    }

}

class DateTypeAdapter extends AbstractTypeAdapter<java.util.Date> {

    @Override
    public Builder<?, ?> build(SerializableAttribute serializable, IndexableAttribute indexable, String name) {
        DateFieldMapper.Builder builder = new DateFieldMapper.Builder(name);
        if (indexable.getBoost() != null) {
            builder.boost(indexable.getBoost());
        }
        if (indexable.getIncludeInAll() != null) {
            builder.includeInAll(indexable.getIncludeInAll());
        }
        //if (indexable.getIndexName() != null) { builder.indexName(indexable.getIndexName()); }
        // TODO [alois.cochard] date format handling
        //serializable.getFormat()
        if (serializable.getNullValue() != null) {
            builder.nullValue(serializable.getNullValue());
        }
        if (indexable.getPrecisionStep() != null) {
            builder.precisionStep(indexable.getPrecisionStep());
        }
        if (indexable.isStored() != null) {
            builder.store(getStore(indexable.isStored()));
        }
        return builder;
    }

    @Override
    public java.util.Date read(SerializableAttribute attribute, String value) {
        return ISODateTimeFormat.dateTime().parseDateTime(value).toDate();
    }

    @Override
    public String write(SerializableAttribute attritute, java.util.Date value) {
        return ISODateTimeFormat.dateTime().print(value.getTime());
    }

}

class DoubleTypeAdapter extends AbstractTypeAdapter<Double> {

    @Override
    public Builder<?, ?> build(SerializableAttribute serializable, IndexableAttribute indexable, String name) {
        DoubleFieldMapper.Builder builder = new DoubleFieldMapper.Builder(name);
        if (indexable.getBoost() != null) {
            builder.boost(indexable.getBoost());
        }
        if (indexable.getIncludeInAll() != null) {
            builder.includeInAll(indexable.getIncludeInAll());
        }
        //if (indexable.getIndexName() != null) { builder.indexName(indexable.getIndexName()); }
        if (serializable.getNullValue() != null) {
            builder.nullValue(Double.parseDouble(serializable.getNullValue()));
        }
        if (indexable.getPrecisionStep() != null) {
            builder.precisionStep(indexable.getPrecisionStep());
        }
        if (indexable.isStored() != null) {
            builder.store(getStore(indexable.isStored()));
        }
        return builder;
    }

}

class FloatTypeAdapter extends AbstractTypeAdapter<Float> {

    @Override
    public Builder<?, ?> build(SerializableAttribute serializable, IndexableAttribute indexable, String name) {
        FloatFieldMapper.Builder builder = new FloatFieldMapper.Builder(name);
        if (indexable.getBoost() != null) {
            builder.boost(indexable.getBoost());
        }
        if (indexable.getIncludeInAll() != null) {
            builder.includeInAll(indexable.getIncludeInAll());
        }
        //if (indexable.getIndexName() != null) { builder.indexName(indexable.getIndexName()); }
        if (serializable.getNullValue() != null) {
            builder.nullValue(Float.parseFloat(serializable.getNullValue()));
        }
        if (indexable.getPrecisionStep() != null) {
            builder.precisionStep(indexable.getPrecisionStep());
        }
        if (indexable.isStored() != null) {
            builder.store(getStore(indexable.isStored()));
        }
        return builder;
    }

}

class IntegerTypeAdapter extends AbstractTypeAdapter<Integer> {

    @Override
    public Builder<?, ?> build(SerializableAttribute serializable, IndexableAttribute indexable, String name) {
        IntegerFieldMapper.Builder builder = new IntegerFieldMapper.Builder(name);
        if (indexable.getBoost() != null) {
            builder.boost(indexable.getBoost());
        }
        if (indexable.getIncludeInAll() != null) {
            builder.includeInAll(indexable.getIncludeInAll());
        }
        //if (indexable.getIndexName() != null) { builder.indexName(indexable.getIndexName()); }
        if (serializable.getNullValue() != null) {
            builder.nullValue(Integer.parseInt(serializable.getNullValue()));
        }
        if (indexable.getPrecisionStep() != null) {
            builder.precisionStep(indexable.getPrecisionStep());
        }
        if (indexable.isStored() != null) {
            builder.store(getStore(indexable.isStored()));
        }
        return builder;
    }

}

class LongTypeAdapter extends AbstractTypeAdapter<Long> {

    @Override
    public Builder<?, ?> build(SerializableAttribute serializable, IndexableAttribute indexable, String name) {
        LongFieldMapper.Builder builder = new LongFieldMapper.Builder(name);
        if (indexable.getBoost() != null) {
            builder.boost(indexable.getBoost());
        }
        if (indexable.getIncludeInAll() != null) {
            builder.includeInAll(indexable.getIncludeInAll());
        }
        //if (indexable.getIndexName() != null) { builder.indexName(indexable.getIndexName()); }
        if (serializable.getNullValue() != null) {
            builder.nullValue(Long.parseLong(serializable.getNullValue()));
        }
        if (indexable.getPrecisionStep() != null) {
            builder.precisionStep(indexable.getPrecisionStep());
        }
        if (indexable.isStored() != null) {
            builder.store(getStore(indexable.isStored()));
        }
        return builder;
    }

}

class ShortTypeAdapter extends AbstractTypeAdapter<Short> {

    @Override
    public Builder<?, ?> build(SerializableAttribute serializable, IndexableAttribute indexable, String name) {
        ShortFieldMapper.Builder builder = new ShortFieldMapper.Builder(name);

        if (indexable.getBoost() != null) {
            builder.boost(indexable.getBoost());
        }
        if (indexable.getIncludeInAll() != null) {
            builder.includeInAll(indexable.getIncludeInAll());
        }
        //if (indexable.getIndexName() != null) { builder.indexName(indexable.getIndexName()); }
        if (serializable.getNullValue() != null) {
            builder.nullValue(Short.parseShort(serializable.getNullValue()));
        }
        if (indexable.getPrecisionStep() != null) {
            builder.precisionStep(indexable.getPrecisionStep());
        }
        if (indexable.isStored() != null) {
            builder.store(getStore(indexable.isStored()));
        }
        return builder;
    }

}

abstract class AbstractStringTypeAdapter<T> extends AbstractTypeAdapter<T> {

    @Override
    public Builder<?, ?> build(SerializableAttribute serializable, IndexableAttribute indexable, String name) {
        StringFieldMapper.Builder builder = new StringFieldMapper.Builder(name);
        if (indexable.getBoost() != null) {
            builder.boost(indexable.getBoost());
        }
        if (indexable.getIncludeInAll() != null) {
            builder.includeInAll(indexable.getIncludeInAll());
        }
        if (indexable.getIndex() != Index.NA) {
            builder.index(getIndex(indexable.getIndex()));
        }
        // TODO [alois.cochard] Link with AnalysisService to get the named analyzer
        //if (indexable.getAnalyzerIndex() != null) { builder.indexAnalyzer(indexable.getAnalyzerIndex()); }
        if (indexable.getAnalyzer() != null) {
            builder.indexAnalyzer(new NamedAnalyzer(indexable.getAnalyzer(), null));
            builder.searchAnalyzer(new NamedAnalyzer(indexable.getAnalyzer(), null));
        }
        if (indexable.getAnalyzerIndex() != null) {
            builder.indexAnalyzer(new NamedAnalyzer(indexable.getAnalyzerIndex(), null));
        }
        if (indexable.getAnalyzerSearch() != null) {
            builder.searchAnalyzer(new NamedAnalyzer(indexable.getAnalyzerSearch(), null));
        }

        //if (indexable.getIndexName() != null) { builder.indexName(indexable.getIndexName()); }
        if (serializable.getNullValue() != null) {
            builder.nullValue(serializable.getNullValue());
        }
        if (indexable.isOmitNorms() != null) {
            builder.omitNorms(indexable.isOmitNorms());
        }
        if (indexable.isOmitTf() != null) {
            builder.omitTermFreqAndPositions(indexable.isOmitTf());
        }
        //if (indexable.getAnalyzerSearch() != null) { builder.searchAnalyzer(indexable.getAnalyzerSearch()); }
        if (indexable.isStored() != null) {
            builder.store(getStore(indexable.isStored()));
        }
        if (indexable.getTermVector() != TermVector.NA) {
            builder.termVector(getTermVector(indexable.getTermVector()));
        }
        return builder;
    }

}

class StringTypeAdapter extends AbstractStringTypeAdapter<String> {

}

class UriTypeAdapter extends AbstractStringTypeAdapter<URI> {

    @Override
    public URI read(SerializableAttribute attribute, String value) {
        try {
            return new URI(value);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
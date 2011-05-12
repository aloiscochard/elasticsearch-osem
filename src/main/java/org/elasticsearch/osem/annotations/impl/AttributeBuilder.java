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

import org.elasticsearch.osem.annotations.Indexable;
import org.elasticsearch.osem.annotations.IndexableAttribute;
import org.elasticsearch.osem.annotations.Searchable;
import org.elasticsearch.osem.annotations.SearchableAttribute;
import org.elasticsearch.osem.annotations.Serializable;
import org.elasticsearch.osem.annotations.SerializableAttribute;

/**
 * 
 * @author alois.cochard
 *
 */
public class AttributeBuilder {

    public static SearchableAttribute build(Searchable searchable) {
        SearchableAttributeImpl searchableAttr = new SearchableAttributeImpl();
        searchableAttr.setAlias(!searchable.alias().equals("") ? searchable.alias() : null);
        return searchableAttr;
    }

    public static SerializableAttribute build(Serializable serializable) {
        SerializableAttributeImpl serializableAttr = new SerializableAttributeImpl();
        serializableAttr.setFormat(!serializable.format().equals("") ? serializable.format() : null);
        serializableAttr.setNullValue(!serializable.nullValue().equals("") ? serializable.nullValue() : null);
        return serializableAttr;
    }

    public static IndexableAttribute build(Indexable indexable) {
        IndexableAttributeImpl indexableAttr = new IndexableAttributeImpl();

        indexableAttr.setAnalyzer(!indexable.analyzer().equals("") ? indexable.analyzer() : null);
        indexableAttr.setAnalyzerIndex(!indexable.analyzerIndex().equals("") ? indexable.analyzerIndex() : null);
        indexableAttr.setAnalyzerSearch(!indexable.analyzerSearch().equals("") ? indexable.analyzerSearch() : null);
        indexableAttr.setBoost(indexable.boost() != -Float.MAX_VALUE ? indexable.boost() : null);
        indexableAttr.setIndex(indexable.index());
        indexableAttr.setIndexName(!indexable.indexName().equals("") ? indexable.indexName() : null);
        indexableAttr.setPrecisionStep(indexable.precisionStep() != -Integer.MAX_VALUE ? indexable.precisionStep() : null);
        indexableAttr.setTermVector(indexable.termVector());

        // Flags
        Boolean includeInAll = null;
        switch (indexable.includeInAll()) {
            case NO:
                includeInAll = false;
                break;
            case YES:
                includeInAll = true;
                break;
        }
        indexableAttr.setIncludeInAll(includeInAll);

        Boolean omitNorms = null;
        switch (indexable.omitNorms()) {
            case NO:
                omitNorms = false;
                break;
            case YES:
                omitNorms = true;
                break;
        }
        indexableAttr.setOmitNorms(omitNorms);

        Boolean omitTf = null;
        switch (indexable.omitTf()) {
            case NO:
                omitTf = false;
                break;
            case YES:
                omitTf = true;
                break;
        }
        indexableAttr.setOmitTf(omitTf);

        Boolean store = null;
        switch (indexable.store()) {
            case NO:
                store = false;
                break;
            case YES:
                store = true;
                break;
        }
        indexableAttr.setStored(store);

        return indexableAttr;
    }
}

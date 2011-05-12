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

import org.elasticsearch.osem.annotations.Index;
import org.elasticsearch.osem.annotations.IndexableAttribute;
import org.elasticsearch.osem.annotations.TermVector;

/**
 * 
 * @author alois.cochard
 *
 */
public class IndexableAttributeImpl implements IndexableAttribute {

    private String indexName;

    private Boolean stored;

    private Index index = Index.NA;

    private TermVector termVector = TermVector.NA;

    private Float boost = 1.0F;

    private Boolean omitNorms;

    private Boolean omitTf;

    private String analyzer;

    private String analyzerIndex;

    private String analyzerSearch;

    private Boolean includeInAll;

    private Integer precisionStep;

    @Override
    public String getIndexName() {
        return indexName;
    }

    @Override
    public Boolean isStored() {
        return stored;
    }

    @Override
    public Index getIndex() {
        return index;
    }

    @Override
    public TermVector getTermVector() {
        return termVector;
    }

    @Override
    public Float getBoost() {
        return boost;
    }

    @Override
    public Boolean isOmitNorms() {
        return omitNorms;
    }

    @Override
    public Boolean isOmitTf() {
        return omitTf;
    }

    @Override
    public String getAnalyzer() {
        return analyzer;
    }

    @Override
    public String getAnalyzerIndex() {
        return analyzerIndex;
    }

    @Override
    public String getAnalyzerSearch() {
        return analyzerSearch;
    }

    @Override
    public Boolean getIncludeInAll() {
        return includeInAll;
    }

    @Override
    public Integer getPrecisionStep() {
        return precisionStep;
    }

    public Boolean getStored() {
        return stored;
    }

    public IndexableAttributeImpl setStored(Boolean stored) {
        this.stored = stored;
        return this;
    }

    public IndexableAttributeImpl setOmitNorms(Boolean omitNorms) {
        this.omitNorms = omitNorms;
        return this;
    }

    public IndexableAttributeImpl setOmitTf(Boolean omitTf) {
        this.omitTf = omitTf;
        return this;
    }

    public IndexableAttributeImpl setIndexName(String indexName) {
        this.indexName = indexName;
        return this;
    }

    public IndexableAttributeImpl setIndex(Index index) {
        this.index = index;
        return this;
    }

    public IndexableAttributeImpl setTermVector(TermVector termVector) {
        this.termVector = termVector;
        return this;
    }

    public IndexableAttributeImpl setBoost(Float boost) {
        this.boost = boost;
        return this;
    }

    public IndexableAttributeImpl setAnalyzer(String analyzer) {
        this.analyzer = analyzer;
        return this;
    }

    public IndexableAttributeImpl setAnalyzerIndex(String analyzerIndex) {
        this.analyzerIndex = analyzerIndex;
        return this;
    }

    public IndexableAttributeImpl setAnalyzerSearch(String analyzerSearch) {
        this.analyzerSearch = analyzerSearch;
        return this;
    }

    public IndexableAttributeImpl setIncludeInAll(Boolean includeInAll) {
        this.includeInAll = includeInAll;
        return this;
    }

    public IndexableAttributeImpl setPrecisionStep(Integer precisionStep) {
        this.precisionStep = precisionStep;
        return this;
    }
}

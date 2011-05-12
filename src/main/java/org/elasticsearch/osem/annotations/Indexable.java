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
package org.elasticsearch.osem.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author alois.cochard
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.FIELD, ElementType.METHOD })
@Documented
public @interface Indexable {

    /**
     * The name of the field that will be stored in the index. Defaults to the property/field name. 
     */
    String indexName() default "";

    /**
     * Set to TRUE the store actual field in the index, FALSE to not store it.
     * Defaults to let ElasticSearch derive it.
     * (note, the JSON document itself is stored, and it can be retrieved from it).
     */
    Store store() default Store.NA;

    /**
     * Set to ANALYZED for the field to be indexed and searchable after being broken down into token using an analyzer.
     * NOT_ANALYZED means that its still searchable, but does not go through any analysis process or broken down into tokens.
     * NO means that it won’t be searchable at all. Defaults to let ElasticSearch derive it.
     */
    Index index() default Index.NA;

    /**
     * Possible values are NO, YES, WITH_OFFSETS, WITH_POSITIONS, WITH_POSITIONS_OFFSETS. Defaults to let ElasticSearch derive it.
     */
    TermVector termVector() default TermVector.NA;

    /**
     * The boost value. Defaults to 1.0.
     */
    float boost() default -Float.MAX_VALUE; // -Double.MAX_VALUE used as workaround to support NA/NULL

    /**
     * Boolean value if norms should be omitted or not. Defaults to let ElasticSearch derive it.
     */
    OmitNorms omitNorms() default OmitNorms.NA;

    /**
     * Boolean value if term freq and positions should be omitted. Defaults to let ElasticSearch derive it.
     */
    OmitTf omitTf() default OmitTf.NA;

    /**
     * The analyzer used to analyze the text contents when analyzed during indexing and when searching using a query string.
     * Defaults to the globally configured analyzer.
     */
    String analyzer() default "";

    /**
     * The analyzer used to analyze the text contents when analyzed during indexing.
     */
    String analyzerIndex() default "";

    /**
     * The analyzer used to analyze the field when part of a query string.
     */
    String analyzerSearch() default "";

    /**
     * Should the field be included in the _all field (if enabled).
     * Defaults to let ElasticSearch derive it.
     */
    IncludeInAll includeInAll() default IncludeInAll.NA;

    /**
     * The precision step (number of terms generated for each number value). Defaults to Let ElasticSearch derive it (AUTOMATIC).
     * AUTOMATIC mode take the best value depending of data type (long, double, date, ...).<br>
     * <i>Used on field of type NUMBER or DATE.</i>
     */
    int precisionStep() default -Integer.MAX_VALUE; // -Integer.MAX_VALUE used as workaround to support NA/NULL
}

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


import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.Map;

import org.elasticsearch.osem.annotations.AttributeSource;
import org.elasticsearch.osem.annotations.IndexableAttribute;
import org.elasticsearch.osem.annotations.SerializableAttribute;
import org.elasticsearch.osem.pojo.twitter.Tweet;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * 
 * @author alois.cochard
 *
 */
public class AttributeSourceImplTest {

    private static String[] properties = new String[] { "message", "user", "date" };

    private AttributeSource source = new AttributeSourceImpl();

    @Test
    public void testGetProperties() {
        Collection<PropertyDescriptor> c = source.getProperties(Tweet.class);
        AssertJUnit.assertNotNull(c);
    }

    @Test
    public void testGetIndexableProperties() {
        Map<PropertyDescriptor, IndexableAttribute> m = source.getIndexableProperties(Tweet.class);
        AssertJUnit.assertNotNull(m);
        AssertJUnit.assertEquals(properties.length, m.size());
        AssertJUnit.assertNotNull(source.getProperty(Tweet.class, "post_date"));
    }

    @Test
    public void testGetSerializableProperties() {
        Map<PropertyDescriptor, SerializableAttribute> m = source.getSerializableProperties(Tweet.class);
        AssertJUnit.assertNotNull(m);
        AssertJUnit.assertEquals(properties.length, m.size());
    }

}

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

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.AssertJUnit;

import java.io.IOException;

import org.elasticsearch.osem.core.ObjectContextMapper;
import org.elasticsearch.osem.core.ObjectContextMappingException;
import org.elasticsearch.osem.pojo.users.Contact;
import org.elasticsearch.osem.pojo.users.User;

/**
 * 
 * @author alois.cochard
 */
public class ObjectContextMapperImplTest extends AbstractObjectContextTest {

    private ObjectContextMapper mapper;

    @BeforeMethod()
	public void setUp() {
        mapper = new ObjectContextMapperImpl(createAttributes(), createSignatures());
    }

    @Test
    public void testAdd() {
        mapper.add(User.class);
    }

    @Test
    public void testGetMapping() throws ObjectContextMappingException, IOException {
        mapper.add(User.class);
        // TODO [acochard] improve polymorphism handling
        String expected = "{\"user\":{\"properties\":{\"_class\":{\"type\":\"string\",\"include_in_all\":false},\"_id\":{\"type\":\"string\"},\"contacts\":{\"properties\":{\"_class\":{\"type\":\"string\",\"include_in_all\":false}}},\"name\":{\"type\":\"string\"}}}}";
        AssertJUnit.assertEquals(expected, new String(mapper.getMapping(User.class).copiedBytes()));
    }

    @Test
    public void testIsRegistred() {
        mapper.add(User.class);
        AssertJUnit.assertTrue(mapper.isRegistred(User.class));
        AssertJUnit.assertTrue(mapper.isRegistred(Contact.class));
    }
}

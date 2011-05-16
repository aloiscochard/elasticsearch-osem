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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.elasticsearch.osem.core.ObjectContext;
import org.elasticsearch.osem.core.ObjectContextSerializationException;
import org.elasticsearch.osem.pojo.twitter.Tweet;
import org.elasticsearch.osem.pojo.users.Contact;
import org.elasticsearch.osem.pojo.users.EmailContact;
import org.elasticsearch.osem.pojo.users.PhoneContact;
import org.elasticsearch.osem.pojo.users.User;
import org.elasticsearch.search.internal.InternalSearchHit;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * 
 * @author alois.cochard
 *
 */
public class ObjectContextImplTest {

    private ObjectContext context;

    @BeforeMethod()
    public void setUp() {
        context = new ObjectContextImpl();
    }

    @Test
    public void testSimple() throws ObjectContextSerializationException, IOException {
        Tweet tweet = new Tweet();
        tweet.setUser("aloiscochard");
        tweet.setMessage("#ElasticSearch: You know, for search !");
        tweet.setDate(new Date());

        context.add(Tweet.class);

        String mapping = "{\"tweet\":{\"properties\":{\"_class\":{\"type\":\"string\",\"include_in_all\":false},\"message\":{\"type\":\"string\"},\"post_date\":{\"type\":\"date\",\"format\":\"dateOptionalTime\"},\"user\":{\"type\":\"string\"}}}}";

        AssertJUnit.assertEquals(mapping, new String(context.getMapping(Tweet.class).copiedBytes()));

        String json = new String(context.write(tweet).copiedBytes());
        Tweet t = context.read(new InternalSearchHit(1, "1", "tweet", json.getBytes(), null));
        AssertJUnit.assertEquals(tweet.getUser(), t.getUser());
        AssertJUnit.assertEquals(tweet.getMessage(), t.getMessage());
        AssertJUnit.assertEquals(tweet.getDate(), t.getDate());
    }

    @Test
    public void testPolymorphism() throws ObjectContextSerializationException, IOException {
        Collection<Contact> contacts = new ArrayList<Contact>();

        EmailContact email = new EmailContact();
        email.setEmail("user@domain.ext");
        contacts.add(email);

        PhoneContact phone = new PhoneContact();
        phone.setNumber("++XX XXX XX XX");
        contacts.add(phone);

        User user = new User();
        user.setContacts(contacts);

        context.add(User.class);
        String json = new String(context.write(user).copiedBytes());
        User u = context.read(new InternalSearchHit(1, "1", "user", json.getBytes(), null));

    }
}

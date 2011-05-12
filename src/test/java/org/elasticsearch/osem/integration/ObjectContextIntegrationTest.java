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
package org.elasticsearch.osem.integration;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.Assert;
import org.testng.AssertJUnit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.jackson.JsonFactory;
import org.elasticsearch.common.jackson.JsonParseException;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.xcontent.json.JsonXContentParser;
import org.elasticsearch.index.query.xcontent.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.osem.core.ObjectContext;
import org.elasticsearch.osem.core.ObjectContextDeserializationException;
import org.elasticsearch.osem.core.impl.ObjectContextImpl;
import org.elasticsearch.osem.pojo.twitter.Tweet;
import org.elasticsearch.osem.pojo.users.Contact;
import org.elasticsearch.osem.pojo.users.EmailContact;
import org.elasticsearch.osem.pojo.users.PhoneContact;
import org.elasticsearch.osem.pojo.users.User;
import org.elasticsearch.search.SearchHit;

/**
 * 
 * @author alois.cochard
 *
 */
public class ObjectContextIntegrationTest {

    private Node node;

    private ObjectContext context;

    @BeforeMethod()
	public void doStart() {
        context = new ObjectContextImpl();
        // Starting node
        ImmutableSettings.Builder settings = NodeBuilder.nodeBuilder().settings();
        settings.put("gateway.type", "none");
        settings.put("path.data", "build/tmp/data");
        // Manual refresh
        settings.put("index.refreshInterval", "-1");
        node = NodeBuilder.nodeBuilder().settings(settings).build();
        node.start();
    }

    @AfterMethod()
	public void doStop() {
        node.stop();
    }

    @Test
    public void pojoTest() throws ObjectContextDeserializationException, JsonParseException, IOException {
        Tweet tweet = new Tweet();
        tweet.setUser("aloiscochard");
        tweet.setMessage("#ElasticSearch: You know, for search !");
        tweet.setDate(new Date());

        context.add(Tweet.class);

        // Create
        node.client().admin().indices().prepareCreate("twitter").execute().actionGet();
        // Mapping
        node.client().admin().indices().preparePutMapping("twitter").setSource(context.getMapping(Tweet.class)).execute().actionGet();
        // Add
        node.client().prepareIndex("twitter", "tweet", "1").setSource(context.write(tweet)).execute().actionGet();
        // Refresh
        node.client().admin().indices().prepareRefresh("twitter").execute().actionGet();
        // Search
        SearchResponse searchResponse = node.client().prepareSearch("twitter").setSearchType(SearchType.DFS_QUERY_AND_FETCH).setQuery(
                QueryBuilders.termQuery("user", "aloiscochard")).setExplain(true).execute().actionGet();
        for (SearchHit hit : searchResponse.getHits()) {
            Tweet t = context.read(Tweet.class, hit);
            AssertJUnit.assertNotNull(t);
            AssertJUnit.assertEquals(tweet.getUser(), t.getUser());
            AssertJUnit.assertEquals(tweet.getMessage(), t.getMessage());
            AssertJUnit.assertEquals(tweet.getDate(), t.getDate());
            return;
        }
        Assert.fail();
    }
    
    @Test
    public void pojoWithIdTest() throws ObjectContextDeserializationException, JsonParseException, IOException {
        Collection<Contact> contacts = new ArrayList<Contact>();

        EmailContact email = new EmailContact();
        email.setEmail("user@domain.ext");
        contacts.add(email);

        PhoneContact phone = new PhoneContact();
        phone.setNumber("++XX XXX XX XX");
        contacts.add(phone);

        User user = new User();
        user.setContacts(contacts);
        user.setName("aloiscochard");
        
        context.add(User.class);

        // Create
        node.client().admin().indices().prepareCreate("users").execute().actionGet();
        // Mapping
        node.client().admin().indices().preparePutMapping("users").setSource(context.getMapping(User.class)).execute().actionGet();
        // Add
        node.client().prepareIndex("users", "user").setSource(context.write(user)).execute().actionGet();
        // Refresh
        node.client().admin().indices().prepareRefresh("users").execute().actionGet();
        // Search
        SearchResponse searchResponse = node.client().prepareSearch("users").setSearchType(SearchType.DFS_QUERY_AND_FETCH).setQuery(
                QueryBuilders.termQuery("name", "aloiscochard")).setExplain(true).execute().actionGet();
        for (SearchHit hit : searchResponse.getHits()) {
            User u = context.read(User.class, hit);
            AssertJUnit.assertNotNull(u);
            Assert.assertNotNull(u.getId());
            Assert.assertFalse(u.getId().isEmpty());
            Assert.assertEquals(user.getName(), u.getName());
            Assert.assertNotNull(u.getContacts());
            // TODO [alois.cochard] check inner objects
            return;
        }
        Assert.fail();
    }
}

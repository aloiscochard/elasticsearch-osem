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
package org.elasticsearch.osem.pojo.users;

import java.util.Collection;

import org.elasticsearch.osem.annotations.Indexable;
import org.elasticsearch.osem.annotations.Searchable;
import org.elasticsearch.osem.annotations.Store;

/**
 * 
 * @author alois.cochard
 *
 */
@Searchable
public class User {
	
	private String id;

    private Collection<Contact> contacts;
    
    private String name;
    
    public String getId() {
		return id;
	}
    
    @Indexable(indexName = "_id")
    public void setId(String id) {
    	this.id = id;
    }

    public Collection<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(Collection<Contact> contacts) {
        this.contacts = contacts;
    }

    public void setName(String name) {
		this.name = name;
	}
    public String getName() {
		return name;
	}
}

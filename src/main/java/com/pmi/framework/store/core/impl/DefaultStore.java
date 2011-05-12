package com.pmi.framework.store.core.impl;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import org.elasticsearch.client.Client;
import org.elasticsearch.osem.core.ObjectContext;
import org.elasticsearch.osem.core.impl.ObjectContextImpl;

import com.pmi.framework.store.core.Store;
import com.pmi.framework.store.core.StoreSession;

public class DefaultStore implements Store {

    private ObjectContext context = new ObjectContextImpl();

    private Client client;

    private String index;

    private Collection<Class<?>> types = new CopyOnWriteArrayList<Class<?>>();

    public DefaultStore(Client client, String index) {
        this.client = client;
        this.index = index;
    }

    @Override
    public Store add(Class<?> clazz) {
        types.add(clazz);
        context.add(clazz);
        return this;
    }

    @Override
    public StoreSession open() {
        // Return new session
        return new DefaultStoreSession(context, client, index);
    }

}
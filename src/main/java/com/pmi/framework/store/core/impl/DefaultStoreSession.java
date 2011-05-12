package com.pmi.framework.store.core.impl;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.xcontent.XContentQueryBuilder;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.osem.core.ObjectContext;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.internal.InternalSearchHit;
import org.elasticsearch.transport.RemoteTransportException;

import com.pmi.framework.store.core.IdentifierNotFoundException;
import com.pmi.framework.store.core.StoreException;
import com.pmi.framework.store.core.StoreSession;

public class DefaultStoreSession implements StoreSession {

    private ObjectContext context;

    private Client client;

    private String index;

    public DefaultStoreSession(ObjectContext context, Client client, String index) {
        this.context = context;
        this.client = client;
        this.index = index;
        open();
    }

    @Override
    public void close() {
        // NOOP
    }

    @Override
    public void delete(Object object) throws StoreException {
        String id = context.getId(object);
        if (id == null) {
            throw new IdentifierNotFoundException(object);
        }
        client.prepareDelete(index, context.getType(object.getClass()), id).execute().actionGet();
    }

    @Override
    public void delete(Class<?> clazz, String id) throws StoreException {
        client.prepareDelete(index, context.getType(clazz), id).execute().actionGet();
    }

    @Override
    public void deleteAll() throws StoreException {
        client.admin().indices().prepareDelete(index).execute().actionGet();
        open();
    }

    @Override
    public void deleteByQuery(XContentQueryBuilder builder) throws StoreException {
        client.prepareDeleteByQuery(index).setQuery(builder).execute();
    }

    @Override
    public SearchResponse find(XContentQueryBuilder builder) throws StoreException {
        return client.prepareSearch(index).setQuery(builder).execute().actionGet();
    }

    @Override
    public SearchResponse find(XContentQueryBuilder builder, int from, int size) throws StoreException {
        return client.prepareSearch(index).setQuery(builder).setFrom(from).setSize(size).execute().actionGet();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(SearchHit hit) throws StoreException {
        return (T) context.read(hit);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<T> clazz, String id) throws StoreException {
        String source = client.prepareGet(index, context.getType(clazz), id).execute().actionGet().sourceAsString();
        return (T) get(new InternalSearchHit(1, id, context.getType(clazz), source.getBytes(), null));
    }

    @Override
    public void save(Object object) throws StoreException {
        String id = context.getId(object);
        IndexResponse response = client.prepareIndex(index, context.getType(object.getClass()), id)
                .setSource(context.write(object))
                .execute()
                .actionGet();
        context.setId(object, response.getId());
    }

    private void open() {
        // Create and configure index
        boolean create = false;
        try {
            client.admin().indices().prepareOpen(index).execute().actionGet();
        } catch (RemoteTransportException exception) {
            if (exception.getCause().getClass().equals(IndexMissingException.class)) {
                create = true;
            }

        } catch (IndexMissingException exception) {
            create = true;
        }
        if (create) {
            client.admin().indices().prepareCreate(index).execute().actionGet();
            for (Class<?> clazz : context.getTypes()) {
                client.admin()
                        .indices()
                        .preparePutMapping(index)
                        .setType(context.getType(clazz))
                        .setSource(context.getMapping(clazz))
                        .execute()
                        .actionGet();
            }
        }
    }
}

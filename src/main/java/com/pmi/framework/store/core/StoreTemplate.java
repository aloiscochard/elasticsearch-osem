package com.pmi.framework.store.core;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.xcontent.XContentQueryBuilder;
import org.elasticsearch.search.SearchHit;

public class StoreTemplate implements StoreOperations {

    private Store store;

    public StoreTemplate() {
    }

    public StoreTemplate(Store store) {
        this.store = store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public <T> T execute(StoreCallback<T> action) throws StoreException {
        StoreSession session = store.open();
        T result = action.execute(session);
        session.close();
        return result;
    }

    @Override
    public void delete(final Object object) throws StoreException {
        execute(new StoreCallback<Object>() {

            @Override
            public Object execute(StoreSession session) {
                session.delete(object);
                return null;
            }
        });
    }

    @Override
    public void delete(final Class<?> clazz, final String id) throws StoreException {
        execute(new StoreCallback<Object>() {

            @Override
            public Object execute(StoreSession session) {
                session.delete(clazz, id);
                return null;
            }
        });
    }

    @Override
    public void deleteAll() throws StoreException {
        execute(new StoreCallback<Object>() {

            @Override
            public Object execute(StoreSession session) {
                session.deleteAll();
                return null;
            }
        });
    }

    @Override
    public void deleteByQuery(final XContentQueryBuilder builder) throws StoreException {
        execute(new StoreCallback<Object>() {

            @Override
            public Object execute(StoreSession session) {
                session.deleteByQuery(builder);
                return null;
            }
        });
    }

    @Override
    public SearchResponse find(final XContentQueryBuilder builder) throws StoreException {
        return execute(new StoreCallback<SearchResponse>() {

            @Override
            public SearchResponse execute(StoreSession session) {
                return session.find(builder);
            }
        });
    }

    @Override
    public SearchResponse find(final XContentQueryBuilder builder, final int from, final int size) throws StoreException {
        return execute(new StoreCallback<SearchResponse>() {

            @Override
            public SearchResponse execute(StoreSession session) {
                return session.find(builder, from, size);
            }
        });
    }

    @Override
    public <T> T get(final SearchHit hit) throws StoreException {
        return execute(new StoreCallback<T>() {

            @SuppressWarnings("unchecked")
            @Override
            public T execute(StoreSession session) {
                return (T) session.get(hit);
            }
        });
    }

    @Override
    public <T> T get(final Class<T> clazz, final String id) throws StoreException {
        return execute(new StoreCallback<T>() {

            @Override
            public T execute(StoreSession session) {
                return session.get(clazz, id);
            }
        });
    }

    @Override
    public void save(final Object object) throws StoreException {
        execute(new StoreCallback<Object>() {

            @Override
            public Object execute(StoreSession session) {
                session.save(object);
                return null;
            }
        });
    }

}

package com.pmi.framework.store.core;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.xcontent.XContentQueryBuilder;
import org.elasticsearch.search.SearchHit;

public interface StoreOperations {

    void delete(Object object) throws StoreException;

    void delete(Class<?> clazz, String id) throws StoreException;

    void deleteAll() throws StoreException;

    void deleteByQuery(XContentQueryBuilder builder) throws StoreException;

    SearchResponse find(XContentQueryBuilder builder) throws StoreException;

    SearchResponse find(XContentQueryBuilder builder, int from, int size) throws StoreException;

    <T> T get(SearchHit hit) throws StoreException;

    <T> T get(Class<T> clazz, String id) throws StoreException;

    void save(Object object) throws StoreException;
}

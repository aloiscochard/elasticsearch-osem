package com.pmi.framework.store.core;

public interface StoreCallback<T> {

    T execute(StoreSession session);

}

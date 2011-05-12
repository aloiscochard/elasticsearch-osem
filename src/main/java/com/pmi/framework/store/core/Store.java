package com.pmi.framework.store.core;


public interface Store {

    Store add(Class<?> clazz);

    StoreSession open();

}

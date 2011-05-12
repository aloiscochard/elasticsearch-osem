package com.pmi.framework.store.core;

public class StoreException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public StoreException(String string, Throwable root) {
        super(string, root);
    }

    public StoreException(String s) {
        super(s);
    }
}

package com.pmi.framework.store.core;

public class IdentifierNotFoundException extends StoreException {

    private static final long serialVersionUID = -4368265997043093221L;

    public IdentifierNotFoundException(Object object) {
        super(String.format("ID was not found on object <%s> of type %s", object, object.getClass()));
    }
}

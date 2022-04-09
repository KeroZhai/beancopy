package com.keroz.morphling.contract;

public interface Mappable {

    default <T> T mapTo(Class<T> clazz) {
        return null;
    }

}

package com.keroz.morphling.converter;

public interface TypeConverter {

    boolean isSupported(Class<?> sourceType, Class<?> targetType);

    Object convert(Object source, Class<?> targetType);

}

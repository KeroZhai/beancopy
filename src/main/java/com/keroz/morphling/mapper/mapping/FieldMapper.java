package com.keroz.morphling.mapper.mapping;

public interface FieldMapper {

    boolean isSupported(Class<?> sourceType, Class<?> targetType);

    Object map(Object source, Class<?> targetType);

}

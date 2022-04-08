package com.keroz.morphling.mapper.mapping;

import java.util.Date;

public class SimpleTypeFieldMapper implements FieldMapper {

    @Override
    public boolean isSupported(Class<?> sourceType, Class<?> targetType) {
        return isSimpleType(sourceType) && isSimpleType(targetType);
    }

    @Override
    public Object map(Object source, Class<?> targetType) {
        return source;
    }

    private boolean isSimpleType(Class<?> clazz) {
        return clazz.isPrimitive() || clazz.isAssignableFrom(Boolean.class)
                || clazz.isAssignableFrom(Character.class)
                || clazz.isAssignableFrom(Byte.class) || clazz.isAssignableFrom(Short.class)
                || clazz.isAssignableFrom(Integer.class)
                || clazz.isAssignableFrom(Long.class) || clazz.isAssignableFrom(Float.class)
                || clazz.isAssignableFrom(Double.class) || clazz.isAssignableFrom(String.class)
                || clazz.isAssignableFrom(Date.class) || clazz.isAssignableFrom(Void.class) || clazz.isEnum();
    }
}

package com.keroz.morphling.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ReflectionUtils {

    public Field findDeclaredField(Class<?> declaringClass, String fieldName) {
        try {
            return declaringClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Method findDeclaredMethod(Class<?> declaringClass, String methodName, Class<?>... parameterTypes) {
        try {
            return declaringClass.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Class<?> toClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        }

        return toClass(((ParameterizedType) type).getRawType());
    }

    public boolean isPrimitiveOrWrapperType(Type type) {
        return toClass(type).isPrimitive() || Boolean.class.equals(type) || Byte.class.equals(type)
                || Character.class.equals(type) || Short.class.equals(type) || Integer.class.equals(type)
                || Long.class.equals(type) || Float.class.equals(type) || Double.class.equals(type);
    }

    public boolean isSimpleType(Type type) {
        if (type instanceof Class) {
            Class<?> classType = (Class<?>) type;
            return isPrimitiveOrWrapperType(classType) || classType.isEnum()
                    || String.class.equals(classType) || Date.class.equals(classType);
        } else {
            return false;
        }
    }

    public boolean isCollectionType(Type type) {
        return Collection.class.isAssignableFrom(toClass(type));
    }

    public boolean isMapType(Type type) {
        return Map.class.isAssignableFrom(toClass(type));
    }

    public Class<?> toWrapper(Class<?> primitiveType) {
        if (!primitiveType.isPrimitive())
            return primitiveType;

        if (primitiveType == Integer.TYPE)
            return Integer.class;
        if (primitiveType == Long.TYPE)
            return Long.class;
        if (primitiveType == Boolean.TYPE)
            return Boolean.class;
        if (primitiveType == Byte.TYPE)
            return Byte.class;
        if (primitiveType == Character.TYPE)
            return Character.class;
        if (primitiveType == Float.TYPE)
            return Float.class;
        if (primitiveType == Double.TYPE)
            return Double.class;
        if (primitiveType == Short.TYPE)
            return Short.class;
        if (primitiveType == Void.TYPE)
            return Void.class;

        return primitiveType;
    }

    public boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        } else if (value instanceof Number) {
            return ((Number) value).intValue() == 0;
        } else if (value instanceof CharSequence) {
            return ((CharSequence) value).length() == 0;
        } else if (value instanceof Collection) {
            return ((Collection<?>) value).isEmpty();
        } else if (value instanceof Map) {
            return ((Map<?, ?>) value).isEmpty();
        } else if (value.getClass().isArray()) {
            return Array.getLength(value) == 0;
        }

        return false;
    }

    public boolean isNotEmpty(Object value) {
        return !isEmpty(value);
    }

}

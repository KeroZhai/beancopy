package com.keroz.morphling.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ReflectionUtils {

    public List<Field> getDeclaredAndInheritedFields(Class<?> declaringClass) {
        List<Field> fields = new ArrayList<>();
        Set<String> fieldNames = new HashSet<>();

        while (declaringClass != Object.class) {
            for (Field field : declaringClass.getDeclaredFields()) {
                if (fieldNames.add(field.getName())) {
                    fields.add(field);
                }
            }

            declaringClass = declaringClass.getSuperclass();
        }

        return fields;
    }

    public List<Method> getDeclaredAndInheritedMethods(Class<?> declaringClass) {
        List<Method> methods = new ArrayList<>();

        while (declaringClass != Object.class) {
            // TODO remove duplicated
            methods.addAll(Arrays.asList(declaringClass.getDeclaredMethods()));

            declaringClass = declaringClass.getSuperclass();
        }

        return methods;
    }

    public Field findDeclaredField(Class<?> declaringClass, String fieldName) {
        Field field = null;
        try {
            field = declaringClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException | SecurityException ignored) {
        }

        return field;
    }

    public Field findDeclaredOrInheritedField(Class<?> declaringClass, String fieldName) {
        while (declaringClass != Object.class) {
            try {
                return declaringClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException | SecurityException ignored) {
                declaringClass = declaringClass.getSuperclass();
            }
        }

        return null;
    }

    public Method findDeclaredMethod(Class<?> declaringClass, String methodName, Class<?>... parameterTypes) {
        try {
            return declaringClass.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException | SecurityException ignored) {
        }

        return null;
    }

    public Method findDeclaredOrInheritedMethod(Class<?> declaringClass, String methodName,
            Class<?>... parameterTypes) {
        while (declaringClass != Object.class) {
            try {
                return declaringClass.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException | SecurityException ignored) {
                declaringClass = declaringClass.getSuperclass();
            }
        }

        return null;
    }

    public Class<?> toClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        }

        return toClass(((ParameterizedType) type).getRawType());
    }

    public boolean isPrimitiveType(Type type) {
        if (type instanceof Class) {
            return ((Class<?>) type).isPrimitive();
        }

        return false;
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

    public Class<?> toWrapper(Type primitiveType) {
        Class<?> clazz = toClass(primitiveType);

        if (!clazz.isPrimitive())
            return clazz;

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

        return clazz;
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

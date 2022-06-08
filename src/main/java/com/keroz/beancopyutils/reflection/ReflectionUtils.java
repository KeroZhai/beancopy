package com.keroz.beancopyutils.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ReflectionUtils {

    public static enum GeneralType {
        PRIMITIVE, ARRAY, COLLECTION, OBJECT
    }

    /**
     * 获取包括父类的所有字段
     */
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (!clazz.equals(Object.class)) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    /**
     * Returns all valid (non-final and non-static) fields (including the inherited
     * ones).
     *
     */
    public static List<Field> getAllValidFields(Class<?> clazz) {
        return getAllFields(clazz).stream().filter(field -> {
            int mod = field.getModifiers();
            return (mod & Modifier.FINAL) == 0 && (mod & Modifier.STATIC) == 0;
        }).collect(Collectors.toList());
    }

    public static List<ExtendedField> getAllValidFieldWrappers(Class<?> clazz) {
        return getAllFields(clazz).stream().filter(field -> {
            int mod = field.getModifiers();
            return (mod & Modifier.FINAL) == 0 && (mod & Modifier.STATIC) == 0;
        }).map(ExtendedField::new).collect(Collectors.toList());
    }

    /**
     * 获取泛型类型
     */
    @SuppressWarnings("rawtypes")
    public static Class getFieldGenericType(ExtendedField field) {
        ParameterizedType type = (ParameterizedType) field.getGenericType();
        return (Class) type.getActualTypeArguments()[0];
    }

    public static List<Method> getAllMethods(Class<?> clazz) {
        List<Method> methods = new ArrayList<>();
        methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        Class<?> superClass = clazz.getSuperclass();
        while (!superClass.equals(Object.class)) {
            methods.addAll(Arrays.asList(superClass.getDeclaredMethods()));
            superClass = superClass.getSuperclass();
        }
        return methods;
    }

    public static GeneralType getGeneralType(Class<?> clazz) {
        if (isPrimitive(clazz)) {
            return GeneralType.PRIMITIVE;
        } else if (clazz.isArray()) {
            return GeneralType.ARRAY;
        } else if (isCollection(clazz)) {
            return GeneralType.COLLECTION;
        }
        return GeneralType.OBJECT;
    }

    /**
     * 判断是否是基本类型(包含{@code String}, {@code Date}和{@code Enum}类型)
     *
     * @param clazz 类对象
     * @return {@code true}或{@code false}
     */
    public static boolean isPrimitive(Class<?> clazz) {
        boolean result = false;
        if (clazz.isPrimitive() || clazz == String.class || clazz == Date.class
                || Temporal.class.isAssignableFrom(clazz) || clazz.isEnum()) {
            result = true;
        } else {
            // 判断是否基本类型的包装类
            try {
                Field field = clazz.getDeclaredField("TYPE");
                if (field != null && ((Class<?>) field.get(null)).isPrimitive()) {
                    result = true;
                }
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
        }
        return result;
    }

    /**
     * Checks if the given class is of type {@link Collection}
     *
     * @param clazz the class to check
     * @return {@code true} if the given class is of type {@link Collection},
     *         otherwise {@code false}.
     */
    public static boolean isCollection(Class<?> clazz) {
        return Collection.class.isAssignableFrom(clazz);
    }

}

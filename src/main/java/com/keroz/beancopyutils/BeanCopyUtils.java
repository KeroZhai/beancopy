package com.keroz.beancopyutils;

import java.lang.ref.ReferenceQueue;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.keroz.beancopyutils.annotation.CopyIgnore;
import com.keroz.beancopyutils.annotation.IgnoreCondition;
import com.keroz.beancopyutils.cache.Cache;
import com.keroz.beancopyutils.cache.CacheReference;
import com.keroz.beancopyutils.fieldaccesser.FieldReader;
import com.keroz.beancopyutils.fieldaccesser.FieldWriter;

/**
 * Bean 拷贝工具, 集合属性只支持{@code List}
 * <p>
 * 提供了拷贝单个对象和{@code List}的若干个静态重载方法，搭配{@link CopyIgnore}和
 * {@link IgnoreCondition}以实现多种灵活的拷贝方式。
 * 
 */
public class BeanCopyUtils {

    /**
     * 缓存
     */
    private static ConcurrentHashMap<Class<?>, CacheReference> cacheMap = new ConcurrentHashMap<>();
    private static ReferenceQueue<Cache> referenceQueue = new ReferenceQueue<>();

    /**
     * 是否启用缓存
     */
    private static boolean cacheEnabled = true;

    /**
     * 查看是否启用了缓存
     * 
     * @return 如果启用则返回{@code true}
     */
    public static boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public static void enableCache() {
        cacheEnabled = true;
    }

    public static void disableCache() {
        cacheEnabled = false;
    }

    /**
     * @param <Source> 源对象类型
     * @param <Target> 目标对象类型
     * @param source   源对象
     * @param target   目标对象
     */
    public static <Target, Source> void copy(Source source, Target target) {
        copy(source, target, false, null);
    }

    /**
     * @param <Source>   源对象类型
     * @param <Target>   目标对象类型
     * @param source     源对象
     * @param target     目标对象
     * @param ignoreNull 是否忽略 null 值
     * 
     * @see CopyIgnore
     */
    public static <Target, Source> void copy(Source source, Target target, boolean ignoreNull) {
        copy(source, target, ignoreNull, null);
    }

    /**
     * @param <Source>         源对象类型
     * @param <Target>         目标对象类型
     * @param source           源对象
     * @param target           目标对象
     * @param ignoreConditions 拷贝忽略条件
     * 
     * @see CopyIgnore
     */
    public static <Target, Source> void copy(Source source, Target target, String[] ignoreConditions) {
        copy(source, target, false, ignoreConditions);
    }

    /**
     * @param <Source>         源对象类型
     * @param <Target>         目标对象类型
     * @param source           源对象
     * @param target           目标对象
     * @param ignoreNull       是否忽略null值
     * @param ignoreConditions 拷贝忽略条件
     * 
     * @see CopyIgnore
     */
    public static <Target, Source> void copy(Source source, Target target, boolean ignoreNull,
            String[] ignoreConditions) {
        if (target == null) {
            throw new IllegalArgumentException("Target object is null");
        }
        doCopy(source, target, ignoreNull, ignoreConditions);
    }

    /**
     * @param <Source> 源对象类型
     * @param <Target> 目标对象类型
     * @param source   源对象
     * @param tarClass 目标对象类对象
     * @return 目标对象
     */
    public static <Target, Source> Target copy(Source source, Class<Target> tarClass) {
        return copy(source, tarClass, false, null);
    }

    /**
     * @param <Source>   源对象类型
     * @param <Target>   目标对象类型
     * @param source     源对象
     * @param tarClass   目标对象类对象
     * @param ignoreNull 是否忽略null值
     * @return 目标对象
     */
    public static <Target, Source> Target copy(Source source, Class<Target> tarClass, boolean ignoreNull) {
        return copy(source, tarClass, ignoreNull, null);
    }

    /**
     * @param <Source>         源对象类型
     * @param <Target>         目标对象类型
     * @param source           源对象
     * @param tarClass         目标对象类对象
     * @param ignoreConditions 拷贝忽略条件
     * @return 目标对象
     */
    public static <Target, Source> Target copy(Source source, Class<Target> tarClass, String[] ignoreConditions) {
        return copy(source, tarClass, false, ignoreConditions);
    }

    /**
     * @param <Source>         源对象类型
     * @param <Target>         目标对象类型
     * @param source           源对象
     * @param tarClass         目标对象类对象
     * @param ignoreConditions 拷贝忽略条件
     * @param ignoreNull       是否忽略null值
     * @return 目标对象
     */
    public static <Target, Source> Target copy(Source source, Class<Target> tarClass, boolean ignoreNull,
            String[] ignoreConditions) {
        if (source == null) {
            return null;
        }
        if (tarClass == null) {
            throw new IllegalArgumentException("Target class is null");
        }
        Target target = null;
        try {
            target = tarClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        copy(source, target, ignoreNull, ignoreConditions);
        return target;
    }

    /**
     * @param <Source> 源对象类型
     * @param <Target> 目标对象类型
     * @param srcList  源对象List
     * @param tarClass 目标对象类对象
     * @return 目标对象List
     */
    public static <Target, Source> List<Target> copyList(List<Source> srcList, Class<Target> tarClass) {
        return copyList(srcList, tarClass, false, null);
    }

    /**
     * @param <Source>         源对象类型
     * @param <Target>         目标对象类型
     * @param srcList          源对象List
     * @param tarClass         目标对象类对象
     * @param ignoreConditions 拷贝忽略条件
     * @return 目标对象List
     */
    public static <Target, Source> List<Target> copyList(List<Source> srcList, Class<Target> tarClass,
            String[] ignoreConditions) {
        return copyList(srcList, tarClass, false, ignoreConditions);
    }

    /**
     * @param <Source>   源对象类型
     * @param <Target>   目标对象类型
     * @param srcList    源对象List
     * @param tarClass   目标对象类对象
     * @param ignoreNull 是否忽略null值
     * @return 目标对象List
     */
    public static <Target, Source> List<Target> copyList(List<Source> srcList, Class<Target> tarClass,
            boolean ignoreNull) {
        return copyList(srcList, tarClass, ignoreNull, null);
    }

    /**
     * @param <Source>         源对象类型
     * @param <Target>         目标对象类型
     * @param srcList          源对象List
     * @param tarClass         目标对象类对象
     * @param ignoreConditions 拷贝忽略条件
     * @param ignoreNull       是否忽略null值
     * @return 目标对象List
     */
    @SuppressWarnings("unchecked")
    public static <Target, Source> List<Target> copyList(List<Source> srcList, Class<Target> tarClass,
            boolean ignoreNull, String[] ignoreConditions) {
        List<Target> tarList = new ArrayList<>();
        if (srcList == null) {
            return null;
        }
        boolean isPrimitive = isPrimitive(tarClass);
        for (Source src : srcList) {
            try {
                if (isPrimitive) {
                    tarList.add((Target) src);
                } else {
                    tarList.add((Target) doCopy(src, tarClass.newInstance(), ignoreNull, ignoreConditions));
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
                throw new com.keroz.beancopyutils.exception.InstantiationException(
                        "Instantiate class failed: " + tarClass.getName());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return tarList;
    }

    /**
     * 判断是否是基本类型(包含{@code String}, {@code Date}和{@code Enum}类型)
     *
     * @param clazz 类对象
     * @return {@code true}或{@code false}
     */
    private static boolean isPrimitive(Class<?> clazz) {
        boolean result = false;
        if (clazz.isPrimitive() || clazz == String.class || clazz == Date.class || clazz.isEnum()) {
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
     * 根据注解中的值和忽略条件确定是否忽略该属性
     * 
     * @param copyIgnore       拷贝忽略注解
     * @param ignoreConditions 忽略条件
     * @return {@code true} 或 {@code false}
     */
    private static boolean shouldIgnore(CopyIgnore copyIgnore, String[] ignoreConditions) {
        boolean ignore = false;
        if (copyIgnore == null) {
            return ignore;
        }
        String[] whenConditions = copyIgnore.when();
        boolean whenConditionsEmpty = whenConditions.length == 1 && "".equals(whenConditions[0]);
        String[] exceptConditions = copyIgnore.except();
        boolean exceptConditionsEmpty = exceptConditions.length == 1 && "".equals(exceptConditions[0]);

        if (whenConditionsEmpty && exceptConditionsEmpty) {
            ignore = true;
        } else {
            if (!whenConditionsEmpty) {
                ignore = hasCondition(whenConditions, ignoreConditions) ? true : false;
            }
            if (!exceptConditionsEmpty) {
                ignore = hasCondition(exceptConditions, ignoreConditions) ? false : true;
            }
        }
        return ignore;
    }

    private static boolean hasCondition(String[] annotationValue, String[] ignoreConditions) {
        if (ignoreConditions == null || ignoreConditions.length == 0) {
            return false;
        }
        for (String s1 : annotationValue) {
            for (String s2 : ignoreConditions) {
                if (s1.equals(s2)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 执行实际的拷贝过程
     * 
     * @param <Source> 源对象类型
     * @param <Target> 目标对象类型
     * @param source
     * @param target
     * @param ignoreNull
     * @param ignoreConditions
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <Target, Source> Target doCopy(Source source, Target target, boolean ignoreNull,
            String[] ignoreConditions) {
        if (source == null) {
            return null;
        }
        try {
            Class<Source> srcClass = (Class<Source>) source.getClass();
            Class<Target> tarClass = (Class<Target>) target.getClass();
            HashMap<String, FieldWriter> fieldWriterMap = null;
            String tarFieldName = null;
            Class<?> tarFieldClass = null;
            Cache srcCache = null, tarCache = null;
            boolean cached = false;
            if (cacheEnabled) {
                srcCache = getCache(srcClass);
                tarCache = getCache(tarClass);
                fieldWriterMap = tarCache.getFieldWriterMap();
                if (fieldWriterMap != null) {
                    // 注意这里只是代表曾经缓存过, 但不一定所有字段都有
                    cached = true;
                    for (Field tarField : getAllValidFields(tarClass)) {
                        tarFieldName = tarField.getName();
                        FieldReader fieldReader = getFieldReaderWithCache(srcClass, tarFieldName, srcCache);
                        if (fieldReader != null) {
                            FieldWriter fieldWriter = getFieldWriterWithCache(tarClass, tarFieldName, tarCache);
                            fieldWriter.write(target, fieldReader.read(source), ignoreNull, ignoreConditions);
                        }
                    }
                }
            }
            if (!cached) {
                List<Field> tarFields = getAllValidFields(tarClass);
                for (Field tarField : tarFields) {
                    tarField.setAccessible(true);
                    tarFieldName = tarField.getName();
                    tarFieldClass = tarField.getType();
                    if (cacheEnabled) {
                        FieldReader fieldReader = getFieldReaderWithCache(srcClass, tarFieldName, srcCache);
                        FieldWriter fieldWriter = getFieldWriterWithCache(tarClass, tarFieldName, tarCache);
                        if (fieldReader != null) {
                            // 由于这里立刻就进行写操作, 会中断后续的缓存
                            fieldWriter.write(target, fieldReader.read(source), ignoreNull, ignoreConditions);
                        }
                    } else {
                        FieldReader fieldReader = getFieldReaderWithoutCache(srcClass, tarFieldName, null);
                        if (fieldReader != null) {
                            Object value = fieldReader.read(source);
                            if (value == null && ignoreNull) {
                                continue;
                            }
                            if (isPrimitive(tarFieldClass)) {
                                // 如果是基本类型, 直接复制
                                tarField.set(target, value);
                            } else if (tarFieldClass == List.class) {
                                // 如果是List类型
                                tarField.set(target, copyList((List<?>) value, getFieldGenericType(tarField),
                                        ignoreNull, ignoreConditions));
                            } else {
                                // 如果是其他引用类型
                                tarField.set(target, BeanCopyUtils.doCopy(value, tarFieldClass.newInstance(),
                                        ignoreNull, ignoreConditions));
                            }
                        }
                    }

                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return target;
    }

    /**
     * 获取缓存, 获取不到则新建
     * 
     * @param clazz 类对象
     * @return 类对象对应的缓存
     */
    private static Cache getCache(Class<?> clazz) {
        expungeStaleEntries();
        CacheReference ref = cacheMap.get(clazz);
        if (ref == null || ref.get() == null) {
           newCacheFor(clazz);
        }
        ref = cacheMap.get(clazz);
        return ref.get();
    }

    private static void newCacheFor(Class<?> clazz) {
        synchronized (clazz) {
            CacheReference ref = cacheMap.get(clazz);
            if (ref == null || ref.get() == null) {
                ref = new CacheReference(clazz, referenceQueue);
                cacheMap.put(clazz, ref);
            }
        } 
    }

    private static void expungeStaleEntries() {
        for (Object collected; (collected = referenceQueue.poll()) != null;) {
            synchronized (referenceQueue) {
                // @SuppressWarnings("unchecked")
                CacheReference ref = (CacheReference) collected;
                cacheMap.remove(ref.getCachedClass());
            }
        }
    }

    /**
     * 获取包括父类的所有字段
     */
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        Class<?> superClass = clazz.getSuperclass();
        while (!superClass.equals(Object.class)) {
            fields.addAll(Arrays.asList(superClass.getDeclaredFields()));
            superClass = superClass.getSuperclass();
        }
        return fields;
    }

    /**
     * 获取包括父类的所有有效字段(不是final且不是忽略条件)
     */
    private static List<Field> getAllValidFields(Class<?> clazz) {
        return getAllFields(clazz).stream().filter(field -> field.getDeclaredAnnotation(IgnoreCondition.class) == null
                || !Modifier.isFinal(field.getModifiers())).collect(Collectors.toList());
    }

    /**
     * 获取List的泛型类型
     */
    private static Class<?> getFieldGenericType(Field field) {
        ParameterizedType type = (ParameterizedType) field.getGenericType();
        return (Class<?>) type.getActualTypeArguments()[0];
    }

    /**
     * 从缓存中获取指定字段的读方法
     * 
     * @param source    源对象
     * @param fieldName 字段名
     */
    private static FieldReader getFieldReaderWithCache(Class<?> srcClass, String fieldName, Cache cache) {
        FieldReader fieldReader = null;
        if (cache.getFieldReaderMap() == null) {
            cache.setFieldReaderMap(new HashMap<>());
        }
        HashMap<String, FieldReader> readMethodMap = cache.getFieldReaderMap();
        // 尝试从缓存中获取读方法
        if (readMethodMap.containsKey(fieldName)) {
            fieldReader = readMethodMap.get(fieldName);
        } else {
            fieldReader = getFieldReaderWithoutCache(srcClass, fieldName, cache.getMethodAccess());
            if (fieldReader != null) {
                readMethodMap.put(fieldName, fieldReader);
            }
        }
        return fieldReader;

    }

    private static FieldReader getFieldReaderWithoutCache(Class<?> srcClass, String fieldName,
            MethodAccess methodAccess) {
        FieldReader fieldReader = null;
        String methodNameSuffix = getMethodNameSuffix(fieldName);
        boolean hasReadMethod = false;
        if (methodAccess != null) {
            String[] methodNames = methodAccess.getMethodNames();
            int index = -1;
            for (int i = 0; i < methodNames.length; i++) {
                if (methodNames[i].equals("is" + methodNameSuffix) || methodNames[i].equals("get" + methodNameSuffix)) {
                    index = i;
                    hasReadMethod = true;
                    break;
                }
            }
            if (index != -1) {
                final int methodIndex = index;
                fieldReader = (s) -> methodAccess.invoke(s, methodIndex);
            }
        } else {
            List<Method> methods = getAllMethods(srcClass);
            for (Method method : methods) {
                if (method.getName().equals("is" + methodNameSuffix)
                        || method.getName().equals("get" + methodNameSuffix)) {
                    fieldReader = (s) -> {
                        try {
                            return method.invoke(s);
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        return null;
                    };
                    hasReadMethod = true;
                    break;
                }
            }
        }
        if (!hasReadMethod) {
            // 没有读方法, 直接通过Field取值
            try {
                Field field = null;
                for (Field f : getAllValidFields(srcClass)) {
                    if (f.getName().equals(fieldName)) {
                        field = f;
                        break;
                    }
                }
                if (field == null) {
                    throw new NoSuchFieldException(fieldName);
                }
                field.setAccessible(true);
                final Field f = field;
                fieldReader = (s) -> {
                    try {
                        return f.get(s);
                    } catch (IllegalArgumentException | IllegalAccessException ignore) {
                    }
                    return null;
                };
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException ignore) {
            }
        }
        return fieldReader;
    }

    private static String getMethodNameSuffix(String fieldName) {
        char[] chars = fieldName.toCharArray();
        chars[0] = (char) (chars[0] - 32);
        return new String(chars);
    }

    private static FieldWriter getFieldWriterWithCache(Class<?> tarClass, String fieldName, Cache cache) {
        FieldWriter writeMethod = (t, v, in, i) -> {
        };
        if (cache.getFieldWriterMap() == null) {
            cache.setFieldWriterMap(new HashMap<>());
        }
        HashMap<String, FieldWriter> writeMethodMap = cache.getFieldWriterMap();
        // 尝试从缓存中获取
        if (writeMethodMap.containsKey(fieldName)) {
            writeMethod = writeMethodMap.get(fieldName);
        } else {
            writeMethod = getFieldWriterWithoutCache(tarClass, fieldName, cache.getMethodAccess());
            writeMethodMap.put(fieldName, writeMethod);
        }
        return writeMethod;
    }

    private static FieldWriter getFieldWriterWithoutCache(Class<?> tarClass, String fieldName,
            MethodAccess methodAccess) {
        Field field = null;
        FieldWriter fieldWriter = (t, v, in, i) -> {
        };
        try {
            for (Field f : getAllValidFields(tarClass)) {
                if (f.getName().equals(fieldName)) {
                    field = f;
                    break;
                }
            }
            if (field == null) {
                throw new NoSuchFieldException(fieldName);
            }
        } catch (NoSuchFieldException | SecurityException e2) {
            e2.printStackTrace();
            return fieldWriter;
        }
        Class<?> fieldClass = field.getType();
        CopyIgnore copyIgnore = field.getDeclaredAnnotation(CopyIgnore.class);
        final Field f = field;
        String methodNameSuffix = getMethodNameSuffix(fieldName);
        boolean hasWriteMethod = false;
        if (methodAccess != null) {
            String[] methodNames = methodAccess.getMethodNames();
            int index = -1;
            for (int i = 0; i < methodNames.length; i++) {
                if (methodNames[i].equals("set" + methodNameSuffix)) {
                    index = i;
                    hasWriteMethod = true;
                    break;
                }
            }
            if (index != -1) {
                final int methodIndex = index;
                if (isPrimitive(fieldClass)) {
                    fieldWriter = (t, v, in, i) -> {
                        invokeMethodAccess(methodAccess, methodIndex, t, v, copyIgnore, in, i);
                    };
                } else if (fieldClass.equals(List.class)) {
                    fieldWriter = (t, v, in, i) -> {
                        v = copyList((List<?>) v, getFieldGenericType(f), in, i);
                        invokeMethodAccess(methodAccess, methodIndex, t, v, copyIgnore, in, i);
                    };
                } else {
                    fieldWriter = (t, v, in, i) -> {
                        v = copy(v, fieldClass, in, i);
                        invokeMethodAccess(methodAccess, methodIndex, t, v, copyIgnore, in, i);
                    };
                }

            }
        } else {
            List<Method> methods = getAllMethods(tarClass);
            for (Method method : methods) {
                if (method.getName().equals("set" + methodNameSuffix)) {
                    if (isPrimitive(fieldClass)) {
                        fieldWriter = (t, v, in, i) -> invokeSetMethod(method, t, v, copyIgnore, in, i);
                    } else if (fieldClass.equals(List.class)) {
                        fieldWriter = (t, v, in, i) -> {
                            v = copyList((List<?>) v, getFieldGenericType(f), in, i);
                            invokeSetMethod(method, t, v, copyIgnore, in, i);
                        };
                    } else {
                        fieldWriter = (t, v, in, i) -> {
                            v = copy(v, fieldClass, in, i);
                            invokeSetMethod(method, t, v, copyIgnore, in, i);
                        };
                    }
                    hasWriteMethod = true;
                    break;
                }
            }
        }
        if (!hasWriteMethod) {
            try {
                f.setAccessible(true);
                if (isPrimitive(fieldClass)) {
                    fieldWriter = (t, v, in, i) -> setFieldValue(f, t, v, copyIgnore, in, i);
                } else if (fieldClass.equals(List.class)) {
                    fieldWriter = (t, v, in, i) -> {
                        v = copyList((List<?>) v, getFieldGenericType(f), in, i);
                        setFieldValue(f, t, v, copyIgnore, in, i);
                    };
                } else {
                    fieldWriter = (t, v, in, i) -> {
                        v = copy(v, fieldClass, in, i);
                        setFieldValue(f, t, v, copyIgnore, in, i);
                    };
                }

            } catch (SecurityException | IllegalArgumentException ex) {
                ex.printStackTrace();
            }
        }
        return fieldWriter;
    }

    private static void invokeMethodAccess(MethodAccess methodAccess, int methodIndex, Object target, Object value,
            CopyIgnore copyIgnore, boolean ignoreNull, String[] ignoreConditions) {
        if (shouldIgnore(copyIgnore, ignoreConditions) || (value == null && ignoreNull)) {
            return;
        }
        methodAccess.invoke(target, methodIndex, value);

    }

    private static void invokeSetMethod(Method method, Object target, Object value, CopyIgnore copyIgnore,
            boolean ignoreNull, String[] ignoreConditions) {
        try {
            if (shouldIgnore(copyIgnore, ignoreConditions) || (value == null && ignoreNull)) {
                return;
            }
            method.invoke(target, value);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static void setFieldValue(Field field, Object target, Object value, CopyIgnore copyIgnore,
            boolean ignoreNull, String[] ignoreConditions) {
        try {
            if (shouldIgnore(copyIgnore, ignoreConditions) || (value == null && ignoreNull)) {
                return;
            }
            field.set(target, value);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private static List<Method> getAllMethods(Class<?> clazz) {
        List<Method> methods = new ArrayList<>();
        methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        Class<?> superClass = clazz.getSuperclass();
        while (!superClass.equals(Object.class)) {
            methods.addAll(Arrays.asList(superClass.getDeclaredMethods()));
            superClass = superClass.getSuperclass();
        }
        return methods;
    }

}

package com.keroz.beancopyutils.copier;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.keroz.beancopyutils.annotation.CopyIgnore;
import com.keroz.beancopyutils.annotation.CopyIgnore.IgnorePolicy;
import com.keroz.beancopyutils.converter.Converter;
import com.keroz.beancopyutils.converter.ConverterFactory;
import com.keroz.beancopyutils.exception.TypeMismatchException;
import com.keroz.beancopyutils.reflection.ExtendedField;
import com.keroz.beancopyutils.reflection.ReflectionUtils;
import com.keroz.beancopyutils.reflection.ReflectionUtils.GeneralType;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * The default copier used by {@code BeanCopyUtils}.
 * <p>
 * This copier class uses a mechanism of field readers and field writers. For
 * each field there is a field reader and a field writer, which will be cached.
 * A field reader or a field writer will be generated only when it's needed,
 * that's to say, a field reader/writer is generated only when getting/setting
 * its value.
 * 
 */
@Slf4j
public class DefaultCopier extends AbstractCachedCopier {

    /**
     * A reader for a certain filed in a source class.
     */
    private static interface FieldReader {
        /**
         * Reads the field value from a source object.
         * 
         * @param source the source object to read from
         * @return the field value
         */
        Object read(Object source);
    }

    /**
     * A writer for a certain filed in a target class.
     */
    private static interface FieldWriter {
        /**
         * Writes the field value into a target object.
         * 
         * @param target           the target object to write into
         * @param source           the source object to read from
         * @param fieldReader      the field reader for the field
         * @param ignorePolicy     whether ignore null source value or not
         * @param ignoreConditions ignore conditions
         */
        void write(Object target, Object source, FieldReader fieldReader, IgnorePolicy ignorePolicy,
                String[] ignoreConditions);
    }

    @Data
    protected static class DefaultCache extends AbstractCachedCopier.Cache {

        private MethodAccess methodAccess;
        private List<ExtendedField> fields;
        private HashMap<String, FieldReader> fieldReaderMap = new HashMap<>();
        private HashMap<String, FieldWriter> fieldWriterMap = new HashMap<>();

        public DefaultCache(Class<?> clazz) {
            super(clazz);
            this.methodAccess = MethodAccess.get(clazz);
            this.fields = ReflectionUtils.getAllValidFieldWrappers(clazz);
        }

    }

    @Override
    @SuppressWarnings("unchecked")
    public <Source, Target> Target copy(Source source, Target target, IgnorePolicy ignorePolicy,
            String[] ignoreConditions) {
        Class<Source> srcClass = (Class<Source>) source.getClass();
        Class<Target> tarClass = (Class<Target>) target.getClass();

        List<ExtendedField> targetFieldList = null;
        DefaultCache srcCache = null, tarCache = null;
        srcCache = (DefaultCache) getCache(srcClass);
        tarCache = (DefaultCache) getCache(tarClass);
        targetFieldList = tarCache.getFields();

        for (ExtendedField targetField : targetFieldList) {
            FieldReader fieldReader = getFieldReader(srcClass, targetField.getAliasFor(), srcCache);
            FieldWriter fieldWriter = getFieldWriter(tarClass, targetField, tarCache);
            if (fieldReader != null) {
                fieldWriter.write(target, source, fieldReader, ignorePolicy, ignoreConditions);
            }
        }

        return target;
    }

    /**
     * 从缓存中获取指定字段的读方法
     * 
     * @param source    源对象
     * @param fieldName 字段名
     */
    private FieldReader getFieldReader(Class<?> srcClass, String fieldName, DefaultCache cache) {
        HashMap<String, FieldReader> readMethodMap = cache.getFieldReaderMap();
        // 尝试从缓存中获取读方法
        if (!readMethodMap.containsKey(fieldName)) {
            generateFieldReader(srcClass, fieldName, cache);
        }
        return readMethodMap.get(fieldName);

    }

    private void generateFieldReader(Class<?> srcClass, String fieldName, DefaultCache cache) {
        synchronized (cache) {
            HashMap<String, FieldReader> readMethodMap = cache.getFieldReaderMap();
            if (!readMethodMap.containsKey(fieldName)) {
                log.debug(Thread.currentThread().getName() + " is generating field reader for " + srcClass.getName()
                        + "." + fieldName);
                FieldReader fieldReader = newFieldReader(srcClass, fieldName, cache.getMethodAccess());
                if (fieldReader != null) {
                    readMethodMap.put(fieldName, fieldReader);
                }
            }
        }
    }

    private FieldReader newFieldReader(Class<?> srcClass, String fieldName, MethodAccess methodAccess) {
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
            List<Method> methods = ReflectionUtils.getAllMethods(srcClass);
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
                for (Field f : ReflectionUtils.getAllValidFields(srcClass)) {
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

    private String getMethodNameSuffix(String fieldName) {
        char[] chars = fieldName.toCharArray();
        chars[0] = (char) (chars[0] - 32);
        return new String(chars);
    }

    private FieldWriter getFieldWriter(Class<?> tarClass, ExtendedField field, DefaultCache cache) {
        String fieldName = field.getName();
        HashMap<String, FieldWriter> writeMethodMap = cache.getFieldWriterMap();
        if (!writeMethodMap.containsKey(fieldName)) {
            generateFieldWriter(tarClass, field, cache);
        }

        return writeMethodMap.get(fieldName);
    }

    private void generateFieldWriter(Class<?> tarClass, ExtendedField field, DefaultCache cache) {
        synchronized (cache) {
            String fieldName = field.getName();
            HashMap<String, FieldWriter> writeMethodMap = cache.getFieldWriterMap();
            // 尝试从缓存中获取
            if (!writeMethodMap.containsKey(fieldName)) {
                log.debug(Thread.currentThread().getName() + " is generating field writer for " + tarClass.getName()
                        + "." + fieldName);
                writeMethodMap.put(fieldName, newFieldWriter(tarClass, field, cache.getMethodAccess()));
            }
        }

    }

    private FieldWriter newFieldWriter(Class<?> tarClass, final ExtendedField field, MethodAccess methodAccess) {

        FieldWriter fieldWriter = (t, s, r, in, i) -> {
        };

        CopyIgnore copyIgnore = field.getCopyIgnore();
        final Converter<?, ?> converter = ConverterFactory.getConverter(field.getConverterClass());
        GeneralType generalType = ReflectionUtils.getGeneralType(field.getType());
        String methodNameSuffix = getMethodNameSuffix(field.getName());
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
                fieldWriter = (t, s, r, in, i) -> {
                    invokeMethodAccess(methodAccess, methodIndex, t, s, r, field, generalType, converter, copyIgnore,
                            in, i);
                };
            }
        } else {
            List<Method> methods = ReflectionUtils.getAllMethods(tarClass);
            for (Method method : methods) {
                if (method.getName().equals("set" + methodNameSuffix)) {
                    fieldWriter = (t, s, r, in, i) -> {
                        invokeSetMethod(method, t, s, r, field, generalType, converter, copyIgnore, in, i);
                    };
                    hasWriteMethod = true;
                    break;
                }
            }
        }
        if (!hasWriteMethod) {
            try {
                field.setAccessible(true);
                fieldWriter = (t, s, r, in, i) -> {
                    setFieldValue(field, t, s, r, field, generalType, converter, copyIgnore, in, i);
                };
            } catch (SecurityException | IllegalArgumentException ex) {
                ex.printStackTrace();
            }
        }
        return fieldWriter;
    }

    @SuppressWarnings(value = { "rawtypes", "unchecked" })
    private Object handle(Object source, FieldReader fieldReader, ExtendedField targetField,
            GeneralType targetFieldGeneralType, Converter converter, Class<? extends Collection<?>> collectionClass,
            IgnorePolicy ignorePolicy, String[] ignoreConditions) {
        Object result = fieldReader.read(source);
        Class targetFieldClass = targetField.getType();
        if (converter != null) {
            result = converter.convert(result);
        } else {
            if (result != null) {
                switch (targetFieldGeneralType) {
                    case PRIMITIVE:
                        // Do nothing
                        break;
                    case ARRAY: {
                        if (result.getClass().isArray()) {
                            result = copyArray(result, targetFieldClass.getComponentType(), ignorePolicy, ignoreConditions);
                        } else {
                            throw new TypeMismatchException(Array.class, result.getClass());
                        }
                        break;
                    }
                    case COLLECTION: {
                        if (result instanceof Collection) {
                            collectionClass = collectionClass != null ? collectionClass
                                    : !targetFieldClass.isInterface() ? targetFieldClass : null;
                            result = copyCollection((Collection) result, ReflectionUtils.getFieldGenericType(targetField),
                                    collectionClass, ignorePolicy, ignoreConditions);
                        } else {
                            throw new TypeMismatchException(Collection.class, result.getClass());
                        }
                        break;
                    }
                    default: {
                        result = copy(result, targetFieldClass, ignorePolicy, ignoreConditions);
                        break;
                    }
    
                }
            }
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    private void invokeMethodAccess(MethodAccess methodAccess, int methodIndex, Object target, Object source,
            FieldReader fieldReader, ExtendedField targetField, GeneralType targetFieldGeneralType, Converter converter,
            CopyIgnore copyIgnore, IgnorePolicy ignorePolicy, String[] ignoreConditions) {
        if (shouldIgnore(copyIgnore, ignoreConditions)) {
            return;
        }
        Object value = handle(source, fieldReader, targetField, targetFieldGeneralType, converter,
                targetField.getCollectionClass(), ignorePolicy, ignoreConditions);
        if (shouldIgnoreNullOrEmpty(value, copyIgnore, ignorePolicy)) {
            return;
        }
        methodAccess.invoke(target, methodIndex, value);
    }

    @SuppressWarnings("rawtypes")
    private void invokeSetMethod(Method method, Object target, Object source, FieldReader fieldReader,
            ExtendedField targetField, GeneralType targetFieldGeneralType, Converter converter, CopyIgnore copyIgnore,
            IgnorePolicy ignorePolicy, String[] ignoreConditions) {
        try {
            if (shouldIgnore(copyIgnore, ignoreConditions)) {
                return;
            }
            Object value = handle(source, fieldReader, targetField, targetFieldGeneralType, converter,
                    targetField.getCollectionClass(), ignorePolicy, ignoreConditions);
            if (shouldIgnoreNullOrEmpty(value, copyIgnore, ignorePolicy)) {
                return;
            }
            method.invoke(target, value);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("rawtypes")
    private void setFieldValue(ExtendedField field, Object target, Object source, FieldReader fieldReader,
            ExtendedField targetField, GeneralType targetFieldGeneralType, Converter converter, CopyIgnore copyIgnore,
            IgnorePolicy ignorePolicy, String[] ignoreConditions) {
        try {
            if (shouldIgnore(copyIgnore, ignoreConditions)) {
                return;
            }
            Object value = handle(source, fieldReader, targetField, targetFieldGeneralType, converter,
                    targetField.getCollectionClass(), ignorePolicy, ignoreConditions);
            if (shouldIgnoreNullOrEmpty(value, copyIgnore, ignorePolicy)) {
                return;
            }
            field.set(target, value);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Cache newCacheFor(Class<?> clazz) {
        log.debug(Thread.currentThread().getName() + " is initiating cache for " + clazz.getName());
        return new DefaultCache(clazz);
    }

}
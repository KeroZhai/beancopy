package com.keroz.beancopyutils.copier;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.keroz.beancopyutils.annotation.CopyIgnore;
import com.keroz.beancopyutils.converter.Converter;
import com.keroz.beancopyutils.reflection.ExtendedField;
import com.keroz.beancopyutils.reflection.ReflectionUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultCopier extends AbstractCachedCopier {

    private static interface FieldReader {
        Object read(Object source);
    }

    private static interface FieldWriter {
        void write(Object target, Object value, boolean ignoreNull, String[] ignoreConditions);
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
    public <Source, Target> Target copy(Source source, Target target, boolean ignoreNull, String[] ignoreConditions) {
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
                fieldWriter.write(target, fieldReader.read(source), ignoreNull, ignoreConditions);
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
        if (cache == null) {
            return getFieldReaderWithoutCache(srcClass, fieldName, null);
        }
        HashMap<String, FieldReader> readMethodMap = cache.getFieldReaderMap();
        // 尝试从缓存中获取读方法
        if (!readMethodMap.containsKey(fieldName)) {
            newFieldReader(srcClass, fieldName, cache);
        }
        return readMethodMap.get(fieldName);

    }

    private void newFieldReader(Class<?> srcClass, String fieldName, DefaultCache cache) {
        synchronized (cache) {
            HashMap<String, FieldReader> readMethodMap = cache.getFieldReaderMap();
            if (!readMethodMap.containsKey(fieldName)) {
                log.debug(Thread.currentThread().getName() + " is initiating field reader for " + srcClass.getName()
                        + "." + fieldName);
                FieldReader fieldReader = getFieldReaderWithoutCache(srcClass, fieldName, cache.getMethodAccess());
                if (fieldReader != null) {
                    readMethodMap.put(fieldName, fieldReader);
                }
            }
        }
    }

    private FieldReader getFieldReaderWithoutCache(Class<?> srcClass, String fieldName, MethodAccess methodAccess) {
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
            newFieldWriter(tarClass, field, cache);
        }

        return writeMethodMap.get(fieldName);
    }

    private void newFieldWriter(Class<?> tarClass, ExtendedField field, DefaultCache cache) {
        synchronized (cache) {
            String fieldName = field.getName();
            HashMap<String, FieldWriter> writeMethodMap = cache.getFieldWriterMap();
            // 尝试从缓存中获取
            if (!writeMethodMap.containsKey(fieldName)) {
                log.debug(Thread.currentThread().getName() + " is initiating field writer for " + tarClass.getName()
                        + "." + fieldName);
                writeMethodMap.put(fieldName, getFieldWriterWithoutCache(tarClass, field, cache.getMethodAccess()));
            }
        }

    }

    private FieldWriter getFieldWriterWithoutCache(Class<?> tarClass, final ExtendedField field,
            MethodAccess methodAccess) {

        FieldWriter fieldWriter = (t, v, in, i) -> {
        };

        Class<?> fieldClass = field.getType();
        CopyIgnore copyIgnore = field.getDeclaredAnnotation(CopyIgnore.class);
        Class<? extends Converter<?, ?>> converterClass = field.getConverterClass();
        Converter<?, ?> converter = null;
        if (converterClass != null) {
            try {
                converter = converterClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                throw new com.keroz.beancopyutils.exception.InstantiationException(
                        "Failed to instantiate converter class: " + converterClass.getName(), e);
            }
        }
        final Converter<?, ?> c = converter;
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
                if (ReflectionUtils.isPrimitive(fieldClass)) {
                    fieldWriter = (t, v, in, i) -> {
                        invokeMethodAccess(methodAccess, methodIndex, t, v, copyIgnore, c, in, i);
                    };
                } else if (fieldClass.equals(List.class)) {
                    fieldWriter = (t, v, in, i) -> {
                        if (c == null) {
                            v = copyList((List<?>) v, ReflectionUtils.getFieldGenericType(field), in, i);
                        }
                        invokeMethodAccess(methodAccess, methodIndex, t, v, copyIgnore, c, in, i);
                    };
                } else {
                    fieldWriter = (t, v, in, i) -> {
                        if (c == null) {
                            v = copy(v, fieldClass, in, i);
                        }
                        invokeMethodAccess(methodAccess, methodIndex, t, v, copyIgnore, c, in, i);
                    };
                }

            }
        } else {
            List<Method> methods = ReflectionUtils.getAllMethods(tarClass);
            for (Method method : methods) {
                if (method.getName().equals("set" + methodNameSuffix)) {
                    if (ReflectionUtils.isPrimitive(fieldClass)) {
                        fieldWriter = (t, v, in, i) -> {
                            invokeSetMethod(method, t, v, copyIgnore, c, in, i);
                        };
                    } else if (fieldClass.equals(List.class)) {
                        fieldWriter = (t, v, in, i) -> {
                            if (c == null) {
                                v = copyList((List<?>) v, ReflectionUtils.getFieldGenericType(field), in, i);
                            }
                            invokeSetMethod(method, t, v, copyIgnore, c, in, i);
                        };
                    } else {
                        fieldWriter = (t, v, in, i) -> {
                            if (c == null) {
                                v = copy(v, fieldClass, in, i);
                            }
                            invokeSetMethod(method, t, v, copyIgnore, c, in, i);
                        };
                    }
                    hasWriteMethod = true;
                    break;
                }
            }
        }
        if (!hasWriteMethod) {
            try {
                field.setAccessible(true);
                if (ReflectionUtils.isPrimitive(fieldClass)) {
                    fieldWriter = (t, v, in, i) -> {
                        setFieldValue(field, t, v, copyIgnore, c, in, i);
                    };
                } else if (fieldClass.equals(List.class)) {
                    fieldWriter = (t, v, in, i) -> {
                        if (c == null) {
                            v = copyList((List<?>) v, ReflectionUtils.getFieldGenericType(field), in, i);
                        }
                        setFieldValue(field, t, v, copyIgnore, c, in, i);
                    };
                } else {
                    fieldWriter = (t, v, in, i) -> {
                        if (c == null) {
                            v = copy(v, fieldClass, in, i);
                        }
                        setFieldValue(field, t, v, copyIgnore, c, in, i);
                    };
                }

            } catch (SecurityException | IllegalArgumentException ex) {
                ex.printStackTrace();
            }
        }
        return fieldWriter;
    }

    @SuppressWarnings(value = { "rawtypes", "unchecked" })
    private void invokeMethodAccess(MethodAccess methodAccess, int methodIndex, Object target, Object value,
            CopyIgnore copyIgnore, Converter converter, boolean ignoreNull, String[] ignoreConditions) {
        if (shouldIgnore(copyIgnore, ignoreConditions, value, ignoreNull)) {
            return;
        }
        if (converter != null) {
            value = converter.convert(value);
        }
        methodAccess.invoke(target, methodIndex, value);

    }

    @SuppressWarnings(value = { "rawtypes", "unchecked" })
    private void invokeSetMethod(Method method, Object target, Object value, CopyIgnore copyIgnore,
            Converter converter, boolean ignoreNull, String[] ignoreConditions) {
        try {
            if (shouldIgnore(copyIgnore, ignoreConditions, value, ignoreNull)) {
                return;
            }
            if (converter != null) {
                value = converter.convert(value);
            }
            method.invoke(target, value);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings(value = { "rawtypes", "unchecked" })
    private void setFieldValue(ExtendedField field, Object target, Object value, CopyIgnore copyIgnore,
            Converter converter, boolean ignoreNull, String[] ignoreConditions) {
        try {
            if (shouldIgnore(copyIgnore, ignoreConditions, value, ignoreNull)) {
                return;
            }
            if (converter != null) {
                value = converter.convert(value);
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
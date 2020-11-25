package com.keroz.beancopyutils.copier;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.keroz.beancopyutils.annotation.CopyIgnore;
import com.keroz.beancopyutils.annotation.CopyIgnore.IgnorePolicy;
import com.keroz.beancopyutils.exception.TypeMismatchException;
import com.keroz.beancopyutils.reflection.ReflectionUtils;

import lombok.Data;

public abstract class AbstractCachedCopier implements Copier {

    /**
     * 缓存
     */
    private ConcurrentHashMap<Class<?>, CacheReference> cacheMap = new ConcurrentHashMap<>();
    private ReferenceQueue<Cache> referenceQueue = new ReferenceQueue<>();

    @Override
    public <Source, Target> Target copy(Source source, Class<Target> targetClass, IgnorePolicy ignorePolicy,
            String[] ignoreConditions) {
        if (source == null) {
            return null;
        }
        if (targetClass == null) {
            throw new IllegalArgumentException("Target class is null");
        }
        Target target = null;
        try {
            target = targetClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new com.keroz.beancopyutils.exception.InstantiationException(
                    "Failed to instantiate class: " + targetClass.getName(), e);
        }
        copy(source, target, ignorePolicy, ignoreConditions);
        return target;
    }

    @Override
    public Object copyArray(Object sourceArray, Class<?> targetComponentClass, IgnorePolicy ignorePolicy,
            String[] ignoreConditions) {
        int length = Array.getLength(sourceArray);
        Object targetArray = Array.newInstance(targetComponentClass, length);
        try {
            if (ReflectionUtils.isPrimitive(targetComponentClass)) {
                for (int i = 0; i < length; i++) {
                    Array.set(targetArray, i, Array.get(sourceArray, i));
                }
            } else {
                for (int i = 0; i < length; i++) {
                    Array.set(targetArray, i,
                            copy(Array.get(sourceArray, i), targetComponentClass, ignorePolicy, ignoreConditions));
                }
            }
        } catch (IllegalArgumentException e) {
            throw new TypeMismatchException(targetArray.getClass(), sourceArray.getClass());
        }
        return targetArray;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <SourceComponent, TargetComponent> Collection<TargetComponent> copyCollection(
            Collection<SourceComponent> sourceCollection, Class<TargetComponent> targetComponentClass,
            Class<? extends Collection<?>> collectionClass, IgnorePolicy ignorePolicy, String[] ignoreConditions) {
        if (sourceCollection == null) {
            return null;
        }
        Class<? extends Collection<TargetComponent>> sourceCollectionClass = (Class<? extends Collection<TargetComponent>>) (collectionClass != null
                ? collectionClass
                : sourceCollection.getClass());
        Collection<TargetComponent> targetCollection = sourceCollection.stream()
                .map(sourceComponent -> ReflectionUtils.isPrimitive(targetComponentClass)
                        ? targetComponentClass.cast(sourceComponent)
                        : copy(sourceComponent, targetComponentClass, ignorePolicy, ignoreConditions))
                .collect(Collectors.toCollection(() -> {
                    try {
                        return sourceCollectionClass.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new com.keroz.beancopyutils.exception.InstantiationException(
                                "Failed to instantiate class: " + sourceCollectionClass.getName(), e);
                    }
                }));
        return targetCollection;
    }

    /**
     * 根据注解中的值和忽略条件确定是否忽略该属性
     * 
     * @param copyIgnore       拷贝忽略注解
     * @param ignoreConditions 忽略条件
     * @return {@code true} 或 {@code false}
     */
    protected final boolean shouldIgnore(CopyIgnore copyIgnore, String[] ignoreConditions) {
        boolean ignore = false;
        if (copyIgnore != null) {
            String[] whenConditions = copyIgnore.when();
            boolean whenConditionsEmpty = whenConditions.length == 1 && "".equals(whenConditions[0]);
            String[] exceptConditions = copyIgnore.except();
            boolean exceptConditionsEmpty = exceptConditions.length == 1 && "".equals(exceptConditions[0]);

            if (!whenConditionsEmpty) {
                ignore = hasCondition(whenConditions, ignoreConditions) ? true : false;
            }
            if (!exceptConditionsEmpty) {
                ignore = hasCondition(exceptConditions, ignoreConditions) ? false : true;
            }

        }
        return ignore;
    }

    @SuppressWarnings("rawtypes")
    protected final boolean shouldIgnoreNullOrEmpty(Object value, CopyIgnore copyIgnore, IgnorePolicy ignorePolicy) {
        boolean ignore = false;
        if (copyIgnore != null) {
            IgnorePolicy ignorePolicyOnField = copyIgnore.policy();
            if (ignorePolicyOnField != IgnorePolicy.DEFAULT) {
                ignorePolicy = ignorePolicyOnField;
            }
        }
        if (ignorePolicy != null) {
            switch (ignorePolicy) {
                case EMPTY: {
                    if (value == null) {
                        ignore = true;
                    } else if (value instanceof String) {
                        ignore = ((String) value).isEmpty();
                    } else if (value instanceof Collection) {
                        ignore = ((Collection) value).isEmpty();
                    } else if (value.getClass().isArray()) {
                        ignore = Array.getLength(value) == 0;
                    } else if (value instanceof Number) {
                        ignore = ((Number) value).equals(0);
                    }
                    break;
                }
                case NULL: {
                    if (value == null) {
                        ignore = true;
                    }
                    break;
                }
                default:
                    break;
            }
        }
        return ignore;
    }

    protected final boolean hasCondition(String[] annotationValue, String[] ignoreConditions) {
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
     * 获取缓存, 获取不到则新建
     * 
     * @param clazz 类对象
     * @return 类对象对应的缓存
     */
    protected Cache getCache(Class<?> clazz) {
        expungeStaleEntries();
        CacheReference ref = cacheMap.get(clazz);
        if (ref == null || ref.get() == null) {
            // 如果竞争锁失败，表示已经有线程正在初始化缓存
            synchronized (clazz) {
                ref = cacheMap.get(clazz);
                // 再次判断
                if (ref == null || ref.get() == null) {
                    ref = new CacheReference(newCacheFor(clazz), referenceQueue);
                    cacheMap.put(clazz, ref);
                }
            }
        }
        ref = cacheMap.get(clazz);
        return ref.get();
    }

    protected abstract Cache newCacheFor(Class<?> clazz);

    protected void expungeStaleEntries() {
        for (Object collected; (collected = referenceQueue.poll()) != null;) {
            synchronized (referenceQueue) {
                CacheReference ref = (CacheReference) collected;
                cacheMap.remove(ref.getCachedClass());
            }
        }
    }

    @Data
    protected static class Cache {

        private Class<?> clazz;

        public Cache(Class<?> clazz) {
            this.clazz = clazz;
        }

        public Class<?> getCachedClass() {
            return this.clazz;
        }

    }

    protected static final class CacheReference extends SoftReference<Cache> {

        private Class<?> clazz;

        public CacheReference(Cache cache, ReferenceQueue<Cache> referenceQueue) {
            super(cache, referenceQueue);
            this.clazz = cache.getCachedClass();
        }

        public Class<?> getCachedClass() {
            return this.clazz;
        }

    }
}
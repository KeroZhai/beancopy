package com.keroz.beancopyutils.copier;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.keroz.beancopyutils.annotation.CopyIgnore;
import com.keroz.beancopyutils.reflection.ReflectionUtils;

import lombok.Data;

public abstract class AbstractCachedCopier implements Copier {

    /**
     * 缓存
     */
    private ConcurrentHashMap<Class<?>, CacheReference> cacheMap = new ConcurrentHashMap<>();
    private ReferenceQueue<Cache> referenceQueue = new ReferenceQueue<>();

    @Override
    public <Source, Target> Target copy(Source source, Class<Target> targetClass, boolean ignoreNull,
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
            e.printStackTrace();
        }
        copy(source, target, ignoreNull, ignoreConditions);
        return target;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Source, Target> List<Target> copyList(List<Source> srcList, Class<Target> targetClass, boolean ignoreNull,
            String[] ignoreConditions) {
        List<Target> tarList = new ArrayList<>();
        if (srcList == null) {
            return null;
        }
        boolean isPrimitive = ReflectionUtils.isPrimitive(targetClass);
        for (Source src : srcList) {
            try {
                if (isPrimitive) {
                    tarList.add((Target) src);
                } else {
                    tarList.add((Target) copy(src, targetClass.newInstance(), ignoreNull, ignoreConditions));
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
                throw new com.keroz.beancopyutils.exception.InstantiationException(
                        "Instantiate class failed: " + targetClass.getName());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return tarList;
    }

    /**
     * 根据注解中的值和忽略条件确定是否忽略该属性
     * 
     * @param copyIgnore       拷贝忽略注解
     * @param ignoreConditions 忽略条件
     * @return {@code true} 或 {@code false}
     */
    protected final boolean shouldIgnore(CopyIgnore copyIgnore, String[] ignoreConditions, Object value,
            boolean ignoreNull) {
        boolean ignore = false;
        if (copyIgnore != null) {
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
        }
        return ignore || (value == null && ignoreNull);
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
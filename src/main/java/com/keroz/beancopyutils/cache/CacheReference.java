package com.keroz.beancopyutils.cache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

public class CacheReference extends SoftReference<Cache> {

    private Class<?> clazz;

    public CacheReference(Class<?> clazz, ReferenceQueue<Cache> referenceQueue) {
        super(new Cache(clazz), referenceQueue);
        this.clazz = clazz;
    }

    public Class<?> getCachedClass() {
        return this.clazz;
    }

}
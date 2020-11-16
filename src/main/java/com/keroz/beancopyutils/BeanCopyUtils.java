package com.keroz.beancopyutils;

import java.util.Collection;

import com.keroz.beancopyutils.annotation.CopyIgnore.IgnorePolicy;
import com.keroz.beancopyutils.copier.Copier;
import com.keroz.beancopyutils.copier.DefaultCopier;

/**
 *
 * 
 */
public class BeanCopyUtils {

    private static Copier copier = new DefaultCopier();

    public static <Target, Source> void copy(Source source, Target target) {
        copy(source, target, null, null);
    }

    public static <Target, Source> void copy(Source source, Target target, IgnorePolicy ignorePolicy) {
        copy(source, target, ignorePolicy, null);
    }

    static <Target, Source> void copy(Source source, Target target, String[] ignoreConditions) {
        copy(source, target, null, ignoreConditions);
    }

    public static <Target, Source> void copy(Source source, Target target, IgnorePolicy ignorePolicy,
            String[] ignoreConditions) {
        doCopy(source, target, ignorePolicy, ignoreConditions);
    }

    public static <Target, Source> Target copy(Source source, Class<Target> tarClass) {
        return copy(source, tarClass, null, null);
    }

    public static <Target, Source> Target copy(Source source, Class<Target> tarClass, IgnorePolicy ignorePolicy) {
        return copy(source, tarClass, ignorePolicy, null);
    }

    public static <Target, Source> Target copy(Source source, Class<Target> tarClass, String[] ignoreConditions) {
        return copy(source, tarClass, null, ignoreConditions);
    }

    public static <Target, Source> Target copy(Source source, Class<Target> tarClass, IgnorePolicy ignorePolicy,
            String[] ignoreConditions) {
        return doCopy(source, tarClass, ignorePolicy, ignoreConditions);
    }

    public static <SourceComponent, TargetComponent> Collection<TargetComponent> copyCollection(
            Collection<SourceComponent> sourceCollection, Class<TargetComponent> targetComponentClass) {
        return doCopyCollection(sourceCollection, targetComponentClass, null, null, null);
    }

    public static <SourceComponent, TargetComponent> Collection<TargetComponent> copyCollection(
            Collection<SourceComponent> sourceCollection, Class<TargetComponent> targetComponentClass,
            String[] ignoreConditions) {
        return doCopyCollection(sourceCollection, targetComponentClass, null, null, ignoreConditions);
    }

    public static <SourceComponent, TargetComponent> Collection<TargetComponent> copyCollection(
            Collection<SourceComponent> sourceCollection, Class<TargetComponent> targetComponentClass,
            IgnorePolicy ignorePolicy) {
        return doCopyCollection(sourceCollection, targetComponentClass, null, ignorePolicy, null);
    }

    public static <SourceComponent, TargetComponent> Collection<TargetComponent> copyCollection(
            Collection<SourceComponent> sourceCollection, Class<TargetComponent> targetComponentClass,
            IgnorePolicy ignorePolicy, String[] ignoreConditions) {
        return doCopyCollection(sourceCollection, targetComponentClass, null, ignorePolicy, ignoreConditions);
    }

    public static <SourceComponent, TargetComponent> Collection<TargetComponent> copyCollection(
            Collection<SourceComponent> sourceCollection, Class<TargetComponent> targetComponentClass,
            Class<? extends Collection<?>> collectionClass, IgnorePolicy ignorePolicy, String[] ignoreConditions) {
        return doCopyCollection(sourceCollection, targetComponentClass, collectionClass, ignorePolicy,
                ignoreConditions);
    }

    private static <Target, Source> Target doCopy(Source source, Target target, IgnorePolicy ignorePolicy,
            String[] ignoreConditions) {
        if (source == null) {
            return null;
        }
        if (target == null) {
            throw new IllegalArgumentException("Target object is null");
        }
        return copier.copy(source, target, ignorePolicy, ignoreConditions);
    }

    private static <Target, Source> Target doCopy(Source source, Class<Target> targetClass, IgnorePolicy ignorePolicy,
            String[] ignoreConditions) {
        if (source == null) {
            return null;
        }
        if (targetClass == null) {
            throw new IllegalArgumentException("Target class is null");
        }
        return copier.copy(source, targetClass, ignorePolicy, ignoreConditions);
    }

    private static <SourceComponent, TargetComponent> Collection<TargetComponent> doCopyCollection(
            Collection<SourceComponent> sourceCollection, Class<TargetComponent> targetComponentClass,
            Class<? extends Collection<?>> collectionClass, IgnorePolicy ignorePolicy, String[] ignoreConditions) {
        if (sourceCollection == null) {
            return null;
        }
        return copier.copyCollection(sourceCollection, targetComponentClass, collectionClass, ignorePolicy,
                ignoreConditions);

    }

}

package com.keroz.beancopyutils;

import java.util.Collection;
import java.util.function.Supplier;

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

    public static <Target, Source> void copy(Source source, Target target, Class<?>[] ignoreConditions) {
        copy(source, target, null, ignoreConditions);
    }

    public static <Target, Source> void copy(Source source, Target target, IgnorePolicy ignorePolicy,
            Class<?>[] ignoreConditions) {
        doCopy(source, target, ignorePolicy, ignoreConditions);
    }

    public static <Target, Source> Target copy(Source source, Class<Target> tarClass) {
        return copy(source, tarClass, null, null);
    }

    public static <Target, Source> Target copy(Source source, Class<Target> tarClass, IgnorePolicy ignorePolicy) {
        return copy(source, tarClass, ignorePolicy, null);
    }

    public static <Target, Source> Target copy(Source source, Class<Target> tarClass, Class<?>[] ignoreConditions) {
        return copy(source, tarClass, null, ignoreConditions);
    }

    public static <Target, Source> Target copy(Source source, Class<Target> tarClass, IgnorePolicy ignorePolicy,
            Class<?>[] ignoreConditions) {
        return doCopy(source, tarClass, ignorePolicy, ignoreConditions);
    }

    public static <SourceComponent, TargetComponent, SourceCollection extends Collection<SourceComponent>, TargetCollection extends Collection<TargetComponent>> TargetCollection copyCollection(
            SourceCollection sourceCollection, Class<TargetComponent> targetComponentClass) {
        return doCopyCollection(sourceCollection, targetComponentClass, null, null, null);
    }

    public static <SourceComponent, TargetComponent, SourceCollection extends Collection<SourceComponent>, TargetCollection extends Collection<TargetComponent>> TargetCollection copyCollection(
            SourceCollection sourceCollection, Class<TargetComponent> targetComponentClass,
            Class<?>[] ignoreConditions) {
        return doCopyCollection(sourceCollection, targetComponentClass, null, null, ignoreConditions);
    }

    public static <SourceComponent, TargetComponent, SourceCollection extends Collection<SourceComponent>, TargetCollection extends Collection<TargetComponent>> TargetCollection copyCollection(
            SourceCollection sourceCollection, Class<TargetComponent> targetComponentClass,
            IgnorePolicy ignorePolicy) {
        return doCopyCollection(sourceCollection, targetComponentClass, null, ignorePolicy, null);
    }

    public static <SourceComponent, TargetComponent, SourceCollection extends Collection<SourceComponent>, TargetCollection extends Collection<TargetComponent>> TargetCollection copyCollection(
            SourceCollection sourceCollection, Class<TargetComponent> targetComponentClass,
            IgnorePolicy ignorePolicy, Class<?>[] ignoreConditions) {
        return doCopyCollection(sourceCollection, targetComponentClass, null, ignorePolicy, ignoreConditions);
    }

    public static <SourceComponent, TargetComponent, SourceCollection extends Collection<SourceComponent>, TargetCollection extends Collection<TargetComponent>> TargetCollection copyCollection(
            SourceCollection sourceCollection, Class<TargetComponent> targetComponentClass,
            Supplier<TargetCollection> supplier) {
        return doCopyCollection(sourceCollection, targetComponentClass, supplier, null, null);
    }

    public static <SourceComponent, TargetComponent, SourceCollection extends Collection<SourceComponent>, TargetCollection extends Collection<TargetComponent>> TargetCollection copyCollection(
            SourceCollection sourceCollection, Class<TargetComponent> targetComponentClass,
            Supplier<TargetCollection> supplier,
            Class<?>[] ignoreConditions) {
        return doCopyCollection(sourceCollection, targetComponentClass, supplier, null, ignoreConditions);
    }

    public static <SourceComponent, TargetComponent, SourceCollection extends Collection<SourceComponent>, TargetCollection extends Collection<TargetComponent>> TargetCollection copyCollection(
            SourceCollection sourceCollection, Class<TargetComponent> targetComponentClass,
            Supplier<TargetCollection> supplier,
            IgnorePolicy ignorePolicy) {
        return doCopyCollection(sourceCollection, targetComponentClass, supplier, ignorePolicy, null);
    }

    public static <SourceComponent, TargetComponent, SourceCollection extends Collection<SourceComponent>, TargetCollection extends Collection<TargetComponent>> TargetCollection copyCollection(
            SourceCollection sourceCollection, Class<TargetComponent> targetComponentClass,
            Supplier<TargetCollection> supplier, IgnorePolicy ignorePolicy, Class<?>[] ignoreConditions) {
        return (TargetCollection) doCopyCollection(sourceCollection, targetComponentClass, supplier, ignorePolicy,
                ignoreConditions);
    }

    private static <Target, Source> Target doCopy(Source source, Target target, IgnorePolicy ignorePolicy,
            Class<?>[] ignoreConditions) {
        if (source == null) {
            return null;
        }
        if (target == null) {
            throw new IllegalArgumentException("Target object is null");
        }
        return copier.copy(source, target, ignorePolicy, ignoreConditions);
    }

    private static <Target, Source> Target doCopy(Source source, Class<Target> targetClass, IgnorePolicy ignorePolicy,
            Class<?>[] ignoreConditions) {
        if (source == null) {
            return null;
        }
        if (targetClass == null) {
            throw new IllegalArgumentException("Target class is null");
        }
        return copier.copy(source, targetClass, ignorePolicy, ignoreConditions);
    }

    private static <SourceComponent, TargetComponent, SourceCollection extends Collection<SourceComponent>, TargetCollection extends Collection<TargetComponent>> TargetCollection doCopyCollection(
            SourceCollection sourceCollection, Class<TargetComponent> targetComponentClass,
            Supplier<TargetCollection> supplier, IgnorePolicy ignorePolicy, Class<?>[] ignoreConditions) {
        if (sourceCollection == null) {
            return null;
        }
        return copier.copyCollection(sourceCollection, targetComponentClass, supplier, ignorePolicy,
                ignoreConditions);

    }
}

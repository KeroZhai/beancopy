package com.keroz.beancopyutils.copier;

import java.util.Collection;
import java.util.function.Supplier;

import com.keroz.beancopyutils.annotation.CopyIgnore.IgnorePolicy;

public interface Copier {

    <Source, Target> Target copy(Source source, Class<Target> targetClass, IgnorePolicy ignorePolicy,
            Class<?>[] ignoreConditions);

    <Source, Target> Target copy(Source source, Target target, IgnorePolicy ignorePolicy,
            Class<?>[] ignoreConditions);

    Object copyArray(Object sourceArray, Class<?> targetComponentClass, IgnorePolicy ignorePolicy,
            Class<?>[] ignoreConditions);

    <SourceComponent, TargetComponent, SourceCollection extends Collection<SourceComponent>, TargetCollection extends Collection<TargetComponent>> TargetCollection copyCollection(
            SourceCollection sourceCollection, Class<TargetComponent> targetComponentClass,
            Supplier<TargetCollection> supplier,
            IgnorePolicy ignorePolicy, Class<?>[] ignoreConditions);
}

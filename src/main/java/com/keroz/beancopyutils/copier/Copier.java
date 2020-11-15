package com.keroz.beancopyutils.copier;

import java.util.Collection;

import com.keroz.beancopyutils.annotation.CopyIgnore.IgnorePolicy;

public interface Copier {

        <Source, Target> Target copy(Source source, Class<Target> targetClass, IgnorePolicy ignorePolicy,
                        String[] ignoreConditions);

        <Source, Target> Target copy(Source source, Target target, IgnorePolicy ignorePolicy,
                        String[] ignoreConditions);

        Object copyArray(Object sourceArray, Class<?> targetComponentClass, IgnorePolicy ignorePolicy,
                        String[] ignoreConditions);

        <SourceComponent, TargetComponent> Collection<TargetComponent> copyCollection(
                        Collection<SourceComponent> sourceCollection, Class<TargetComponent> targetComponentClass,
                        IgnorePolicy ignorePolicy, String[] ignoreConditions);
}
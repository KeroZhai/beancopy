package com.keroz.beancopyutils.copier;

import java.util.List;

import com.keroz.beancopyutils.annotation.CopyIgnore.IgnorePolicy;

public interface Copier {

    <Source, Target> Target copy(Source source, Class<Target> targetClass, IgnorePolicy ignorePolicy, String[] ignoreConditions);

    <Source, Target> Target copy(Source source, Target target, IgnorePolicy ignorePolicy, String[] ignoreConditions);

    <Source, Target> List<Target> copyList(List<Source> srcList, Class<Target> targetClass, IgnorePolicy ignorePolicy, String[] ignoreConditions);
}
package com.keroz.beancopyutils.copier;

import java.util.List;

public interface Copier {

    <Source, Target> Target copy(Source source, Class<Target> targetClass, boolean ignoreNull, String[] ignoreConditions);

    <Source, Target> Target copy(Source source, Target target, boolean ignoreNull, String[] ignoreConditions);

    <Source, Target> List<Target> copyList(List<Source> srcList, Class<Target> targetClass, boolean ignoreNull, String[] ignoreConditions);
}
package com.keroz.morphling.codegenerator;

import java.lang.reflect.Type;

import com.keroz.morphling.util.ReflectionUtils;

public class NestedTypeConversionCodeGenerator implements ConversionCodeGenerator {

    @Override
    public boolean isSupported(Type sourceType, Type targetType) {
        if (ReflectionUtils.isSimpleType(sourceType) || ReflectionUtils.isSimpleType(targetType)
                || ReflectionUtils.isCollectionType(sourceType) || ReflectionUtils.isCollectionType(targetType)
                || ReflectionUtils.isMapType(sourceType) || ReflectionUtils.isMapType(targetType)) {
            return false;
        }

        return true;
    }

    @Override
    public String generate(Type sourceType, Type targetType) {
        String sourceTypeName = sourceType.getTypeName();
        String targetTypeName = targetType.getTypeName();

        return "Mapper mapper = mapperFactory.getMapperFor(" + sourceTypeName + ".class, "
                + targetTypeName + ".class);tv = (" + targetTypeName + ") mapper.map(sv);";
    }

}

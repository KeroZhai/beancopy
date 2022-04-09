package com.keroz.morphling.codegenerator;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.keroz.morphling.exception.TypeMismatchException;
import com.keroz.morphling.util.ReflectionUtils;

public class CollectionTypeConversionCodeGenerator implements ConversionCodeGenerator {

    @Override
    public boolean isSupported(Type sourceType, Type targetType) {
        if (sourceType instanceof ParameterizedType && targetType instanceof ParameterizedType) {
            ParameterizedType sourceParameterizedType = (ParameterizedType) sourceType;
            ParameterizedType targetParameterizedType = (ParameterizedType) targetType;

            // must be of Collection type
            if (ReflectionUtils.isCollectionType((Class<?>) sourceParameterizedType.getRawType())
                    && ReflectionUtils.isCollectionType((Class<?>) targetParameterizedType.getRawType())) {
                // components must be of simple type
                return sourceParameterizedType.getActualTypeArguments()[0] instanceof Class
                        && targetParameterizedType.getActualTypeArguments()[0] instanceof Class;
            }
        }

        return false;
    }

    @Override
    public String generate(Type sourceType, Type targetType) {
        ParameterizedType sourceParameterizedType = (ParameterizedType) sourceType;
        ParameterizedType targetParameterizedType = (ParameterizedType) targetType;
        Class<?> sourceTypeClass = (Class<?>) sourceParameterizedType.getRawType();
        Class<?> targetTypeClass = (Class<?>) targetParameterizedType.getRawType();
        Class<?> sourceComponentType = (Class<?>) sourceParameterizedType.getActualTypeArguments()[0];
        Class<?> targetComponentType = (Class<?>) targetParameterizedType.getActualTypeArguments()[0];
        StringBuilder builder = new StringBuilder();

        builder.append("tv = null;");

        builder
        .append("if (sv != null) {")
        .append("Class sourceCollectionType = sv.getClass();");

        if (targetTypeClass.isInterface()) {
            if (targetTypeClass.isAssignableFrom(sourceTypeClass)) {
                builder.append("Class targetCollectionType = sourceCollectionType;");
            } else {
                // throw
            }
        } else {
            builder.append("Class targetCollectionType = ").append(targetTypeClass.getName()).append(".class;");
        }

        builder
        .append("try { tv = (").append(targetTypeClass.getName()).append(") targetCollectionType.newInstance();")
        .append("if (!sv.isEmpty()) {");

        if (ReflectionUtils.isSimpleType(sourceComponentType) && ReflectionUtils.isSimpleType(targetComponentType)) {
            if (sourceComponentType.equals(targetComponentType)) {
                builder.append("for (int i = 0; i < sv.size(); i++) { tv.add(sv.get(i)); }");
            } else {
                throw new TypeMismatchException("");
            }
        } else if (!ReflectionUtils.isSimpleType(sourceComponentType) && !ReflectionUtils.isSimpleType(targetComponentType)) {
            builder
            .append("Mapper mapper = mapperFactory.getMapperFor(")
            .append(sourceComponentType.getName())
            .append(".class").append(",")
            .append(targetComponentType.getName())
            .append(".class").append(");")
            .append("for (int i = 0; i < sv.size(); i++) { tv.add(mapper.map(sv.get(i))); }");
        }

        builder.append("}} catch (InstantiationException ignored) {} catch (IllegalAccessException ignored) {}}");

        return builder.toString();
    }

}

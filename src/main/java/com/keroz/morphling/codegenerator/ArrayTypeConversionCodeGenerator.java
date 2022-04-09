package com.keroz.morphling.codegenerator;

import java.lang.reflect.Type;

import com.keroz.morphling.util.ReflectionUtils;

public class ArrayTypeConversionCodeGenerator implements ConversionCodeGenerator {

    @Override
    public boolean isSupported(Type sourceType, Type targetType) {
        return (sourceType instanceof Class && targetType instanceof Class)
                && (((Class<?>) sourceType).isArray() && ((Class<?>) targetType).isArray());
    }

    @Override
    public String generate(Type sourceType, Type targetType) {
        Class<?> sourceClassType = (Class<?>) sourceType;
        Class<?> targetClassType = (Class<?>) targetType;
        Class<?> sourceComponentType = sourceClassType.getComponentType();
        Class<?> targetComponentType = targetClassType.getComponentType();
        StringBuilder builder = new StringBuilder();

        String name = targetType.getTypeName();
        int indexOfLeftBracket = name.indexOf("[");
        String simpleName = name.substring(0, indexOfLeftBracket);
        int dimensions = name.substring(indexOfLeftBracket).length() / 2;

        if (dimensions > 1) {
            // multi-dimension not supported yet
            return null;
        }

        builder
                .append("tv = null;") // important
                .append("if (sv != null) {")
                .append("int length = sv.length;")
                .append("tv = new ").append(simpleName).append("[length];");

        boolean sourceComponentTypeSimple = ReflectionUtils.isSimpleType(sourceComponentType);
        boolean targetComponentTypeSimple = ReflectionUtils.isSimpleType(targetComponentType);

        if (sourceComponentTypeSimple && targetComponentTypeSimple) {
            // both are of simple type
            builder.append("for (int i = 0; i < length; i++) {")
                    // use reflection to avoid manually boxing and unboxing
                    .append("java.lang.reflect.Array.set(tv, i, java.lang.reflect.Array.get(sv, i));")
                    .append("}");
        } else if (!sourceComponentTypeSimple && !targetComponentTypeSimple) {
            if (ReflectionUtils.isCollectionType(sourceComponentType)
                    || ReflectionUtils.isCollectionType(targetComponentType)
                    || ReflectionUtils.isMapType(sourceComponentType)
                    || ReflectionUtils.isMapType(targetComponentType)) {
                // array of Collection or Map is not supported yet
                return null;
            }
            // bean type
            builder.append("Mapper mapper = mapperFactory.getMapperFor(")
                    .append(sourceComponentType.getName())
                    .append(".class").append(",")
                    .append(targetComponentType.getName())
                    .append(".class").append(");")
                    .append("for (int i = 0; i < length; i++) {")
                    .append("java.lang.reflect.Array.set(tv, i, mapper.map(java.lang.reflect.Array.get(sv, i)));")
                    .append("}");
        } else {
            return null;
        }
        builder.append("}");

        return builder.toString();

        // bodyBuilder
        // .append("{Object sourceArray = $1.get").append(capitalize(field.getName()))
        // .append("();\n")
        // .append("int length = java.lang.reflect.Array.getLength(sourceArray);\n")
        // .append("Object targetArray = java.lang.reflect.Array.newInstance(")
        // .append(targetComponentType.getName()).append(".class, length);\n")
        // .append("try {\n");

        // if (sourceComponentType.isPrimitive() || targetComponentType.isPrimitive()) {
        // bodyBuilder.append("for (int i = 0; i < length; i++) {\n")
        // .append("java.lang.reflect.Array.set(targetArray, i,
        // java.lang.reflect.Array.get(sourceArray, i));\n")
        // .append("}");
        // } else {
        // bodyBuilder.append("com.keroz.morphling.mapper.Mapper mapper = ")
        // .append(MapperFactory.class.getName())
        // .append(".getMapperFor(")
        // .append(sourceComponentType.getName().replace("$", "."))
        // .append(".class").append(",")
        // .append(targetComponentType.getName().replace("$", "."))
        // .append(".class").append(");\n")
        // .append("for (int i = 0; i < length; i++) {\n")
        // .append("java.lang.reflect.Array.set(targetArray, i,
        // mapper.map(java.lang.reflect.Array.get(sourceArray, i)));\n")
        // .append("}\n");
        // }

        // bodyBuilder.append("} catch (IllegalArgumentException ignored) {}\n")
        // .append("target.set").append(capitalize(field.getName())).append("((")
        // .append(targetComponentType.getName().replace("$", "."))
        // .append("[]) targetArray);}\n");
    }

}

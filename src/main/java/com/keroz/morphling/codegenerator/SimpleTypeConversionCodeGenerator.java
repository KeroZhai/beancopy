package com.keroz.morphling.codegenerator;

import java.lang.reflect.Type;
import java.util.Date;

import com.keroz.morphling.exception.TypeMismatchException;
import com.keroz.morphling.util.ReflectionUtils;

/**
 * Generates conversion code between simple types.
 * <p>
 * Primitive types(and their corresponding wrapper types), enum type,
 * {@code String} type and {@code java.util.Date} type are considered as simple
 * types.
 */
public class SimpleTypeConversionCodeGenerator implements ConversionCodeGenerator {

    @Override
    public boolean isSupported(Type sourceType, Type targetType) {
        return isSimpleType(sourceType) && isSimpleType(targetType);
    }

    @Override
    public String generate(Type sourceType, Type targetType) {
        Class<?> sourceClassType = (Class<?>) sourceType;
        Class<?> targetClassType = (Class<?>) targetType;
        boolean sourceTypePrimitive = sourceClassType.isPrimitive();
        boolean targetTypePrimitive = targetClassType.isPrimitive();

        if (sourceTypePrimitive && !targetTypePrimitive) {
            if (ReflectionUtils.toWrapper(sourceClassType).equals(targetType)) {
                return "tv = " + targetClassType.getName() + ".valueOf(sv);";
            } else {
                throw new TypeMismatchException("");
            }
        } else if (!sourceTypePrimitive && targetTypePrimitive) {
            if (ReflectionUtils.toWrapper(targetClassType).equals(sourceType)) {
                return "tv = sv." + targetClassType.getName() + "Value();";
            } else {
                throw new TypeMismatchException("");
            }
        } else {
            return "tv = sv;";
        }
    }

    private boolean isSimpleType(Type type) {
        if (type instanceof Class) {
            Class<?> classType = (Class<?>) type;
            return ReflectionUtils.isPrimitiveOrWrapperType(classType) || classType.isEnum()
                    || String.class.equals(classType) || Date.class.equals(classType);
        } else {
            return false;
        }
    }

    public static class ConvertedValue {

    }

}

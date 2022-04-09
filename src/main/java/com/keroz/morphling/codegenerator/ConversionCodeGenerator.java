package com.keroz.morphling.codegenerator;

import java.lang.reflect.Type;

/**
 * Generate conversion code from source type to target type.
 * <p>
 * {@code ConversionCodeGenerator}s are used by the
 * {@link com.keroz.morphling.mapper.MapperFactory MapperFactory}. Without a
 * {@code ConversionCodeGenerator}, the {@code MapperFactory} is not able to map
 * a source object to a target object, since it does not know how to convert a
 * field value into another, even when they're of the same type. This is kinda
 * like obtaining abilities.
 * <p>
 * Since Morphling uses Javassist internally, implementing a custom
 * {@code ConversionCodeGenerator} may requires familiarity with it.
 *
 * @see SimpleTypeConversionCodeGenerator
 */
public interface ConversionCodeGenerator {

    /**
     * Whether the conversion from the given {@code sourceType} to
     * {@code targetType} is supported or not.
     *
     * @param sourceType
     * @param targetType
     * @return {@code true} or {@code false}
     */
    boolean isSupported(Type sourceType, Type targetType);

    /**
     * Generates the conversion code.
     * <p>
     * The source value and target value can be accessed with pre-defined variables
     * {@code sv} and {@code tv}.
     * <p>
     * For example, the following code will assign the value of source to
     * target:
     *
     * <pre>
     * <code>
     * tv = sv;
     * </code>
     * </pre>
     *
     * @param sourceType
     * @param targetType
     * @return the generated code.
     */
    String generate(Type sourceType, Type targetType);

}

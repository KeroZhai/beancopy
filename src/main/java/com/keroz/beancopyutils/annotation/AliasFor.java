package com.keroz.beancopyutils.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated field is an <i>alias</i> for the
 * <strong>source</strong> field, which means it will be considered to have the
 * same {@link java.lang.reflect.Field#getName() name} as the latter during
 * copying. However, if they are of different types, a converter class might be
 * needed.
 * 
 * @see Converter
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AliasFor {

    String value();
}
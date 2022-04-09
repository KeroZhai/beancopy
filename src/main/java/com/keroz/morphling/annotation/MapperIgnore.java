package com.keroz.morphling.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotated field will always be ignored during mapping by default. However,
 * you can specify a policy to change the behavior.
 *
 * @see IgnorePolicy#NULL
 * @see IgnorePolicy#EMPTY
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MapperIgnore {

    IgnorePolicy policy() default IgnorePolicy.DEFAULT;

    enum IgnorePolicy {
        /**
         * Ignore the field if source value is {@code null}.
         */
        NULL,
        /**
         * Ignore the field if source value is empty.
         */
        EMPTY,
        /**
         * Default.
         */
        DEFAULT;
    }
}

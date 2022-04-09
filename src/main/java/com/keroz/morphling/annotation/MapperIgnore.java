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
 * @see Policy#IGNORE_NULL
 * @see Policy#IGNORE_EMPTY
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MapperIgnore {

    Class<? extends Group>[] groups() default {};

    Policy policy() default Policy.DEFAULT;

    enum Policy {
        /**
         * Ignore the field if source value is {@code null}.
         */
        IGNORE_NULL,
        /**
         * Ignore the field if source value is empty.
         */
        IGNORE_EMPTY,
        /**
         * Default.
         */
        DEFAULT;
    }

    interface Group {}

    interface Included extends Group {}

    interface Excluded extends Group {}

}

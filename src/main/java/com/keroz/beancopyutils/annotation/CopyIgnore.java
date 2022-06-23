package com.keroz.beancopyutils.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates whether the annotated field in a <strong>target</strong> class
 * should be ignored (during copying). A {@link IgnorePolicy} can also be
 * specified.
 * <p>
 * At least one attribute must be provided, otherwise this annotation will have
 * no effect. Futhermore, it can even be misleading, since a field annotated
 * with {@code CopyIgnore} <i>may</i> never be ignored during copying.
 * <p>
 * The example below shows how to use this annotation.
 *
 * <pre>
 * <code>
 * public class Foo {
 *     public static interface ExcludingId {
 *     }
 *
 *     public static interface IncludingName {
 *     }
 *
 *     {@literal @}CopyIgnore(defaultIgnored = false, exceptionGroups = ExcludingId.class)
 *     public int id;
 *     {@literal @}CopyIgnore(exceptionGroups = IncludingName.class)
 *     public String name;
 * }
 * </code>
 * </pre>
 *
 * The two interfaces serve as groups, which can be specified
 * by providing an array when copying.
 *
 * <pre>
 * <code>
 * Foo source = new Foo();
 * source.id = 1;
 * source.name = "foo";
 * Foo target0 = BeanCopyUtils.copy(source, Foo.class);
 * Foo target1 = BeanCopyUtils.copy(source, Foo.class, new Class<?>[] { Foo.ExcludingId.class });
 * Foo target2 = BeanCopyUtils.copy(source, Foo.class, new Class<?>[] { Foo.IncludingName.class });
 * Foo target2 = BeanCopyUtils.copy(source, Foo.class, new Class<?>[] { Foo.ExcludingId.class, Foo.IncludingName.class });
 * </code>
 * </pre>
 *
 * And the result comes out as below.
 *
 * <ul>
 * <li>target1: id = 1, name = null</li>
 * <li>target1: id = 0, name = null</li>
 * <li>target2: id = 1, name = "foo"</li>
 * <li>target3: id = 0, name = "foo"</li>
 * </ul>
 *
 * @see IgnorePolicy
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CopyIgnore {

    /**
     *
     * @deprecated Use {@code defaultIgnored} and {@code exceptionGroups} instead.
     */
    @Deprecated
    Class<?>[] when() default {};

    /**
     *
     * @deprecated Use {@code defaultIgnored} and {@code exceptionGroups} instead.
     */
    @Deprecated
    Class<?>[] except() default {};

    boolean defaultIgnored() default true;

    Class<?>[] exceptionGroups() default {};

    IgnorePolicy policy() default IgnorePolicy.DEFAULT;

    String supplierMethod() default "";

    /**
     * Determines whether null or empty values should be ignored or not.
     */
    enum IgnorePolicy {
        /**
         * Ignore null values.
         */
        NULL,
        /**
         * Ignore empty values.
         * <p>
         * An empty string, array, collection or zero is all considered as an empty
         * value, including null.
         */
        EMPTY,
        /**
         * Don't ignore empty values.
         */
        NONE,
        /**
         * Default value for the policy attribute in {@code CopyIgnore}, which means let
         * copy methods specify the {@code IgnorePolicy}.
         */
        DEFAULT;
    }
}

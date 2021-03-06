package com.keroz.beancopyutils.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates {@code when} or {@code except} a certain condition is met that the
 * annotated field in a <strong>target</strong> class should be ignored (during
 * copying). A {@linkplain IgnorePolicy} can also be specified.
 * <p>
 * At least one attribute must be provided, otherwise this annotation will have
 * no effect. Futhermore, it can even be misleading, since a field annotated
 * with {@code CopyIgnore} <i>may</i> never be ignored during copying.
 * <p>
 * The example below shows how to use this annotation.
 * 
 * <pre>
 * {@code
 * public class Foo {
 *     public static final String COPY_WITHOUT_ID = "copyWithoutID";
 *     public static final String COPY_WITH_NAME = "copyWithName";
 * 
 *     {@literal @}CopyIgnore(when = COPY_WITHOUT_ID)
 *     public int id;
 *     {@literal @}CopyIgnore(except = COPY_WITH_NAME)
 *     public String name;
 * }}
 * </pre>
 * 
 * The two {@code String} constants serve as conditions, which can be specified
 * by providing an array when copying.
 * 
 * <pre>
 * {@code
 *     Foo source = new Foo();
 *     source.id = 1;
 *     source.name = "foo";
 *     Foo target0 = BeanCopyUtils.copy(source, Foo.class);
 *     Foo target1 = BeanCopyUtils.copy(source, Foo.class, new String[] { Foo.COPY_WITHOUT_ID });
 *     Foo target2 = BeanCopyUtils.copy(source, Foo.class, new String[] { Foo.COPY_WITH_NAME });
 *     Foo target2 = BeanCopyUtils.copy(source, Foo.class, new String[] { Foo.COPY_WITHOUT_ID, Foo.COPY_WITH_NAME });
 * }
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
 * It is recommanded to declare {@code String} condtions as
 * {@code public static} so that they will be ignored. It's also a good practice
 * to make them unique, otherwise, the result may not meet expectations.
 * 
 * @see IgnorePolicy
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CopyIgnore {

    String[] when() default "";

    String[] except() default "";

    IgnorePolicy policy() default IgnorePolicy.DEFAULT;

    /**
     * Determines whether null or empty values should be ignored or not.
     */
    public static enum IgnorePolicy {
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
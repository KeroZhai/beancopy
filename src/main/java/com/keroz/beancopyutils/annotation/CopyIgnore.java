package com.keroz.beancopyutils.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标明<strong>目标对象中</strong>被注解的字段在拷贝时需要被忽略。
 * <p>
 * {@code when}用以指明在什么{@link IgnoreCondition
 * 条件}下忽略该字段，{@code except}用以指明在什么{@link IgnoreCondition 条件}下不忽略此字段。
 * <p>
 * 例如，有如下类：
 * 
 * <pre>
 * {@code
 * public class Foo {
 *     {@literal @}IgnoreCondition
 *     public static final String COPY_WITHOUT_ID = "copyWithoutID";
 *     {@literal @}IgnoreCondition
 *     public static final String COPY_WITH_NAME = "copyWithName";
 * 
 *     {@literal @}CopyIgnore(when = COPY_WITHOUT_ID)
 *     public int id;
 *     {@literal @}CopyIgnore(except = COPY_WITH_NAME)
 *     public String name;
 * }}
 * </pre>
 * 
 * 其中定义了两个忽略条件，一个表示不拷贝字段{@code id}，另一个表示拷贝字段{@code name}。在两个字段上的{@code
 * CopyIgnore}注解如同字面意思，即当不拷贝字段{@code id}时，忽略{@code id}字段，除了拷贝字段{@code
 * name}时，都忽略字段{@code name}。使用代码表示，则如：
 * 
 * <pre>
 * {@code
 * Foo source = new Foo();
 * source.id = 1;
 * source.name = "foo";
 * Foo target0 = BeanCopyUtils.copy(source, Foo.class);
 * Foo target1 = BeanCopyUtils.copy(source, Foo.class, new String[] { Foo.COPY_WITHOUT_ID });
 * Foo target2 = BeanCopyUtils.copy(source, Foo.class, new String[] { Foo.COPY_WITH_NAME });
 * Foo target2 = BeanCopyUtils.copy(source, Foo.class, new String[] { Foo.COPY_WITHOUT_ID, Foo.COPY_WITH_NAME });
 * }
 * </pre>
 * 
 * 结果将会如下：
 * <ul>
 * <li>target1: id = 1, name = null</li>
 * <li>target1: id = 0, name = null</li>
 * <li>target2: id = 1, name = "foo"</li>
 * <li>target3: id = 0, name = "foo"</li>
 * </ul>
 * 
 * @see IgnoreCondition
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CopyIgnore {

    String[] when() default "";

    String[] except() default "";

}
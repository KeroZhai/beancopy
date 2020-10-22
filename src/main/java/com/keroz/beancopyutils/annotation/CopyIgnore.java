package com.keroz.beancopyutils.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标明<strong>目标对象中</strong>被注解的字段在拷贝时需要被忽略。
 * <p>
 * {@code when}用以指明在什么{@link IgnoreCondition 条件}下忽略该字段，{@code except}用以指明在什么{@link IgnoreCondition 条件}下不忽略此字段。
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
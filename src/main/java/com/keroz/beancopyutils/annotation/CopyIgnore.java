package com.keroz.beancopyutils.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 设置拷贝对象时忽略的属性 
 * <p>
 *     <code>when</code> 表示何时忽略, <code>except</code> 表示何时不忽略, 为空则默认忽略
 * </p>
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CopyIgnore {

    String[] when() default  "";

    String[] except() default "";
    
}
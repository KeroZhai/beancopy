package com.keroz.beancopyutils.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识被注解的字段是定义的拷贝忽略条件，此字段在拷贝时会被默认忽略。
 * <p>拷贝忽略条件必须为字符串类型，例如：
 * 
 * <pre> {@code 
 * @IgnoreCondition
 * public static final String COPY_NAME_ONLY = "copyNameOnly";}
 * </pre>
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreCondition {
    
}
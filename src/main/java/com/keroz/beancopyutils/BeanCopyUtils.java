package com.keroz.beancopyutils;

import java.util.List;

import com.keroz.beancopyutils.annotation.CopyIgnore;
import com.keroz.beancopyutils.annotation.CopyIgnore.IgnorePolicy;
import com.keroz.beancopyutils.copier.Copier;
import com.keroz.beancopyutils.copier.DefaultCopier;

/**
 * Bean 拷贝工具, 集合属性只支持{@code List}
 * <p>
 * 提供了拷贝单个对象和{@code List}的若干个静态重载方法，搭配{@link CopyIgnore}以实现多种灵活的拷贝方式。
 * 
 */
public class BeanCopyUtils {

    private static Copier copier = new DefaultCopier();

    /**
     * @param <Source> 源对象类型
     * @param <Target> 目标对象类型
     * @param source   源对象
     * @param target   目标对象
     */
    public static <Target, Source> void copy(Source source, Target target) {
        copy(source, target, null, null);
    }

    /**
     * @param <Source>   源对象类型
     * @param <Target>   目标对象类型
     * @param source     源对象
     * @param target     目标对象
     * @param ignoreNull 是否忽略 null 值
     * 
     * @see CopyIgnore
     */
    public static <Target, Source> void copy(Source source, Target target, IgnorePolicy ignorePolicy) {
        copy(source, target, ignorePolicy, null);
    }

    /**
     * @param <Source>         源对象类型
     * @param <Target>         目标对象类型
     * @param source           源对象
     * @param target           目标对象
     * @param ignoreConditions 拷贝忽略条件
     * 
     * @see CopyIgnore
     */
    public static <Target, Source> void copy(Source source, Target target, String[] ignoreConditions) {
        copy(source, target, null, ignoreConditions);
    }

    /**
     * @param <Source>         源对象类型
     * @param <Target>         目标对象类型
     * @param source           源对象
     * @param target           目标对象
     * @param ignoreNull       是否忽略null值
     * @param ignoreConditions 拷贝忽略条件
     * 
     * @see CopyIgnore
     */
    public static <Target, Source> void copy(Source source, Target target, IgnorePolicy ignorePolicy,
            String[] ignoreConditions) {
        doCopy(source, target, ignorePolicy, ignoreConditions);
    }

    /**
     * @param <Source> 源对象类型
     * @param <Target> 目标对象类型
     * @param source   源对象
     * @param tarClass 目标对象类对象
     * @return 目标对象
     */
    public static <Target, Source> Target copy(Source source, Class<Target> tarClass) {
        return copy(source, tarClass, null, null);
    }

    /**
     * @param <Source>   源对象类型
     * @param <Target>   目标对象类型
     * @param source     源对象
     * @param tarClass   目标对象类对象
     * @param ignoreNull 是否忽略null值
     * @return 目标对象
     */
    public static <Target, Source> Target copy(Source source, Class<Target> tarClass, IgnorePolicy ignorePolicy) {
        return copy(source, tarClass, ignorePolicy, null);
    }

    /**
     * @param <Source>         源对象类型
     * @param <Target>         目标对象类型
     * @param source           源对象
     * @param tarClass         目标对象类对象
     * @param ignoreConditions 拷贝忽略条件
     * @return 目标对象
     */
    public static <Target, Source> Target copy(Source source, Class<Target> tarClass, String[] ignoreConditions) {
        return copy(source, tarClass, null, ignoreConditions);
    }

    /**
     * @param <Source>         源对象类型
     * @param <Target>         目标对象类型
     * @param source           源对象
     * @param tarClass         目标对象类对象
     * @param ignoreConditions 拷贝忽略条件
     * @param ignoreNull       是否忽略null值
     * @return 目标对象
     */
    public static <Target, Source> Target copy(Source source, Class<Target> tarClass, IgnorePolicy ignorePolicy,
            String[] ignoreConditions) {
        return doCopy(source, tarClass, ignorePolicy, ignoreConditions);
    }

    /**
     * @param <Source> 源对象类型
     * @param <Target> 目标对象类型
     * @param srcList  源对象List
     * @param tarClass 目标对象类对象
     * @return 目标对象List
     */
    public static <Target, Source> List<Target> copyList(List<Source> srcList, Class<Target> tarClass) {
        return doCopyList(srcList, tarClass, null, null);
    }

    /**
     * @param <Source>         源对象类型
     * @param <Target>         目标对象类型
     * @param srcList          源对象List
     * @param tarClass         目标对象类对象
     * @param ignoreConditions 拷贝忽略条件
     * @return 目标对象List
     */
    public static <Target, Source> List<Target> copyList(List<Source> srcList, Class<Target> tarClass,
            String[] ignoreConditions) {
        return doCopyList(srcList, tarClass, null, ignoreConditions);
    }

    /**
     * @param <Source>   源对象类型
     * @param <Target>   目标对象类型
     * @param srcList    源对象List
     * @param tarClass   目标对象类对象
     * @param ignoreNull 是否忽略null值
     * @return 目标对象List
     */
    public static <Target, Source> List<Target> copyList(List<Source> srcList, Class<Target> tarClass,
            IgnorePolicy ignorePolicy) {
        return doCopyList(srcList, tarClass, null, null);
    }

    /**
     * 执行实际的拷贝过程
     * 
     * @param <Source>         源对象类型
     * @param <Target>         目标对象类型
     * @param source
     * @param target
     * @param ignoreNull
     * @param ignoreConditions
     * @return
     */
    private static <Target, Source> Target doCopy(Source source, Target target, IgnorePolicy ignorePolicy,
            String[] ignoreConditions) {
        if (source == null) {
            return null;
        }
        if (target == null) {
            throw new IllegalArgumentException("Target object is null");
        }
        return copier.copy(source, target, ignorePolicy, ignoreConditions);
    }

    private static <Target, Source> Target doCopy(Source source, Class<Target> targetClass, IgnorePolicy ignorePolicy,
            String[] ignoreConditions) {
        if (source == null) {
            return null;
        }
        if (targetClass == null) {
            throw new IllegalArgumentException("Target class is null");
        }
        return copier.copy(source, targetClass, ignorePolicy, ignoreConditions);
    }

    /**
     * @param <Source>         源对象类型
     * @param <Target>         目标对象类型
     * @param srcList          源对象List
     * @param tarClass         目标对象类对象
     * @param ignoreConditions 拷贝忽略条件
     * @param ignoreNull       是否忽略null值
     * @return 目标对象List
     */
    public static <Target, Source> List<Target> doCopyList(List<Source> srcList, Class<Target> tarClass,
            IgnorePolicy ignorePolicy, String[] ignoreConditions) {
        if (srcList == null) {
            return null;
        }
        if (tarClass == null) {
            throw new IllegalArgumentException("Target class is null");
        }
        return copier.copyList(srcList, tarClass, ignorePolicy, ignoreConditions);
    }

}

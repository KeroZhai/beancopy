package com.keroz.beancopyutils.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

import com.keroz.beancopyutils.annotation.AliasFor;
import com.keroz.beancopyutils.converter.Converter;

public class ExtendedField {

    private Field field;
    private String aliasFor;
    private Class<? extends Converter<?, ?>> converterClass;


    public ExtendedField(Field field) {
        this.field = field;
        this.aliasFor = internalGetAliasFor();
        this.converterClass = internalGetConverterClass();
    }

    private String internalGetAliasFor() {
        AliasFor aliasFor = field.getDeclaredAnnotation(AliasFor.class);
        if (aliasFor != null) {
            return aliasFor.value();
        }
        return field.getName();
    }

    private Class<? extends Converter<?, ?>> internalGetConverterClass() {
        com.keroz.beancopyutils.annotation.Converter converter = field.getDeclaredAnnotation(com.keroz.beancopyutils.annotation.Converter.class);
        if (converter != null) {
            return converter.value();
        }
        return null;
    }

    
    public String getName() {
        return this.field.getName();
    }

    public Class<?> getType() {
        return this.field.getType();
    }

    public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        return this.field.getDeclaredAnnotation(annotationClass);
    }

    public void setAccessible(boolean flag) {
        this.field.setAccessible(flag);
    }

    public void set(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException {
        this.field.set(obj, value);
    }

    public Type getGenericType() {
        return this.field.getGenericType();
    }

    /**
     * Returns the name of this field, or the name of the source field if this field
     * is an {@link AliasFor alias for} it.
     * 
     * @return the name of this field or the source field
     * @see AliasFor
     */
    public String getAliasFor() {
        return this.aliasFor;
    }

    public Class<? extends Converter<?, ?>> getConverterClass() {
        return this.converterClass;
    }


}
package com.keroz.beancopyutils.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;

import com.keroz.beancopyutils.annotation.AliasFor;
import com.keroz.beancopyutils.annotation.CopyIgnore;
import com.keroz.beancopyutils.annotation.ToCollection;
import com.keroz.beancopyutils.converter.Converter;

public class ExtendedField {

    private Field field;
    private String aliasFor;
    private CopyIgnore copyIgnore;
    private Class<? extends Converter<?, ?>> converterClass;
    private Class<? extends Collection<?>> collectionClass;


    public ExtendedField(Field field) {
        this.field = field;
        this.aliasFor = internalGetAliasFor();
        this.copyIgnore = field.getDeclaredAnnotation(CopyIgnore.class);
        this.converterClass = internalGetConverterClass();
        this.collectionClass = internalGetCollectionClass();
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

    @SuppressWarnings("unchecked")
    private Class<? extends Collection<?>> internalGetCollectionClass() {
        ToCollection toCollection = field.getDeclaredAnnotation(ToCollection.class);
        if (toCollection != null) {
            return (Class<? extends Collection<?>>) toCollection.value();
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

    public CopyIgnore getCopyIgnore() {
        return this.copyIgnore;
    }

    public Class<? extends Converter<?, ?>> getConverterClass() {
        return this.converterClass;
    }

    public Class<? extends Collection<?>> getCollectionClass() {
        return this.collectionClass;
    }
}
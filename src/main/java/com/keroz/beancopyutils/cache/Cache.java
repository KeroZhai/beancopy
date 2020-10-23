package com.keroz.beancopyutils.cache;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.keroz.beancopyutils.fieldaccesser.FieldReader;
import com.keroz.beancopyutils.fieldaccesser.FieldWriter;

import lombok.Data;

@Data
public class Cache {

    public Cache(Class<?> clazz) {
        this.clazz = clazz;
        this.methodAccess = MethodAccess.get(clazz);
    }

    private Class<?> clazz;
    private MethodAccess methodAccess;
    private List<Field> fields;
    private HashMap<String, FieldReader> fieldReaderMap;
    private HashMap<String, FieldWriter> fieldWriterMap;

}
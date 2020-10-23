package com.keroz.beancopyutils.fieldaccesser;

public interface FieldWriter {
    void write(Object target, Object value, boolean ignoreNull, String[] ignoreConditions);
}
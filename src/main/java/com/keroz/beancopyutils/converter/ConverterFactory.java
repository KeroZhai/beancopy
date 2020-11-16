package com.keroz.beancopyutils.converter;

import java.util.HashMap;
import java.util.Map;

public class ConverterFactory {

    private static final Map<Class<? extends Converter<?, ?>>, Converter<?, ?>> CACHE = new HashMap<>();

    public static Converter<?, ?> getConverter(Class<? extends Converter<?, ?>> converterClass) {
        Converter<?, ?> converter = null;
        if (converterClass != null) {
            synchronized (CACHE) {
                converter = CACHE.get(converterClass);
            }
            if (converter == null) {
                synchronized (CACHE) {
                    converter = CACHE.get(converterClass);
                    if (converter == null) {
                        try {
                            converter = converterClass.newInstance();
                        } catch (InstantiationException | IllegalAccessException e) {
                            e.printStackTrace();
                            throw new com.keroz.beancopyutils.exception.InstantiationException(
                                    "Failed to instantiate converter class: " + converterClass.getName(), e);
                        }
                        CACHE.put(converterClass, converter);
                    }
                }
            }
        }
        return converter;
    }

}
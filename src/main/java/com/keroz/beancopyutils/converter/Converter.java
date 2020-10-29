package com.keroz.beancopyutils.converter;

/**
 * A converter interface to convert one object to another.
 */
public interface Converter<Source, Target> {

    Target convert(Source source);
    
}
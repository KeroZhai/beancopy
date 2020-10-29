package com.keroz.beancopyutils.converter;

public class IntegerToStringConverter implements Converter<Integer, String> {

    @Override
    public String convert(Integer source) {
        return "Wow, it's amazing!!";
    }

   
    
}
package com.keroz.beancopyutils.converter;

import java.util.Date;

public class TimestampToDateConverter implements Converter<Long, Date> {

    @Override
    public Date convert(Long source) {
        return new Date(source);
    }

}
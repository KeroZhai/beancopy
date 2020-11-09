package com.keroz.beancopyutils;

import java.util.Date;

import com.keroz.beancopyutils.annotation.AliasFor;
import com.keroz.beancopyutils.annotation.Converter;
import com.keroz.beancopyutils.converter.TimestampToDateConverter;

import org.junit.jupiter.api.Test;

import lombok.Data;
import lombok.ToString;

public class AliasForAndConverterTest {

    @Data
    public static class Source {
        private long timestamp = 1603941172886L;
    }

    @Data
    @ToString
    public static class Target {

        @AliasFor("timestamp")
        @Converter(TimestampToDateConverter.class)
        private Date date;

    }


    @Test
    public void testAliasForAndConverter() {
        Target t = BeanCopyUtils.copy(new Source(), Target.class);
        System.out.println(t);
    }
    
}
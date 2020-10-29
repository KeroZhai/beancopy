package com.keroz.beancopyutils;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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

        private Set<Integer> set;

        Source() {
            this.set = new HashSet<>();
            set.add(1);
            set.add(2);
            set.add(3);
        }
    }

    public static class SetConverter implements com.keroz.beancopyutils.converter.Converter<Set<Object>, Set<Object>> {

        @Override
        public Set<Object> convert(Set<Object> source) {
            Set<Object> set = new HashSet<>();
            for (Object o : source) {
                set.add(o);
            }
            return set;
        }

    }

    @Data
    @ToString
    public static class Target {

        @AliasFor("timestamp")
        @Converter(TimestampToDateConverter.class)
        private Date date;

        @Converter(SetConverter.class)
        private Set<Integer> set;
    }


    @Test
    public void testAliasForAndConverter() {
        System.out.println(BeanCopyUtils.copy(new Source(), Target.class));
    }
    
}
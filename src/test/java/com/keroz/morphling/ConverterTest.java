package com.keroz.morphling;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

import com.keroz.morphling.annotation.AliasFor;
import com.keroz.morphling.annotation.Converter;
import com.keroz.morphling.mapper.Mapper;
import com.keroz.morphling.mapper.MapperFactory;

import org.junit.jupiter.api.Test;

import lombok.Data;

public class ConverterTest {

    private MapperFactory mapperFactory = MapperFactory.defaultMapperFactory();

    public static class TimestampToDateConverter implements com.keroz.morphling.converter.Converter<Long, Date> {

        @Override
        public Date convert(Long source, MapperFactory mapperFactory) {
            return new Date(source);
        }

    }

    @Data
    public static class Source {
        private long timestamp = new Date().getTime();
    }

    @Data
    public static class Target {
        @AliasFor("timestamp")
        @Converter(TimestampToDateConverter.class)
        private Date date;
    }

    @Test
    public void test() {
        Source source = new Source();
        Mapper<Source, Target> mapper = mapperFactory.getMapperFor(Source.class, Target.class);
        Target target = mapper.map(source);

        assertEquals(new Date(source.getTimestamp()), target.getDate());
    }
}

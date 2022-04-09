package com.keroz.morphling;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.keroz.morphling.annotation.AliasFor;
import com.keroz.morphling.mapper.Mapper;
import com.keroz.morphling.mapper.MapperFactory;

import org.junit.jupiter.api.Test;

import lombok.Data;

public class AliasForTest {

    private MapperFactory mapperFactory = MapperFactory.defaultMapperFactory();

    @Data
    public static class Source {
        private String a = "123";
    }

    @Data
    public static class Target {
        @AliasFor("a")
        private String b;
    }

    @Test
    public void test() {
        Source source = new Source();
        Mapper<Source, Target> mapper = mapperFactory.getMapperFor(Source.class, Target.class);
        Target target = mapper.map(source);

        assertEquals(source.getA(), target.getB());
    }

}

package com.keroz.morphling;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.keroz.morphling.mapper.Mapper;
import com.keroz.morphling.mapper.MapperFactory;

import org.junit.jupiter.api.Test;

import lombok.Data;

public class NestedTypeMappingTest {

    private MapperFactory mapperFactory = MapperFactory.defaultMapperFactory();

    @Data
    public static class Foo {
        private int id = 1;

        private String name = "nested";
    }

    @Data
    public static class Bar {
        private int id;

        private String name;
    }

    @Data
    public static class Source {
        private int id = 1;

        private String name = "name";

        private Foo nested = new Foo();
    }

    @Data
    public static class Target {
        private int id;

        private String name;

        private Bar nested;
    }

    @Test
    public void testMapping() {
        Source source = new Source();
        Mapper<Source, Target> mapper = mapperFactory.getMapperFor(Source.class, Target.class);
        Target target = mapper.map(source);

        assertEquals(source.getId(), target.getId());
        assertEquals(source.getName(), target.getName());
        assertEquals(source.getNested().getId(), target.getNested().getId());
        assertEquals(source.getNested().getName(), target.getNested().getName());
    }

}

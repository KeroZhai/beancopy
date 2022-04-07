package com.keroz.morphling;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.keroz.morphling.mapper.Mapper;
import com.keroz.morphling.mapper.MapperFactory;

import org.junit.jupiter.api.Test;

import lombok.Data;

public class NestedBeanMappingTest {

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

    @Test
    public void testMapping() {
        Source source = new Source();
        Mapper<Source, Target> mapper = MapperFactory.getMapperFor(Source.class, Target.class);
        Target target = mapper.map(source);

        assertEquals(target.getId(), source.getId());
        assertEquals(target.getName(), source.getName());
        assertEquals(target.getNested().getId(), source.getNested().getId());
        assertEquals(target.getNested().getName(), source.getNested().getName());
    }

}

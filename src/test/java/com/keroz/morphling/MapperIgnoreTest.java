package com.keroz.morphling;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.keroz.morphling.annotation.MapperIgnore;
import com.keroz.morphling.annotation.MapperIgnore.Included;
import com.keroz.morphling.annotation.MapperIgnore.Policy;
import com.keroz.morphling.mapper.Mapper;
import com.keroz.morphling.mapper.MapperFactory;

import org.junit.jupiter.api.Test;

import lombok.Data;

public class MapperIgnoreTest {

    private MapperFactory mapperFactory = MapperFactory.defaultMapperFactory();

    @Data
    public static class Source {
        private String name = "source";
        private String nullValue;
        private String emptyValue = "";
    }

    @Data
    public static class Target {

        public static interface CopyName extends Included {}

        @MapperIgnore(groups = CopyName.class)
        private String name;
        @MapperIgnore(policy = Policy.IGNORE_NULL)
        private String nullValue = "nonNullValue";
        @MapperIgnore(policy = Policy.IGNORE_EMPTY)
        private String emptyValue = "nonEmptyValue";
    }

    @Test
    public void testIgnore() {
        Source source = new Source();
        Target target = new Target();
        Mapper<Source, Target> mapper = mapperFactory.getMapperFor(Source.class, Target.class);

        mapper.map(source, target);

        assertNull(target.getName());
        assertNotNull(target.getNullValue());
        assertNotEquals("", target.getEmptyValue());
    }

}

package com.keroz.morphling;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.keroz.morphling.mapper.Mapper;
import com.keroz.morphling.mapper.MapperFactory;

import org.junit.jupiter.api.Test;

import lombok.Data;

public class CollectionTypeMappingTest {

    private static MapperFactory mapperFactory = MapperFactory.defaultMapperFactory();

    @Data
    public static class Foo {
        private long id = 1;
        private String name = "foo";
    }

    @Data
    public static class Bar {
        private Long id;
        private String name;
    }

    @Data
    public static class Source1 {
        private List<Integer> stringArrayList = new ArrayList<>();
        private List<Foo> objectLinkedList = new LinkedList<>();

        public Source1() {
            stringArrayList.add(1);
            objectLinkedList.add(new Foo());
        }
    }

    @Data
    public static class Target1 {
        private List<Integer> stringArrayList;
        private List<Bar> objectLinkedList;
    }

    @Test
    public void testInterface() {
        Source1 source = new Source1();
        Mapper<Source1, Target1> mapper = mapperFactory.getMapperFor(Source1.class, Target1.class);
        Target1 target = mapper.map(source);

        System.out.println(target);
    }

    @Data
    public static class Source2 {
        private List<Integer> stringList = new LinkedList<>();
        private List<Foo> objectList = new ArrayList<>();

        public Source2() {
            stringList.add(1);
            objectList.add(new Foo());
        }
    }

    @Data
    public static class Target2 {
        private ArrayList<Integer> stringList;
        private LinkedList<Bar> objectList;
    }

    @Test
    public void testImplementation() {
        Source2 source = new Source2();
        Mapper<Source2, Target2> mapper = mapperFactory.getMapperFor(Source2.class, Target2.class);
        Target2 target = mapper.map(source);

        System.out.println(target);
    }

}

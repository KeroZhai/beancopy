package com.keroz.beancopy;

import com.keroz.beancopy.contract.Mappable;
import com.keroz.beancopy.mapper.MapperFactory;

import lombok.Data;

public class Main {

    @Data
    public static class Source implements Mappable {
        private long id;
        private String name;
        private Foo obj;
        private int[] ints;
        private boolean[] booleans;
        private Foo[] objectArray;
    }

    @Data
    public static class Target {
        private long id;
        private String name;
        private Bar obj;
        private int[] ints;
        private boolean[] booleans;
        private Bar[] objectArray;

    }

    @Data
    public static class Target2 {
        private String name;
        private Bar obj;
    }

    @Data
    public static class Foo {
        private long id;
        private String name;
        private Baz baz;
    }

    @Data
    public static class Bar {
        private long id;
        private String name;
        private Baz baz;
    }

    @Data
    public static class Baz {
        private long id;
        private String name;
    }

    public static void main(String[] args) {
        Baz baz = new Baz();
        baz.setId(789);
        baz.setName("baz");

        Foo foo = new Foo();
        foo.setId(456);
        foo.setName("foo");
        foo.setBaz(baz);

        Source source = new Source();
        source.setId(123L);
        source.setName("source");
        source.setInts(new int[] { 1, 2, 3 });
        source.setBooleans(new boolean[] { true, false, true });
        source.setObjectArray(new Foo[] { foo });
        source.setObj(foo);

        Target target = MapperFactory.getMapperFor(Source.class, Target.class).map(source);
        System.out.println(target);
    }

}

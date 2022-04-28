package com.keroz.beancopyutils;

import com.keroz.beancopyutils.annotation.CopyIgnore;

import org.junit.jupiter.api.Test;

import lombok.Data;
import lombok.ToString;

public class IgnoreConditionTest {

    @Data
    public static class Source1 {
        private int id = 1;
        private String name = "source1";
        private Source2 obj = new Source2();
    }

    @Data
    public static class Source2 {
        private int id = 2;
        private String name = "source2";
    }

    @Data
    @ToString
    public static class Target1 {

        public static interface CopyWithoutID {}

        public static interface CopyWithName {}

        @CopyIgnore(when = CopyWithoutID.class)
        private int id;
        @CopyIgnore(except = CopyWithName.class)
        private String name;
        private Target2 obj;
    }

    @Data
    @ToString
    public static class Target2 {

        public static interface CopyWithoutName {}

        private int id;
        @CopyIgnore(when = CopyWithoutName.class)
        private String name;
    }

    @Test
    public void testIgnoreWhen() {
        System.out.println(BeanCopyUtils.copy(new Source1(), Target1.class));
        System.out.println(BeanCopyUtils.copy(new Source1(), Target1.class,
                new Class<?>[] { Target1.CopyWithoutID.class, Target2.CopyWithoutName.class }));
    }

    @Test
    public void testIgnoreExcept() {
        System.out.println(BeanCopyUtils.copy(new Source1(), Target1.class));
        System.out.println(BeanCopyUtils.copy(new Source1(), Target1.class, new Class<?>[] { Target1.CopyWithName.class }));
    }
}

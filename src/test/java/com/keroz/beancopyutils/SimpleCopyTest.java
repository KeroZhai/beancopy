package com.keroz.beancopyutils;

import org.junit.jupiter.api.Test;

import lombok.Data;
import lombok.ToString;

public class SimpleCopyTest {

    public static class Source1 {
        private int id = 1;
        private String name = "source1";
    }

    @ToString
    public static class Target1 {
        private int id;
        private String name;
    }

    /**
     * Copy without getters/setters.
     */
    @Test
    public void testWithoutGettersAndSetters() {
        System.out.println(BeanCopyUtils.copy(new Source1(), Target1.class));
    }

    @Data
    public static class Source2 {
        private int id;
        private String name;
    }

    @Data
    @ToString
    public static class Target2 {
        private int id;
        private String name;
    }

    /**
     * Copy with getters/setters.
     */
    @Test
    public void testWithGettersAndSetters() {
        System.out.println(BeanCopyUtils.copy(new Source1(), Target1.class));
    }
    
}
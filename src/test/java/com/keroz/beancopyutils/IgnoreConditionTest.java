package com.keroz.beancopyutils;

import com.keroz.beancopyutils.annotation.CopyIgnore;
import com.keroz.beancopyutils.annotation.IgnoreCondition;

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

        public static final String COPY_WITHOUT_ID = "copyWithoutID";

        public static final String COPY_WITH_NAME = "copyWithName";

        @CopyIgnore(when = COPY_WITHOUT_ID)
        private int id;
        @CopyIgnore(except = COPY_WITH_NAME)
        private String name;
        private Target2 obj;
    }

    @Data
    @ToString
    public static class Target2 {
        private int id;
        private String name;
    }

    @Test
    public void testIgnoreWhen() {
        System.out.println(BeanCopyUtils.copy(new Source1(), Target1.class));
        System.out.println(BeanCopyUtils.copy(new Source1(), Target1.class, new String[] {
            Target1.COPY_WITHOUT_ID
        }));
    }

    @Test
    public void testIgnoreExcept() {
        System.out.println(BeanCopyUtils.copy(new Source1(), Target1.class));
        System.out.println(BeanCopyUtils.copy(new Source1(), Target1.class, new String[] {
            Target1.COPY_WITH_NAME
        }));
    }
}
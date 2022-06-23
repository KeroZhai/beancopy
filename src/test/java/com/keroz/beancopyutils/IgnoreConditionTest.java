package com.keroz.beancopyutils;

import com.keroz.beancopyutils.annotation.CopyIgnore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

        public static interface CopyWithoutID {
        }

        public static interface CopyWithName {
        }

        @CopyIgnore(when = CopyWithoutID.class)
        private int id;
        @CopyIgnore(except = CopyWithName.class)
        private String name;
        private Target2 obj;
    }

    @Data
    @ToString
    public static class Target2 {

        public static interface CopyWithoutName {
        }

        private int id;
        @CopyIgnore(when = CopyWithoutName.class)
        private String name;
    }

    @Data
    @ToString
    public static class Target3 {

        // Exclude `id` by default, except the IncludingId group is specified
        @CopyIgnore(exceptionGroups = IncludingId.class)
        private int id;

        // Include `name` by default, except the ExcludingName group is specified
        @CopyIgnore(defaultIgnored = false, exceptionGroups = ExcludingName.class)
        private String name;

        public static interface IncludingId {
        }

        public static interface ExcludingName {
        }

        public static interface Excluding extends ExcludingName {
        }
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
        System.out.println(
                BeanCopyUtils.copy(new Source1(), Target1.class, new Class<?>[] { Target1.CopyWithName.class }));
    }

    @Test
    public void testIgnoreGroup() {
        Source1 source = new Source1();
        Target3 target = BeanCopyUtils.copy(source, Target3.class);

        assertEquals(0, target.getId());
        assertEquals(source.getName(), target.getName());

        target = BeanCopyUtils.copy(source, Target3.class,
                new Class<?>[] { Target3.IncludingId.class, Target3.ExcludingName.class });

        assertEquals(source.getId(), target.getId());
        assertNull(target.getName());

        target = BeanCopyUtils.copy(source, Target3.class,
                new Class<?>[] { Target3.IncludingId.class, Target3.Excluding.class });

        assertEquals(source.getId(), target.getId());
        assertNull(target.getName());
    }
}

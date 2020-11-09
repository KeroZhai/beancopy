package com.keroz.beancopyutils;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.keroz.beancopyutils.annotation.CopyIgnore;
import com.keroz.beancopyutils.annotation.CopyIgnore.IgnorePolicy;

import org.junit.jupiter.api.Test;

import lombok.Data;

public class IgnorePolicyTest {

    @Data
    public static class Source1 {
        private Object object1 = null;
        private Object object2 = null;
        private String string1 = "";
        private String string2 = "";
        private List<Integer> list1 = new ArrayList<>();
        private List<Integer> list2 = new ArrayList<>();
        private Object object3 = null;
    }

    @Data
    public static class Target1 {
        private Object object1 = new Object();
        @CopyIgnore(policy = IgnorePolicy.NULL)
        private Object object2 = new Object();
        private String string1 = "string";
        @CopyIgnore(policy = IgnorePolicy.EMPTY)
        private String string2 = "string";
        private List<Integer> list1 = Arrays.asList(1, 2);
        @CopyIgnore(policy = IgnorePolicy.EMPTY)
        private List<Integer> list2 = Arrays.asList(1, 2);
        @CopyIgnore(policy = IgnorePolicy.NONE)
        private Object object3 = new Object();
    }

    /**
     * Test if null values are ignored.
     */
    @Test
    public void testIgnoreNull() {
        Target1 t1 = new Target1();
        BeanCopyUtils.copy(new Source1(), t1, IgnorePolicy.NULL);
        assertTrue(t1.getObject1() != null);
        Target1 t2 = new Target1();
        BeanCopyUtils.copy(new Source1(), t2);
        assertTrue(t2.getObject1() == null);
        assertTrue(t2.getObject2() != null);
    }

    @Data
    public static class Source2 {
        private String string1 = "";
        private String string2 = "";
        private int[] array1 = {};
        private int[] array2 = {};
        private List<Integer> list1 = new ArrayList<>();
        private List<Integer> list2 = new ArrayList<>();
        private int int1 = 0;
        private Integer integer1 = 0;
    }

    @Data
    public static class Target2 {
        private String string1 = "string";
        @CopyIgnore(policy = IgnorePolicy.EMPTY)
        private String string2 = "string";
        private int[] array1 = {1, 2};
        @CopyIgnore(policy = IgnorePolicy.EMPTY)
        private int[] array2 = {1, 2};
        private List<Integer> list1 = Arrays.asList(1, 2);
        @CopyIgnore(policy = IgnorePolicy.EMPTY)
        private List<Integer> list2 = Arrays.asList(1, 2);
        private int int1 = 1;
        private Integer integer1 = 1;
    }

    /**
     * Test if empty values are ignored. An empty string, array, collection or zero
     * is all considered as an empty value, including null.
     */
    @Test
    public void testIgnoreEmpty() {
        Target2 t1 = new Target2();
        BeanCopyUtils.copy(new Source2(), t1, IgnorePolicy.EMPTY);
        assertTrue(!t1.getString1().isEmpty());
        assertTrue(t1.getArray1().length != 0);
        assertTrue(!t1.getList1().isEmpty());
        assertTrue(t1.getInt1() != 0);
        Target2 t2 = new Target2();
        BeanCopyUtils.copy(new Source2(), t2);
        assertTrue(t2.getString1().isEmpty());
        assertTrue(t2.getArray1().length == 0);
        assertTrue(t2.getList1().isEmpty());
        assertTrue(!t2.getString2().isEmpty());
        assertTrue(t2.getArray2().length != 0);
        assertTrue(!t2.getList2().isEmpty());
    }

    /**
     * Test if the {@code IgnorePolicy} specified in the annotation has higher
     * priority than in the copy method(s).
     */
    @Test
    public void testPriority() {
        // Target t = new Target();
        // BeanCopyUtils.copy(new Source(), t, IgnorePolicy.NULL);
        // assertTrue(t.getObject3() == null);


    }

}
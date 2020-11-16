package com.keroz.beancopyutils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.keroz.beancopyutils.annotation.AliasFor;
import com.keroz.beancopyutils.annotation.CopyIgnore;
import com.keroz.beancopyutils.annotation.ToCollection;
import com.keroz.beancopyutils.exception.TypeMismatchException;

import org.junit.jupiter.api.Test;

import lombok.Data;
import lombok.ToString;

public class CopyCollectionTest {

    @Data
    public static class Source1 {
        private int[] intArray = { 1, 2, 3 };
        private Object[] objectArray = { new Object() };
    }

    @Data
    public static class Target1 {
        private int[] intArray;
        @AliasFor("intArray")
        private Integer[] integerArray;
        @AliasFor("intArray")
        private String[] stringArray; // This will cause a TypeMismatchException to be thrown
        private Object[] objectArray;
    }

    @Test
    public void testCopyArray() {
        assertThrows(TypeMismatchException.class, () -> {
            System.out.println(BeanCopyUtils.copy(new Source1(), Target1.class));
        });
    }

    @Data
    public static class Source2 {
        private ArrayList<Integer> intArrayList = new ArrayList<>();
        private LinkedList<Integer> intLinkedList = new LinkedList<>();
        private HashSet<Integer> intHashSet = new HashSet<>();
        private Queue<Integer> intQueue = new LinkedList<>();
        private Deque<Integer> intDeque = new LinkedList<>();

        public Source2() {
            this.intArrayList.add(1);
            this.intLinkedList.add(2);
            this.intHashSet.add(3);
            this.intQueue.add(4);
            this.intDeque.add(5);
        }
    }

    @Data
    @ToString
    public static class Target2 {

        public static final String TEST_COMPONENT_MISMATCH = "testComponnetMismatch";
        public static final String TEST_CONTAINER_MISMATCH = "testContainerMismatch";

        private ArrayList<Integer> intArrayList;
        private LinkedList<Integer> intLinkedList;
        @AliasFor("intArrayList")
        private List<Integer> intList1;
        @AliasFor("intLinkedList")
        private List<Integer> intList2;
        @CopyIgnore(except = TEST_COMPONENT_MISMATCH)
        @AliasFor("intArrayList")
        private List<String> stringList; // This will cause a ClassCastException to be thrown
        @CopyIgnore(except = TEST_CONTAINER_MISMATCH)
        @AliasFor("intLinkedList")
        private ArrayList<Integer> intArrayList2; // This will cause a ClassCastException to be thrown
        @AliasFor("intLinkedList")
        @ToCollection(ArrayList.class)
        private ArrayList<Integer> intArrayList3;
        private HashSet<Integer> intHashSet;
        private Queue<Integer> intQueue;
        private Deque<Integer> intDeque;
    }

    @Test
    public void testCopyCollection() {
        Target2 target = BeanCopyUtils.copy(new Source2(), Target2.class);
        System.out.println(target);
        assertTrue(ArrayList.class.equals(target.getIntArrayList3().getClass()));
        assertThrows(ClassCastException.class, () -> {
            BeanCopyUtils.copy(new Source2(), Target2.class, new String[] { Target2.TEST_COMPONENT_MISMATCH });
        });
        assertThrows(ClassCastException.class, () -> {
            BeanCopyUtils.copy(new Source2(), Target2.class, new String[] { Target2.TEST_CONTAINER_MISMATCH });
        });
    }

}
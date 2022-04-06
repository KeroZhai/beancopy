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

import lombok.AllArgsConstructor;
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
    public void testCopyArrayField() {
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
        private ArrayList<Integer> intArrayList2;
        @AliasFor("intLinkedList")
        @ToCollection(ArrayList.class)
        private ArrayList<Integer> intArrayList3;
        private HashSet<Integer> intHashSet;
        private Queue<Integer> intQueue;
        private Deque<Integer> intDeque;
    }

    @Test
    public void testCopyCollectionField() {
        Target2 target = BeanCopyUtils.copy(new Source2(), Target2.class);
        System.out.println(target);
        assertTrue(ArrayList.class.equals(target.getIntArrayList3().getClass()));
        assertThrows(ClassCastException.class, () -> {
            BeanCopyUtils.copy(new Source2(), Target2.class, new String[] { Target2.TEST_COMPONENT_MISMATCH });
        });
        System.out.println(
                BeanCopyUtils.copy(new Source2(), Target2.class, new String[] { Target2.TEST_CONTAINER_MISMATCH }));
    }

    @Data
    @AllArgsConstructor
    public static class Source3 {
        private String name;
    }

    @Data
    @ToString
    public static class Target3 {
        private String name;
    }

    @Test
    public void testCopyBeanCollection() {
        ArrayList<Source3> sourceArrayList = new ArrayList<>();
        sourceArrayList.add(new Source3("A"));
        sourceArrayList.add(new Source3("B"));
        sourceArrayList.add(new Source3("C"));
        sourceArrayList.add(new Source3("D"));
        sourceArrayList.add(new Source3("E"));
        ArrayList<Target3> targetArrayList = BeanCopyUtils.copyCollection(sourceArrayList, Target3.class);
        assertTrue(targetArrayList.size() == sourceArrayList.size());
        assertTrue(targetArrayList.get(0) instanceof Target3);
        // Use supplier to supply a collection implementation which holds the copied elements.
        LinkedList<Target3> targetLinkedList = BeanCopyUtils.copyCollection(sourceArrayList, Target3.class,
                LinkedList::new);
        assertTrue(targetLinkedList.size() == sourceArrayList.size());
        assertTrue(targetLinkedList.get(0) instanceof Target3);
        // If no supplier is provided, result will be of the same type as source
        // collection, thus a ClassCastException will be thrown.
        assertThrows(ClassCastException.class, () -> {
            @SuppressWarnings("unused")
            LinkedList<Target3> wrongType = BeanCopyUtils.copyCollection(sourceArrayList, Target3.class);
        });
    }

}

package com.keroz.beancopyutils;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public class TestBeanCopy {

    public static void main(String[] args) throws ClassNotFoundException {
        // int times = 2500;
        // int type = 1;
        // Object source;
        // Object target1, target2;
        // switch (type) {
        //     case 1:
        //         source = new Source1();
        //         target1 = new Target1();
        //         target2 = new Target1();
        //         break;
        //     case 2:
        //         source = new Source2();
        //         target1 = new Target2();
        //         target2 = new Target2();
        //         break;
        //     case 3:
        //         source = new Source3();
        //         target1 = new Target3();
        //         target2 = new Target3();
        //         break;
        //     case 4:
        //         source = new Source4();
        //         target1 = new Target4();
        //         target2 = new Target4();
        //         break;
        //     default:
        //         source = new Source4();
        //         target1 = new Target4();
        //         target2 = new Target4();
        //         break;
        // }
        // // Class.forName("com.keroz.beancopyutils.BeanCopyUtils");

        // // Use cache default.
        // long start0, start1, start2, start3, start4;

        // // New
        // start0 = System.currentTimeMillis();
        // BeanCopyUtils.copy(source, Target1.class);
        // System.out.println("Copied once using new tool after " + (System.currentTimeMillis() - start0) + " ms");
        // start1 = System.currentTimeMillis();
        // for (int i = 0; i < times; i++) {
        //     BeanCopyUtils.copy(source, Target1.class);
        // }
        // System.out.println(
        //         "Copied " + times + " times using new tool after " + (System.currentTimeMillis() - start1) + " ms");
        // start2 = System.currentTimeMillis();
        // for (int i = 0; i < times; i++) {
        //     BeanCopyUtils.copy(source, Target1.class);
        // }
        // System.out.println(
        //         "Copied " + times + " times using new tool after " + (System.currentTimeMillis() - start2) + " ms");
        // start3 = System.currentTimeMillis();
        // for (int i = 0; i < times; i++) {
        //     BeanCopyUtils.copy(source, Target1.class);
        // }
        // System.out.println(
        //         "Copied " + times + " times using new tool after " + (System.currentTimeMillis() - start3) + " ms");
        // start4 = System.currentTimeMillis();
        // Object t = null;
        // for (int i = 0; i < times; i++) {
        //     t = BeanCopyUtils.copy(source, Target1.class);
        // }
        // System.out.println(
        //         "Copied " + times + " times using new tool after " + (System.currentTimeMillis() - start4) + " ms");
        //         System.out.println(
        //         "Copied " + (times * 4 + 1) + " times using new tool after " + (System.currentTimeMillis() - start0) + " ms");
        // System.out.println(t);


        // Disable cache.
        // BeanCopyUtils.disableCache();
        // start = System.currentTimeMillis();
        // for (int i = 0; i < times; i++) {
        //     BeanCopyUtils.copy(source, target1);
        // }
        // System.out.println("Without cacahe: " + (System.currentTimeMillis() - start));
        // System.out.println(target1);
        
        Source1 source1 = new Source1();
        source1.setChildren(null);
        Source1 source2 = new Source1();
        System.out.println(source2);
        BeanCopyUtils.copy(source1, source2, true);
        System.out.println(source2);

    }


    
}

@Getter
@Setter
@ToString
class Source1 {

    private int id = 1;
    private String name = "source1";
    private List<Source2> children;

    Source1() {
        children = new ArrayList<>();
        children.add(new Source2());
    }
}

@Getter
class Source2 {
    private int id = 2;
    private String name = "source2";
    private List<Source3> children;

    Source2() {
        children = new ArrayList<>();
        children.add(new Source3());
    }
}

@Getter
class Source3 {
    private int id = 3;
    private String name = "source3";
}

@Getter
class Source4 {
    private String name = "source4";
}

@ToString
@Setter
class Target1 {

    private int id;
    private String name;
    private List<Target2> children;
}

@ToString
@Setter
class Target2 {
    private int id;
    private String name;
    private List<Target3> children;
}

@ToString
@Setter
class Target3 {
    private int id;
    private String name;
}

@ToString
// @Setter
class Target4 {
    private String name;
}
package com.keroz.beancopy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import lombok.Data;

public class Main {

    @Data
    public static class Source {
        public long id;
        public String name;
        public Foo obj;
    }

    @Data
    public static class Target {
        public long id;
        public String name;
        public Bar obj;
    }

    @Data
    public static class Foo {
        public long id;
        public String name;
    }

    @Data
    public static class Bar {
        public long id;
        public String name;
    }

    public static void main(String[] args) {
        ClassPool pool = ClassPool.getDefault();
        try {
            CtClass sourceClass = pool.get("com.keroz.beancopy.Main$Source");
            CtClass targetClass = pool.get("com.keroz.beancopy.Main$Target");

            generateMapMethodFor(sourceClass, targetClass);

            Source source = new Source();
            source.setId(123);
            source.setName("source");
            Foo foo = new Foo();
            foo.setId(456);
            foo.setName("foo");
            source.setObj(foo);

            try {
                Target target = (Target) Source.class
                        .getDeclaredMethod("mapToComKerozBeancopyMain$Target", new Class[] { Source.class })
                        .invoke(null, source);
                System.out.println(target);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } catch (NotFoundException | SecurityException | IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void generateMapMethodFor(CtClass sourceClass, CtClass targetClass) {
        try {
            CtMethod mapToTargetMethod = new CtMethod(targetClass,
                    generateMapMethodNameFor(targetClass), new CtClass[] { sourceClass }, sourceClass);
            String targetClassName = targetClass.getName();
            StringBuilder bodyBuilder = new StringBuilder("{\n");

            mapToTargetMethod.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
            bodyBuilder.append(targetClassName).append(" target = new ").append(targetClassName).append("();\n");

            for (CtField field : targetClass.getDeclaredFields()) {
                String fieldName = field.getName();
                CtClass targetFieldType = field.getType();
                CtField sourceField = sourceClass.getDeclaredField(fieldName);

                // Check if sourceClass has the field with the same name
                if (sourceField != null) {
                    CtClass sourceFieldType = sourceField.getType();

                    // Check if fieldType is primitive or its corresponding wrapper class
                    if (isPrimitiveOrWrapper(targetFieldType)) {
                        bodyBuilder.append("target.").append(field.getName()).append(" = ").append("$1.")
                                .append(field.getName()).append(";\n");
                    } else {
                        String mapMethodName = generateMapMethodNameFor(targetFieldType);

                        // Check if sourceFieldType has the mapMethod
                        try {
                            sourceFieldType.getDeclaredMethod(mapMethodName,
                                    new CtClass[] { sourceFieldType });
                        } catch (NotFoundException e) {
                            generateMapMethodFor(sourceFieldType, targetFieldType);
                        }

                        bodyBuilder.append("target.").append(field.getName()).append(" = ")
                                .append(sourceFieldType.getName()).append(".")
                                .append(mapMethodName).append("($1.").append(field.getName()).append(");\n");
                    }
                }

            }

            bodyBuilder.append("return target;\n");
            mapToTargetMethod.setBody(bodyBuilder.append("}").toString());
            sourceClass.addMethod(mapToTargetMethod);
            sourceClass.toClass();
        } catch (CannotCompileException | NotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static boolean isPrimitiveOrWrapper(CtClass ctClass) {
        return ctClass.isPrimitive() || ctClass.isEnum() || isAssignable(ctClass, "java.lang.String")
                || isAssignable(ctClass, "java.util.Date") || isAssignable(ctClass, "java.lang.Boolean")
                || isAssignable(ctClass, "java.lang.Byte")
                || isAssignable(ctClass, "java.lang.Character") || isAssignable(ctClass, "java.lang.Short")
                || isAssignable(ctClass, "java.lang.Integer") || isAssignable(ctClass, "java.lang.Long")
                || isAssignable(ctClass, "java.lang.Float") || isAssignable(ctClass, "java.lang.Double")
                || isAssignable(ctClass, "java.lang.Void");
    }

    public static boolean isAssignable(CtClass thisClass, String anotherClassName) {
        if (thisClass == null) {
            return false;
        }
        if (thisClass.getName().equals(anotherClassName)) {
            return true;
        }
        try {
            // Check if extends
            if (isAssignable(thisClass.getSuperclass(), anotherClassName)) {
                return true;
            }
            // Check if implements
            for (CtClass interfaceClass : thisClass.getInterfaces()) {
                if (isAssignable(interfaceClass, anotherClassName)) {
                    return true;
                }
            }
        } catch (NotFoundException e) {
            // keep going
        }

        return false;
    }

    private static String generateMapMethodNameFor(CtClass targetClass) {
        // Change class qualified name to camel case
        return "mapTo" + toCamelCase(targetClass.getName());
    }

    private static String toCamelCase(String className) {
        String[] parts = className.split("\\.");
        String camelCaseString = "";
        for (String part : parts) {
            camelCaseString = camelCaseString + toProperCase(part);
        }
        return camelCaseString;
    }

    private static String toProperCase(String part) {
        return part.substring(0, 1).toUpperCase() + part.substring(1);
    }

}

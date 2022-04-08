package com.keroz.morphling.mapper;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.keroz.morphling.exception.MethodNotFoundException;
import com.keroz.morphling.exception.TypeMismatchException;
import com.keroz.morphling.mapper.mapping.FieldMapper;
import com.keroz.morphling.mapper.mapping.SimpleTypeFieldMapper;
import com.keroz.morphling.util.JavassistUtils;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.SignatureAttribute.ClassSignature;
import javassist.bytecode.SignatureAttribute.ClassType;
import javassist.bytecode.SignatureAttribute.TypeArgument;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MapperFactory {

    private ClassPool POOL = ClassPool.getDefault();
    private CtClass mapperInterfaceCtClass = JavassistUtils.getCtClass(POOL, "com.keroz.morphling.mapper.Mapper");
    private CtClass objectCtClass = JavassistUtils.getCtClass(POOL, "java.lang.Object");
    private List<FieldMapper> fieldMappers = new ArrayList<>();

    @SuppressWarnings("rawtypes")
    private HashMap<String, Mapper> mapperMap = new HashMap<>();

    public <Source, Target> Mapper<Source, Target> getMapperFor(Class<Source> sourceClass, Class<Target> targetClass) {
        CtClass sourceCtClass = JavassistUtils.getCtClass(POOL, sourceClass.getName());
        CtClass targetCtClass = JavassistUtils.getCtClass(POOL, targetClass.getName());

        return getMapperFor(sourceCtClass, targetCtClass);
    }

    @SuppressWarnings("unchecked")
    public <Source, Target> Mapper<Source, Target> getMapperFor(CtClass sourceCtClass, CtClass targetCtClass) {
        String mapperClassName = generateMapperClassNameFor(sourceCtClass, targetCtClass);

        Mapper<Source, Target> mapper = mapperMap.get(mapperClassName);

        if (mapper == null) {
            try {
                mapper = (Mapper<Source, Target>) generateMapperClassFor(sourceCtClass, targetCtClass).newInstance();
                mapperMap.put(mapperClassName, mapper);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return mapper;
    }

    public Class<?> generateMapperClassFor(CtClass sourceClass, CtClass targetClass) {
        try {
            CtClass mapperCtClass = POOL.makeClass(generateMapperClassNameFor(sourceClass, targetClass));
            CtMethod mapMethod = new CtMethod(targetClass,
                    "map", new CtClass[] { sourceClass }, mapperCtClass);
            String targetClassName = targetClass.getName();
            StringBuilder bodyBuilder = new StringBuilder("{\n");
            ClassSignature classSignature = new ClassSignature(null, null,
                    new ClassType[] { new ClassType(Mapper.class.getName(),
                            new TypeArgument[] { new TypeArgument(new ClassType(sourceClass.getName())),
                                    new TypeArgument(new ClassType(targetClass.getName())) }) });

            mapperCtClass.setInterfaces(new CtClass[] { mapperInterfaceCtClass });
            mapperCtClass.setGenericSignature(classSignature.encode());
            mapMethod.setModifiers(Modifier.PUBLIC);
            bodyBuilder.append(targetClassName).append(" target = new ").append(targetClassName).append("();\n");

            for (CtField field : targetClass.getDeclaredFields()) {
                String fieldName = field.getName();
                CtClass targetFieldType = field.getType();
                CtField sourceField = JavassistUtils.getDeclaredField(sourceClass, fieldName);

                // Check if sourceClass has the field with the same name
                if (sourceField != null) {
                    CtClass sourceFieldType = sourceField.getType();
                    String getterPrefix = "boolean".equals(sourceFieldType.getName()) ? "is" : "get";
                    String capitalizedFieldName = capitalize(fieldName);
                    String getterName = getterPrefix + capitalizedFieldName;
                    String setterName = "set" + capitalizedFieldName;



                    if (!sourceFieldType.isPrimitive()) {
                        bodyBuilder.append("Object sourceValue = $1.").append(getterName).append("();\n")
                                .append("if (sourceValue != null) {\n");
                    }

                    // Check if fieldType is primitive or its corresponding wrapper class
                    if (JavassistUtils.isPrimitiveOrWrapper(targetFieldType)) {
                        if (!sourceFieldType.isPrimitive() && targetFieldType.isPrimitive()) {
                            switch (sourceFieldType.getName()) {
                                case "java.lang.Boolean": {
                                    bodyBuilder.append("target.").append(setterName).append("($1.")
                                            .append(getterName)
                                            .append("().booleanValue());\n");
                                    break;
                                }
                                case "java.lang.Byte": {
                                    bodyBuilder.append("target.").append(setterName).append("($1.")
                                            .append(getterName)
                                            .append("().byteValue());\n");
                                    break;
                                }
                                case "java.lang.Character": {
                                    bodyBuilder.append("target.").append(setterName).append("($1.")
                                            .append(getterName)
                                            .append("().charValue());\n");
                                    break;
                                }
                                case "java.lang.Short": {
                                    bodyBuilder.append("target.").append(setterName).append("($1.")
                                            .append(getterName)
                                            .append("().shortValue());\n");
                                    break;
                                }
                                case "java.lang.Integer": {
                                    bodyBuilder.append("target.").append(setterName).append("($1.")
                                            .append(getterName)
                                            .append("().intValue());\n");
                                    break;
                                }
                                case "java.lang.Long": {
                                    bodyBuilder.append("target.").append(setterName).append("($1.")
                                            .append(getterName)
                                            .append("().longValue());\n");
                                    break;
                                }
                                case "java.lang.Float": {
                                    bodyBuilder.append("target.").append(setterName).append("($1.")
                                            .append(getterName)
                                            .append("().floatValue());\n");
                                    break;
                                }
                                case "java.lang.Double": {
                                    bodyBuilder.append("target.").append(setterName).append("($1.")
                                            .append(getterName)
                                            .append("().doubleValue());\n");
                                    break;
                                }
                                default:
                                    break;
                            }

                        } else if (sourceFieldType.isPrimitive() && !targetFieldType.isPrimitive()) {
                            switch (targetFieldType.getName()) {
                                case "java.lang.Boolean": {
                                    bodyBuilder.append("target.").append(setterName).append("(Boolean.valueOf($1.")
                                            .append(getterName)
                                            .append("()));\n");
                                    break;
                                }
                                case "java.lang.Byte": {
                                    bodyBuilder.append("target.").append(setterName).append("(Byte.valueOf($1.")
                                            .append(getterName)
                                            .append("()));\n");
                                    break;
                                }
                                case "java.lang.Character": {
                                    bodyBuilder.append("target.").append(setterName).append("(Character.valueOf($1.")
                                            .append(getterName)
                                            .append("()));\n");
                                    break;
                                }
                                case "java.lang.Short": {
                                    bodyBuilder.append("target.").append(setterName).append("(Short.valueOf($1.")
                                            .append(getterName)
                                            .append("()));\n");
                                    break;
                                }
                                case "java.lang.Integer": {
                                    bodyBuilder.append("target.").append(setterName).append("(Integer.valueOf($1.")
                                            .append(getterName)
                                            .append("()));\n");
                                    break;
                                }
                                case "java.lang.Long": {
                                    bodyBuilder.append("target.").append(setterName).append("(Long.valueOf($1.")
                                            .append(getterName)
                                            .append("()));\n");
                                    break;
                                }
                                case "java.lang.Float": {
                                    bodyBuilder.append("target.").append(setterName).append("(Float.valueOf($1.")
                                            .append(getterName)
                                            .append("()));\n");
                                    break;
                                }
                                case "java.lang.Double": {
                                    bodyBuilder.append("target.").append(setterName).append("(Double.valueOf($1.")
                                            .append(getterName)
                                            .append("()));\n");
                                    break;
                                }
                                default:
                                    break;
                            }
                        } else if (sourceFieldType.getName().equals(targetFieldType.getName())) {
                            bodyBuilder.append("target.").append(setterName).append("($1.")
                                    .append(getterPrefix)
                                    .append(capitalize(field.getName())).append("());\n");
                        } else {
                            throw new TypeMismatchException("Field " + fieldName + " in class "
                                    + sourceClass.getName() + " is of type " + sourceFieldType.getName()
                                    + " but the target field is of type " + targetFieldType.getName());
                        }
                    } else if (targetFieldType.isArray()) {
                        if (sourceFieldType.isArray()) {
                            CtClass sourceComponentType = sourceFieldType.getComponentType();
                            CtClass targetComponentType = targetFieldType.getComponentType();

                            bodyBuilder
                                    .append("{Object sourceArray = $1.get").append(capitalize(field.getName()))
                                    .append("();\n")
                                    .append("int length = java.lang.reflect.Array.getLength(sourceArray);\n")
                                    .append("Object targetArray = java.lang.reflect.Array.newInstance(")
                                    .append(targetComponentType.getName()).append(".class, length);\n")
                                    .append("try {\n");

                            if (sourceComponentType.isPrimitive() || targetComponentType.isPrimitive()) {
                                bodyBuilder.append("for (int i = 0; i < length; i++) {\n")
                                        .append("java.lang.reflect.Array.set(targetArray, i, java.lang.reflect.Array.get(sourceArray, i));\n")
                                        .append("}");
                            } else {
                                bodyBuilder.append("com.keroz.morphling.mapper.Mapper mapper = ")
                                        .append(MapperFactory.class.getName())
                                        .append(".getMapperFor(")
                                        .append(sourceComponentType.getName().replace("$", "."))
                                        .append(".class").append(",")
                                        .append(targetComponentType.getName().replace("$", "."))
                                        .append(".class").append(");\n")
                                        .append("for (int i = 0; i < length; i++) {\n")
                                        .append("java.lang.reflect.Array.set(targetArray, i, mapper.map(java.lang.reflect.Array.get(sourceArray, i)));\n")
                                        .append("}\n");
                            }

                            bodyBuilder.append("} catch (IllegalArgumentException ignored) {}\n")
                                    .append("target.set").append(capitalize(field.getName())).append("((")
                                    .append(targetComponentType.getName().replace("$", "."))
                                    .append("[]) targetArray);}\n");

                        } else {
                            throw new TypeMismatchException("Field " + fieldName + " in class "
                                    + sourceClass.getName() + " is of type " + sourceFieldType.getName()
                                    + " but the target field is of type " + targetFieldType.getName());
                        }

                    } else if (JavassistUtils.isAssignable(targetFieldType, "java.util.Collection")) {

                    } else {
                        String sourceFieldTypeName = sourceFieldType.getName().replace("$",
                                ".");
                        String targetFieldTypeName = targetFieldType.getName().replace("$",
                                ".");
                        bodyBuilder.append("target.set").append(capitalize(field.getName())).append("((")
                                .append(targetFieldTypeName).append(") ")
                                .append(MapperFactory.class.getName()).append(".getMapperFor(")
                                .append(sourceFieldTypeName)
                                .append(".class").append(",")
                                .append(targetFieldTypeName)
                                .append(".class").append(")").append(".map")
                                .append("($1.get").append(capitalize(field.getName())).append("()));\n");
                    }

                    if (!sourceFieldType.isPrimitive()) {
                        bodyBuilder.append("}\n");
                    }
                }

            }
            bodyBuilder.append("return target;\n");
            String body = bodyBuilder.append("}").toString();
            // System.out.println(body);
            mapMethod.setBody(body);
            mapperCtClass.addMethod(mapMethod);

            CtMethod bridgeMethod = new CtMethod(objectCtClass,
                    "map", new CtClass[] { objectCtClass }, mapperCtClass);

            bridgeMethod.setBody("{ return map((" + sourceClass.getName() + ") $1); }");
            mapperCtClass.addMethod(bridgeMethod);

            Class<?> mapperClass = mapperCtClass.toClass();
            mapperCtClass.detach();

            return mapperClass;
        } catch (CannotCompileException | NotFoundException e) {
            e.printStackTrace();

            if (e.getMessage().contains("not found")) {

                throw new MethodNotFoundException(e.getMessage() + ". Did you forget to provide it?");
            }
        }

        return null;
    }

    private String generateMapperClassNameFor(CtClass sourceCtClass, CtClass targeCtClass) {
        return toCamelCase(sourceCtClass.getName()) + "To" + toCamelCase(targeCtClass.getName()) + "Mapper";
    }

    private String toCamelCase(String className) {
        String[] parts = className.split("\\.");
        String camelCaseString = "";
        for (String part : parts) {
            camelCaseString = camelCaseString + capitalize(part);
        }
        return camelCaseString;
    }

    private String capitalize(String part) {
        return part.substring(0, 1).toUpperCase() + part.substring(1);
    }

}

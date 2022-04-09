package com.keroz.morphling.mapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.keroz.morphling.codegenerator.ArrayTypeConversionCodeGenerator;
import com.keroz.morphling.codegenerator.CollectionTypeConversionCodeGenerator;
import com.keroz.morphling.codegenerator.ConversionCodeGenerator;
import com.keroz.morphling.codegenerator.NestedTypeConversionCodeGenerator;
import com.keroz.morphling.codegenerator.SimpleTypeConversionCodeGenerator;
import com.keroz.morphling.exception.MethodNotFoundException;
import com.keroz.morphling.util.JavassistUtils;
import com.keroz.morphling.util.ReflectionUtils;
import com.keroz.morphling.util.StringUtils;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.bytecode.SignatureAttribute.ClassSignature;
import javassist.bytecode.SignatureAttribute.ClassType;
import javassist.bytecode.SignatureAttribute.TypeArgument;

/**
 * 123
 */
public final class MapperFactory {

    private final String uniqueId = UUID.randomUUID().toString().substring(0, 8);
    private ClassPool POOL = ClassPool.getDefault();
    private CtClass mapperInterfaceCtClass = JavassistUtils.getCtClass(POOL, "com.keroz.morphling.mapper.Mapper");
    private CtClass objectCtClass = JavassistUtils.getCtClass(POOL, "java.lang.Object");
    private List<ConversionCodeGenerator> conversionCodeGenerators = new ArrayList<>();

    @SuppressWarnings("rawtypes")
    private HashMap<String, Mapper> mapperMap = new HashMap<>();

    public MapperFactory() {
        POOL.importPackage("com.keroz.morphling.mapper");
    }

    public static MapperFactory defaultMapperFactory() {
        MapperFactory instance = new MapperFactory();

        instance.addConversionCodeGenerator(new SimpleTypeConversionCodeGenerator());
        instance.addConversionCodeGenerator(new ArrayTypeConversionCodeGenerator());
        instance.addConversionCodeGenerator(new CollectionTypeConversionCodeGenerator());
        instance.addConversionCodeGenerator(new NestedTypeConversionCodeGenerator());

        return instance;
    }

    @SuppressWarnings("unchecked")
    public <Source, Target> Mapper<Source, Target> getMapperFor(Class<Source> sourceClass, Class<Target> targetClass) {
        String mapperClassName = generateMapperClassNameFor(sourceClass, targetClass);

        Mapper<Source, Target> mapper = mapperMap.get(mapperClassName);

        if (mapper == null) {
            try {
                mapper = (Mapper<Source, Target>) generateMapperClassFor(sourceClass, targetClass).newInstance();
                mapper.getClass().getDeclaredField("mapperFactory").set(mapper, this);
                mapperMap.put(mapperClassName, mapper);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | NoSuchFieldException
                    | SecurityException e) {
                e.printStackTrace();
            }
        }

        return mapper;
    }

    public Class<?> generateMapperClassFor(Class<?> sourceClass, Class<?> targetClass) {
        CtClass sourceCtClass = JavassistUtils.getCtClass(POOL, sourceClass.getName());
        CtClass targetCtClass = JavassistUtils.getCtClass(POOL, targetClass.getName());

        try {
            CtClass mapperCtClass = POOL.makeClass(generateMapperClassNameFor(sourceClass, targetClass));
            CtField mapperFactoryField = new CtField(JavassistUtils.getCtClass(POOL, getClass().getName()),
                    "mapperFactory", mapperCtClass);
            mapperFactoryField.setModifiers(Modifier.PUBLIC);
            mapperCtClass.addField(mapperFactoryField);
            CtMethod mapMethod = new CtMethod(targetCtClass,
                    "map", new CtClass[] { sourceCtClass }, mapperCtClass);
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

            for (Field targetField : targetClass.getDeclaredFields()) {
                String fieldName = targetField.getName();
                Type targetFieldType = targetField.getGenericType();
                Field sourceField = ReflectionUtils.findDeclaredField(sourceClass, fieldName);

                // Check if sourceClass has the field with the same name
                if (sourceField != null) {
                    Type sourceFieldType = sourceField.getGenericType();
                    String getterPrefix = "boolean".equals(sourceFieldType.getTypeName()) ? "is" : "get";
                    String capitalizedFieldName = StringUtils.capitalize(fieldName);
                    String getterName = getterPrefix + capitalizedFieldName;
                    String setter = "target.set" + capitalizedFieldName;
                    String sourceValue = "$1." + getterName + "()";

                    String sourceFieldNonGenericTypeName = getNonGenericTypeName(sourceFieldType);
                    String targetFieldNonGenericTypeName = getNonGenericTypeName(targetFieldType);

                    for (ConversionCodeGenerator codeGenerator : conversionCodeGenerators) {
                        if (codeGenerator.isSupported(sourceFieldType, targetFieldType)) {
                            String code = codeGenerator.generate(sourceFieldType, targetFieldType);

                            if (code != null) {
                                bodyBuilder.append("{").append(sourceFieldNonGenericTypeName).append(" sv = ")
                                        .append(sourceValue).append(";").append(targetFieldNonGenericTypeName)
                                        .append(" tv;").append(code)
                                        .append(setter).append("(tv);").append("}\n");
                            }

                            break;
                        }
                    }
                }

            }
            bodyBuilder.append("return target;\n");
            String body = bodyBuilder.append("}").toString();
            System.out.println(body);
            mapMethod.setBody(body);
            mapperCtClass.addMethod(mapMethod);

            CtMethod bridgeMethod = new CtMethod(objectCtClass,
                    "map", new CtClass[] { objectCtClass }, mapperCtClass);

            bridgeMethod.setBody("{ return map((" + sourceClass.getName() + ") $1); }");
            mapperCtClass.addMethod(bridgeMethod);

            Class<?> mapperClass = mapperCtClass.toClass();
            mapperCtClass.detach();

            return mapperClass;
        } catch (CannotCompileException e) {
            e.printStackTrace();

            if (e.getMessage().contains("not found")) {
                throw new MethodNotFoundException(e.getMessage() + ". Did you forget to provide it?");
            }
        }

        return null;
    }

    public void addConversionCodeGenerator(ConversionCodeGenerator generator) {
        conversionCodeGenerators.add(generator);
    }

    private String generateMapperClassNameFor(Class<?> sourceClass, Class<?> targetClass) {

        return StringUtils.classNameToPascalCase(sourceClass.getName()) + "To"
                + StringUtils.classNameToPascalCase(targetClass.getName()) + "Mapper$" + uniqueId;
    }

    private String getNonGenericTypeName(Type type) {
        String typeName = type.getTypeName();

        if (type instanceof ParameterizedType) {
            return typeName.substring(0, typeName.indexOf("<"));
        }

        return typeName;
    }
}

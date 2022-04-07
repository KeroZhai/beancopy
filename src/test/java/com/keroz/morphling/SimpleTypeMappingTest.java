package com.keroz.morphling;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

import com.keroz.morphling.mapper.Mapper;
import com.keroz.morphling.mapper.MapperFactory;

import org.junit.jupiter.api.Test;

import lombok.Data;

/**
 * Primitive types(and their corresponding wrapper types), String, Enum and
 * java.util.Date
 * are considered as simple types.
 */
public class SimpleTypeMappingTest {

    public static enum State {
        ON, OFF;
    }

    @Data
    public static class Source1 {
        private boolean booleanValue = true;
        private byte byteValue = 0;
        private char charValue = '1';
        private short shortValue = 2;
        private int intValue = 3;
        private long longValue = 4L;
        private float floatValue = 5.1f;
        private double doubleValue = 6.2;
        private String stringValue = "name";
        private State enumValue = State.ON;
        private Date dateValue = new Date();
    }

    @Data
    public static class Target1 {
        private boolean booleanValue;
        private byte byteValue;
        private char charValue;
        private short shortValue;
        private int intValue;
        private long longValue;
        private float floatValue;
        private double doubleValue;
        private String stringValue;
        private State enumValue;
        private Date dateValue;
    }

    @Test
    public void testMapping1() {
        Source1 source = new Source1();
        Mapper<Source1, Target1> mapper = MapperFactory.getMapperFor(Source1.class, Target1.class);
        Target1 target = mapper.map(source);

        assertEquals(target.isBooleanValue(), source.isBooleanValue());
        assertEquals(target.getByteValue(), source.getByteValue());
        assertEquals(target.getCharValue(), source.getCharValue());
        assertEquals(target.getShortValue(), source.getShortValue());
        assertEquals(target.getIntValue(), source.getIntValue());
        assertEquals(target.getLongValue(), source.getLongValue());
        assertEquals(target.getFloatValue(), source.getFloatValue());
        assertEquals(target.getDoubleValue(), source.getDoubleValue());
        assertEquals(target.getStringValue(), source.getStringValue());
        assertEquals(target.getEnumValue(), source.getEnumValue());
        assertEquals(target.getDateValue(), source.getDateValue());
    }

    @Data
    public static class Source2 {
        private Boolean booleanValue = true;
        private Byte byteValue = 0;
        private Character charValue = '1';
        private Short shortValue = 2;
        private Integer intValue = 3;
        private Long longValue = 4L;
        private Float floatValue = 5.1f;
        private Double doubleValue = 6.2;
    }

    @Data
    public static class Target2 {
        private boolean booleanValue;
        private byte byteValue;
        private char charValue;
        private short shortValue;
        private int intValue;
        private long longValue;
        private float floatValue;
        private double doubleValue;
    }

    @Test
    public void testMapping2() {
        Source2 source = new Source2();
        Mapper<Source2, Target2> mapper = MapperFactory.getMapperFor(Source2.class, Target2.class);
        Target2 target = mapper.map(source);

        assertEquals(target.isBooleanValue(), source.getBooleanValue());
        assertEquals(target.getByteValue(), source.getByteValue());
        assertEquals(target.getCharValue(), source.getCharValue());
        assertEquals(target.getShortValue(), source.getShortValue());
        assertEquals(target.getIntValue(), source.getIntValue());
        assertEquals(target.getLongValue(), source.getLongValue());
        assertEquals(target.getFloatValue(), source.getFloatValue());
        assertEquals(target.getDoubleValue(), source.getDoubleValue());

        Mapper<Target2, Source2> reverseMapper = MapperFactory.getMapperFor(Target2.class, Source2.class);
        target.setBooleanValue(false);

        Source2 target2 = reverseMapper.map(target);

        assertEquals(target.isBooleanValue(), target2.getBooleanValue());
        assertEquals(target.getByteValue(), target2.getByteValue());
        assertEquals(target.getCharValue(), target2.getCharValue());
        assertEquals(target.getShortValue(), target2.getShortValue());
        assertEquals(target.getIntValue(), target2.getIntValue());
        assertEquals(target.getLongValue(), target2.getLongValue());
        assertEquals(target.getFloatValue(), target2.getFloatValue());
        assertEquals(target.getDoubleValue(), target2.getDoubleValue());
    }
}

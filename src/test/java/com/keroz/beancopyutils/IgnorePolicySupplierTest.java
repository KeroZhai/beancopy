package com.keroz.beancopyutils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.keroz.beancopyutils.annotation.CopyIgnore;
import com.keroz.beancopyutils.exception.InvokeIgnorePolicySupplierFailedException;

import org.junit.jupiter.api.Test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

public class IgnorePolicySupplierTest {

    @Data
    @Builder
    public static class Source {

        private Payload payload;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {

        private int index;

        private String content;
    }

    @Data
    @ToString
    public static class Target {

        public static interface IgnorePayload {
        }

        @CopyIgnore(when = IgnorePayload.class, supplierMethod = "shouldIgnorePayload")
        private Payload payload;

        public boolean shouldIgnorePayload(Object source) {
            if (source instanceof Source) {
                return ((Source) source).getPayload().getIndex() == 0;
            }
            return false;
        }
    }

    @Test
    public void testIgnorePolicySupplier() {
        Source source1 = Source.builder().payload(Payload.builder().index(0).content("payload-0").build()).build();
        Target target1 = BeanCopyUtils.copy(source1, Target.class, new Class<?>[] { Target.IgnorePayload.class });
        System.out.println(target1);
        assertNull(target1.getPayload());
        Source source2 = Source.builder().payload(Payload.builder().index(1).content("payload-1").build()).build();
        Target target2 = BeanCopyUtils.copy(source2, Target.class);
        System.out.println(target2);
        assertNotNull(target2.getPayload());
    }

    @Data
    @ToString
    public static class InvalidTarget {

        @CopyIgnore(supplierMethod = "invalid")
        private Payload payload;

    }

    @Test
    public void testInvalidIgnorePolicySupplier() {
        Source source = Source.builder().payload(Payload.builder().index(0).content("payload-0").build()).build();
        assertThrows(InvokeIgnorePolicySupplierFailedException.class,
                () -> BeanCopyUtils.copy(source, InvalidTarget.class));
    }

}

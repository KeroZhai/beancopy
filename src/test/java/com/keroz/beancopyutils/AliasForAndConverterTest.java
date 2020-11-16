package com.keroz.beancopyutils;

import java.util.Date;
import java.util.concurrent.CountDownLatch;

import com.keroz.beancopyutils.annotation.AliasFor;
import com.keroz.beancopyutils.annotation.Converter;

import org.junit.jupiter.api.Test;

import lombok.Data;
import lombok.ToString;

public class AliasForAndConverterTest {

    @Data
    public static class Source {
        private long timestamp = 1603941172886L;
    }

    @Data
    @ToString
    public static class Target {

        @AliasFor("timestamp")
        @Converter(TimestampToDateConverter.class)
        private Date date;

    }

    public static class TimestampToDateConverter implements com.keroz.beancopyutils.converter.Converter<Long, Date> {

        @Override
        public Date convert(Long source) {
            System.out.println(this);
            return new Date(source);
        }
    
    }

    @Test
    public void testAliasForAndConverter() {
        Target t = BeanCopyUtils.copy(new Source(), Target.class);
        System.out.println(t);
    }

    @Test
    public void testConverterSingleton() {
        int threadNum = 10;
        CountDownLatch latch = new CountDownLatch(threadNum);
        for (int i = 0; i < threadNum; i++) {
            new Thread(() -> {
                BeanCopyUtils.copy(new Source(), Target.class);
                latch.countDown();
            }).start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
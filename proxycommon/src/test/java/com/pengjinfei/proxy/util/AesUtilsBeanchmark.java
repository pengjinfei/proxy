package com.pengjinfei.proxy.util;

import org.apache.commons.lang3.RandomUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class AesUtilsBeanchmark {

    private static byte[] bytes = RandomUtils.nextBytes(16);

    @Benchmark
    public void encrypt() {
        AesUtils.encrypt("pjf".getBytes(), bytes);
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(AesUtilsBeanchmark.class.getSimpleName())
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
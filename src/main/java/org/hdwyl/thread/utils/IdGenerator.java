package org.hdwyl.thread.utils;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Created by wangyanl on 2020/5/9.
 */
public class IdGenerator {

    private IdGenerator() {
    }

    private static AtomicLong atomicLong = new AtomicLong(0);

    private static LongAdder longAdder = new LongAdder();

    public static long getNextIdWithAtomicLong() {
        return atomicLong.incrementAndGet();
    }

    public static long getNextIdWithLongAdder() {
        longAdder.increment();
        return longAdder.longValue();
    }

}

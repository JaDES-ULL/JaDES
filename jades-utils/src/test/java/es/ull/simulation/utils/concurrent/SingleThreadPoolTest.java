package es.ull.simulation.utils.concurrent;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class SingleThreadPoolTest {

    @Test
    void shouldExecuteTasksSequentially() throws InterruptedException {
        SingleThreadPool<Runnable> pool = new SingleThreadPool<>();
        AtomicInteger sum = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(3);

        pool.execute(() -> {
            sum.addAndGet(1);
            latch.countDown();
        });
        pool.execute(() -> {
            sum.addAndGet(2);
            latch.countDown();
        });
        pool.execute(() -> {
            sum.addAndGet(3);
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        pool.shutdown();
        pool.join(1000);

        assertEquals(6, sum.get());
        assertEquals(1, pool.getNThreads());
    }
}

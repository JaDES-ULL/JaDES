package es.ull.simulation.utils.concurrent;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class StandardThreadPoolTest {

    @Test
    void shouldRejectInvalidThreadCount() {
        assertThrows(IllegalArgumentException.class, () -> new StandardThreadPool<>(0));
    }

    @Test
    void shouldExecutePendingTasks() throws InterruptedException {
        StandardThreadPool<Runnable> pool = new StandardThreadPool<>(1);
        List<Integer> order = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(2);

        pool.execute(() -> {
            order.add(1);
            latch.countDown();
        });
        pool.execute(() -> {
            order.add(2);
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        pool.shutdown();

        assertEquals(List.of(1, 2), order);
        assertEquals(1, pool.getNThreads());
    }

    @Test
    void shouldReuseSharedPoolAndShutdownWhenReleased() {
        StandardThreadPool<Runnable> first = StandardThreadPool.getPool(1);
        StandardThreadPool<Runnable> second = StandardThreadPool.getPool(1);

        assertSame(first, second);
        first.shutdown();
        second.shutdown();
    }
}

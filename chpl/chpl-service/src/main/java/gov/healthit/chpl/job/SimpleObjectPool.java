package gov.healthit.chpl.job;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class SimpleObjectPool<T> {
    private static final Logger LOGGER = LogManager.getLogger(SimpleObjectPool.class);
    
    private final BlockingQueue<T> objects;

    public SimpleObjectPool(Collection<? extends T> objects) {
        this.objects = new ArrayBlockingQueue<T>(objects.size(), false, objects);
    }

    public T borrow() throws InterruptedException {
        return this.objects.take();
    }

    public void giveBack(T object) throws InterruptedException {
        this.objects.put(object);
    }
}
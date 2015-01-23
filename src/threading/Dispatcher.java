package threading;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import threading.queue.DispatchQueue;
import threading.queue.MainQueue;
import threading.queue.NativeQueue;
import threading.queue.PoolQueue;

public final class Dispatcher {
	
	private static MainQueue mainQueue = null;
	private static ConcurrentLinkedQueue<DispatchQueue> queues = new ConcurrentLinkedQueue<>();
	
	//Custom Name for easy debugging
	private static final String EXECUTOR_THREADNAME_PREFIX = "DISPATCH";
	private static ThreadFactory threadFactory = new ThreadFactory() {
	    private final AtomicInteger id = new AtomicInteger(0);
	    @Override
	    public Thread newThread(Runnable r) {
	        Thread thread = new Thread(r);
	        thread.setName(EXECUTOR_THREADNAME_PREFIX + "_" + id.incrementAndGet());
	        return thread;
	    }
	};
	private static ExecutorService service = Executors.newCachedThreadPool(threadFactory);
	
	/**
	 * This function needs to be started from the main thread.
	 *  It will then schedule the given mainFunction on the mainQueue.
	 *  This function will block while the mainQueue is in use.
	 */
	public static void start(Runnable mainLoop) {
		//Assume we get started on the main thread
		mainQueue = new MainQueue();
		mainQueue.dispatchAsync(() -> {
			mainLoop.run();
		});
		mainQueue.run();
	}
	
	/**
	 * This function shuts down the dispatcher.
	 * 1. It shuts down all queues waiting for remaining tasks
	 * 2. it returns from the start function effectively continuing the main function.
	 */
	public static void shutdown() {
		mainQueue.dispatchAsync(() -> {
			while (!queues.isEmpty()) {
				Iterator<DispatchQueue> itr = queues.iterator();
				while (itr.hasNext()) { //This Iterator is mutable!
					itr.next().stop();
					itr.remove();
				}
			}
			
			service.shutdown();
			boolean waiting = true;
			while (waiting) {
				try {
					waiting = !service.awaitTermination(1, TimeUnit.SECONDS);
				} catch (InterruptedException e) {}
			}
			
			mainQueue.stop();
		});
	}
	
	public static DispatchQueue mainQueue() {
		if (mainQueue == null)
			throw new RuntimeException("Used Dispatcher method before starting it.");
		return mainQueue;
	}
	
	public static DispatchQueue createQueue() {
		DispatchQueue newQueue = new PoolQueue(service);
		queues.add(newQueue);
		return newQueue;
	}
	
	public static DispatchQueue createNativeQueue() {
		NativeQueue newQueue = new NativeQueue();
		new Thread(newQueue).start();
		queues.add(newQueue);
		return newQueue;
	}
	
}

package threading.queue;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import threading.box.Box;
import threading.box.FutureBox;
import threading.box.UnsafeBox;

public abstract class DispatchQueue {
	
	protected ConcurrentLinkedQueue<Runnable> tasks = new ConcurrentLinkedQueue<>();
	protected AtomicBoolean running = new AtomicBoolean(true); //default running
	
	public final boolean running() {
		return running.get();
	}
	
	/*
	 * Blocks until all tasks passed.
	 */
	public void stop() {
		running.set(false);
		while (!tasks.isEmpty()) { 
			try {
			Thread.sleep(100);
			} catch (InterruptedException e) {} 
		}
	}
	
	public abstract void dispatchAsync(Runnable task);
	
	public <T> FutureBox<T> dispatchAsync(Callable<T> task) {
		FutureBox<T> returnValue = new FutureBox<T>(null);
		this.dispatchAsync(() -> {
			try {
				returnValue.set(task.call());
			} catch (Exception e) {
				returnValue.setException(e);
			}
		});
		return returnValue;
	}
	
	/*
	 * THIS METHOD IS DANGEROUS(!)
	 * You may end up in a deadlock, if you have two queues waiting in a synchronized manner on each other.
	 * 
	 * Also this method may cause unpredicatable overhead, if called from a non-native queue,
	 * because it will block the worker thread of the calling queue, possibly resulting in
	 * a new created thread. If possible always use runAsync and let your async-function
	 * queue another task on your old queue to handle completion of your task(!).
	 */
	public abstract void dispatchSync(Runnable task);
	
	public <T> Box<T> dispatchSync(Callable<T> task) {
		Box<T> returnValue = new UnsafeBox<T>(null);
		this.dispatchSync(() -> {
			try {
				returnValue.set(task.call());
			} catch (Exception e) {
				returnValue.setException(e);
			}
		});
		return returnValue;
	}
	
}

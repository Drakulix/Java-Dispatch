package threading.queue;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class PoolQueue extends DispatchQueue {

	private ExecutorService service = null;
	private AtomicBoolean workWaiting = new AtomicBoolean(false);
	
	/*
	 * We need this ugly capsulation, because of the recursive queuing,
	 * otherwise we our function is not completely initialized, which is a requirement.
	 * 
	 * see this not-working code:
	 * 		Runnable task = () -> {
	 * 			task();  // <- Warning task may not be initialized
	 * 		}
	 * 
	 * Also we want recursive queuing instead of recursive calling
	 * to avoid blocking the worker thread(!)
	 */
	
	private Consumer<ExecutorService> mainTask = service -> {
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Consumer<Consumer> helper = recursive -> {
			
			/*
			 * This would have been nice, but it blocks the worker thread, so avoid it
			synchronized(this) {
				while (tasks.peek() == null) {
					this.wait(); 
					
				}
			}
			 * So we cancel instead and start it again, when necessary
			 */
			
			synchronized(workWaiting) {
				if (tasks.isEmpty()) {
					workWaiting.set(false);
					return;
				}
			}
			
			/*
			 * Finally start executing
			 */
			
			while (!tasks.isEmpty()) {
				Runnable task = tasks.poll();
				synchronized(task) {
					while (true) {
						try {
							service.submit(task).get();
						} catch (CancellationException e) {}
						  catch (ExecutionException e) {
							  e.printStackTrace();
						  }
						  catch (InterruptedException e) {
							  continue;
						}
						break;
					}
					task.notify();
				}
			}
			
			/*
			 * And try again
			 */
			if (running.get()) service.execute(() -> {recursive.accept(recursive);});
		
		};
		helper.accept(helper);
	};
	
	public PoolQueue(ExecutorService service) {
		this.service = service;
	}
	
	public void start() {
		service.execute(() -> { mainTask.accept(service); });
	}
	
	@Override
	public void dispatchAsync(Runnable task) {
		tasks.add(task);
		synchronized(workWaiting) {
			if (workWaiting.getAndSet(true) == false) {
				this.start(); //start if it is not running
			}
		}
	}
	
	/*
	 * See DispatchQueue Documentation(!)
	 */
	@Override
	public void dispatchSync(Runnable task) {
		synchronized(task) {
			tasks.add(task);
			synchronized(workWaiting) {
				if (workWaiting.getAndSet(true) == false) {
					this.start(); //start if it is not running
				}
			}
			while (true) {
				try {
					task.wait();
				} catch (InterruptedException e) {
					continue;
				}
				break;
			}
		}
	}

}

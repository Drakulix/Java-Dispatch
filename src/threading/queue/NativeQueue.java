package threading.queue;


public class NativeQueue extends DispatchQueue implements Runnable {

	private long ThreadId;

	public void run() {

		this.ThreadId = Thread.currentThread().getId();
		
		while (running.get()) {
			
			/*
			 * Here we can finally wait, because this is a full featured thread(!)
			 */
			synchronized(this) {	
				while (tasks.isEmpty()) {
					try {
						this.wait();
					} catch (InterruptedException e) {}
				}
			}


			/*
			 * And start executing
			 */
			while (!tasks.isEmpty()) {
				Runnable task = tasks.poll();
				synchronized(task) {
					try {
						task.run();
					} catch (Exception e) {
						e.printStackTrace();
					}
					task.notify();
				}
			}
		}
	}
	
	@Override
	public void dispatchAsync(Runnable task) {
		tasks.add(task);
		synchronized(this) {
			this.notify();
		}
	}

	public boolean isCurrentThread() {
		return Thread.currentThread().getId() == this.ThreadId;
	}
	
	/* *
	 * See Dispatch-Queue Documentation(!)
	 */
	@Override
	public void dispatchSync(Runnable task) {
		synchronized(task) {
			if (isCurrentThread()) {
				task.run();
			} else {
				tasks.add(task);
				synchronized(this) {
					this.notify();
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

}

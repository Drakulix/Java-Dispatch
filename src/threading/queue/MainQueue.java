package threading.queue;


public class MainQueue extends NativeQueue {

	@Override
	public void stop() {
		running.set(false);
	}
	
}

package threading.box;

import threading.queue.CapturedTaskException;

public class ThreadSafeBox<T> extends Box<T> {

	public ThreadSafeBox () {
		this(null);
	}
	
	public ThreadSafeBox(T initial) {
		super(initial);
	}

	@Override
	public T get() throws CapturedTaskException {
		if (e != null)
			throw e;
		synchronized(boxedVariable) {
			return boxedVariable;
		}
	}

	@Override
	public void set(T newBox) {
		synchronized(boxedVariable) {
			boxedVariable = newBox;
		}
	}

}

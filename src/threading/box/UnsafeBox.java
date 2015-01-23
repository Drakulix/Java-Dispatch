package threading.box;


import threading.queue.CapturedTaskException;

public class UnsafeBox<T> extends Box<T> {
	
	public UnsafeBox () {
		this(null);
	}
	
	public UnsafeBox(T initial) {
		super(initial);
	}

	@Override
	public T get() throws CapturedTaskException  {
		if (e != null)
			throw e;
		return boxedVariable;
	}

	@Override
	public void set(T newBox) {
		boxedVariable = newBox;
	}

}

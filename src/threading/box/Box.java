package threading.box;

import threading.queue.CapturedTaskException;

public abstract class Box<T> {

	T boxedVariable;
	CapturedTaskException e = null;
	
	public Box () {
		this(null);
	}
	
	public Box (T initial) {
		boxedVariable = initial;
	}
	
	public abstract T get() throws CapturedTaskException;
	public abstract void set(T newBox);
	
	public void setException(Exception e) {
		this.e = new CapturedTaskException(e);
	}
	
}

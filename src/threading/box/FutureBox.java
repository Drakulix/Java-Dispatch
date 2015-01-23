package threading.box;

import threading.queue.CapturedTaskException;

public final class FutureBox<T> extends Box<T> {

	public FutureBox () {
		this(null);
	}
	
	public FutureBox(T initial) {
		super(initial);
	}

	boolean changed = false;
	
	@Override
	public T get() throws CapturedTaskException {
		synchronized(this) {
			if (changed == false) {
				while (true) {
					try {
						this.wait();
						break;
					} catch (InterruptedException e) {
						continue;
					}
				}
			}
			if (e != null)
				throw e;
			T returnVal = boxedVariable;
			changed = false;
			return returnVal;
		}
	}

	@Override
	public void set(T newBox) {
		synchronized(this) {
			boxedVariable = newBox;
			changed = true;
			this.notifyAll();
		}
	}
	
	public void setException(Exception e) {
		synchronized(this) {
			this.e = new CapturedTaskException(e);
			changed = true;
			this.notifyAll();
		}
	}

}

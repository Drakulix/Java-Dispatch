package threading.queue;

/**
 * Created by vbrekenfeld on 23.01.15.
 */
public class CapturedTaskException extends RuntimeException {

    Exception capturedException;
    public CapturedTaskException(Exception e) {
        this.capturedException = e;
    }

    @Override
    public void printStackTrace() {
        capturedException.printStackTrace();
    }

}

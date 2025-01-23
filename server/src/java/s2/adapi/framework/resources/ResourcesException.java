package s2.adapi.framework.resources;

/**
 * Exception related resources.
 *
 * @author kimhd
 * @since 1.0
 */
public class ResourcesException extends RuntimeException {

	private static final long serialVersionUID = -2834647161724260138L;

	public ResourcesException(String message) {
        super(message, null);
    }

    public ResourcesException(Throwable rootCause) {
        super(null,rootCause);
    }

    public ResourcesException(String message, Throwable rootCause) {
        super(message,rootCause);
    }
}



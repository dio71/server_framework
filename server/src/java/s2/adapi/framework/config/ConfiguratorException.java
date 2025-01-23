package s2.adapi.framework.config;

/**
 * Exception class occurs during configruation.
 *
 * @author kimhd
 * @since 1.0
 */
public class ConfiguratorException extends Exception {

	private static final long serialVersionUID = -4518890644234626888L;

	/**
     * Default constructor
     */
    public ConfiguratorException() {
        super();
    }

    /**
     * Constructor with an error message.
     *
     * @param msg an error message
     */
    public ConfiguratorException(String msg) {
        super(msg);
    }

    /**
     * Constructor with a caused exception.
     *
     * @param cause a throwable instance causes this exception.
     */
    public ConfiguratorException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with an error message and a caused exception.
     *
     * @param msg an error message
     * @param cause a throwable instance causes this exception.
     */
    public ConfiguratorException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

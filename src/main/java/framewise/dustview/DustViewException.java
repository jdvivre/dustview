package framewise.dustview;

/**
 *
 * Exception class when Dust Rendering..
 *
 * @author chanwook
 */
public class DustViewException extends RuntimeException {
    public DustViewException(String message, Exception e) {
        super(message, e);
    }

    public DustViewException(String message) {
        super(message);
    }

    public DustViewException(Exception e) {
        super(e);
    }
}

package framewise.dustview;

/**
 * Created with IntelliJ IDEA.
 * User: chanwook
 * Date: 2013. 12. 8.
 * Time: 오후 3:31
 * To change this template use File | Settings | File Templates.
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

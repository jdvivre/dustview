package framewise.dustview;

/**
 * Created by chanwook on 2014. 1. 15..
 */
public interface DustViewErrorHandler {

    /**
     * throw exception if occurred
     *
     * @param templateKey
     * @param renderedView
     */
    void checkError(String templateKey, String renderedView);
}

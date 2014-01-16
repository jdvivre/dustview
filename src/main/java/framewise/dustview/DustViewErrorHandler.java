package framewise.dustview;

import java.io.StringWriter;

/**
 * Additional error handler for dust renering result.
 *
 * @author chanwook
 */
public interface DustViewErrorHandler {

    /**
     * throw exception if occurred
     *
     * @param templateKey
     * @param errorWriter
     * @param viewEncoding
     */
    void checkError(String templateKey, StringWriter errorWriter, String viewEncoding) throws Exception;
}

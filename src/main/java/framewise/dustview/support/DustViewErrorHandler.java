package framewise.dustview.support;

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
    void handleError(String templateKey, StringWriter errorWriter, String viewEncoding) throws Exception;
}

package framewise.dustview.support.springmvc;

import framewise.dustview.DustViewException;
import framewise.dustview.support.DustViewErrorHandler;
import org.springframework.util.StringUtils;

import java.io.StringWriter;

/**
 * Default implementation for DustViewErrorHandler
 *
 * @author chanwook
 */
public class DefaultDustViewErrorHandler implements DustViewErrorHandler {

    /**
     * Throw exception if has error message in rhino error buffer
     *
     * @param templateKey
     * @param errorWriter
     * @param viewEncoding
     * @throws Exception
     */
    public void handleError(String templateKey, StringWriter errorWriter, String viewEncoding) throws Exception {
        String errorMessage = new String(errorWriter.getBuffer().toString().getBytes(viewEncoding), viewEncoding);
        if (StringUtils.hasText(errorMessage)) {
            throw new DustViewException("Exception thrown when rendering! templateKey: " + templateKey +
                    ", caused by: " + errorMessage);
        }
    }
}

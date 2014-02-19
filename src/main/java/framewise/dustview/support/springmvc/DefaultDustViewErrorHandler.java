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
    public void checkError(String templateKey, StringWriter errorWriter, String viewEncoding) throws Exception {
        String errorMessage = new String(errorWriter.getBuffer().toString().getBytes(viewEncoding), viewEncoding);
        if (StringUtils.hasText(errorMessage)) {
            throw new DustViewException("Exception thrown when rendering! templatekey: " + templateKey + ", caused by: " + errorMessage);
        }
    }
}

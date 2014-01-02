package framewise.dustview;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;

/**
 * Created with IntelliJ IDEA.
 * User: chanwook
 * Date: 2013. 12. 8.
 * Time: 오후 3:54
 * To change this template use File | Settings | File Templates.
 */
public class HttpConnectDustViewTemplateLoader implements DustViewTemplateLoader {

    public static final String DEFAULT_FROM_ENCODING = "ISO-8859-1";
    public static final String DEFAULT_TO_ENCODING = "UTF-8";

    private RestTemplate restTemplate = new RestTemplate();
    private String fromEncoding = DEFAULT_FROM_ENCODING;
    private String toEncoding = DEFAULT_TO_ENCODING;

    @Override
    public String loadFile(String viewFile) {

        if (viewFile.startsWith("http://")) {
            try {
                ResponseEntity<String> responseEntity =
                        restTemplate.exchange(viewFile, HttpMethod.GET, new HttpEntity<String>(
                                new HttpHeaders()), String.class);
                String rawTemplate = responseEntity.getBody();
                String encodedTemplateSource = new String(rawTemplate.getBytes(Charset.forName(fromEncoding)), toEncoding);
                return encodedTemplateSource;
            } catch (Exception e) {
                throw new DustViewException("Failed to load Dust Tempmlate.", e);
            }
        } else {
            throw new DustViewException("View path must start with 'http://~~' statement");
        }
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}

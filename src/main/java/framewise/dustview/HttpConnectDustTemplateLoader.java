package framewise.dustview;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;

/**
 * This class support to loading template file where is remote repository(ex. CDN)
 *
 * @author chanwook
 */
public class HttpConnectDustTemplateLoader implements DustTemplateLoader {

    private static final String DEFAULT_FROM_ENCODING = "ISO-8859-1";
    private static final String DEFAULT_TO_ENCODING = "UTF-8";

    private RestTemplate restTemplate = new RestTemplate();

    private String fromEncoding = DEFAULT_FROM_ENCODING;
    private String toEncoding = DEFAULT_TO_ENCODING;

    @Override
    public String loadTemplate(String templatePath) {

        if (templatePath.startsWith("http://") || templatePath.startsWith("https://")) {
            try {
                ResponseEntity<String> responseEntity =
                        restTemplate.exchange(templatePath, HttpMethod.GET, new HttpEntity<String>(
                                new HttpHeaders()), String.class);
                if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
                    throw new DustViewException("Failed load template source!(status code: " + responseEntity.getStatusCode() + ", reason: " + responseEntity.getBody());
                }
                String rawTemplate = responseEntity.getBody();
                return new String(rawTemplate.getBytes(Charset.forName(fromEncoding)), toEncoding);
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

    public void setFromEncoding(String fromEncoding) {
        this.fromEncoding = fromEncoding;
    }

    public void setToEncoding(String toEncoding) {
        this.toEncoding = toEncoding;
    }
}

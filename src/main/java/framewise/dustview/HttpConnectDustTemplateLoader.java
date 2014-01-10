package framewise.dustview;

import java.nio.charset.Charset;

import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * This class support to loading template file where is remote repository(ex. CDN)
 *
 * @author chanwook
 */
public class HttpConnectDustTemplateLoader implements DustTemplateLoader {

    private static final String DEFAULT_FROM_ENCODING = "ISO-8859-1";
    private static final String DEFAULT_TO_ENCODING = "UTF-8";
    private static final String DEFAULT_RESOURCE_ENCODING = "UTF-8";

    private RestTemplate restTemplate = new RestTemplate();

    private String fromEncoding = DEFAULT_FROM_ENCODING;
    private String toEncoding = DEFAULT_TO_ENCODING;

    private String resourceEncoding = DEFAULT_RESOURCE_ENCODING;

    @Override
    public String loadTemplate(String templatePath) {

        if (templatePath.startsWith("http://") || templatePath.startsWith("https://")) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Accept", "text/html;charset=" + resourceEncoding);

                ResponseEntity<String> responseEntity =
                        restTemplate.exchange(templatePath, HttpMethod.GET, new HttpEntity<String>(
                                headers), String.class);
                if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
                    throw new DustViewException("Failed load template source!(status code: " + responseEntity.getStatusCode() + ", reason: " + responseEntity.getBody());
                }
                
                String rawTemplate = responseEntity.getBody();
                if(StringUtils.hasText(fromEncoding) && StringUtils.hasText(toEncoding)) {
                	rawTemplate = new String(rawTemplate.getBytes(Charset.forName(fromEncoding)), toEncoding);
                }
                
                return rawTemplate;
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

    public void setResourceEncoding(String resourceEncoding) {
        this.resourceEncoding = resourceEncoding;
    }
}

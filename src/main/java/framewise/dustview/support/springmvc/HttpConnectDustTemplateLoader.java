package framewise.dustview.support.springmvc;

import framewise.dustview.DustViewException;
import framewise.dustview.support.DustTemplateLoader;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;

/**
 * This class support to loading template file where is remote repository(ex. CDN)
 *
 * @author chanwook
 */
public class HttpConnectDustTemplateLoader implements DustTemplateLoader {

    private static final String DEFAULT_RESOURCE_ENCODING = "UTF-8";

    private RestTemplate restTemplate = new RestTemplate();

    private String fromEncoding = "ISO-8859-1";
    private String toEncoding = "UTF-8";

    private String resourceEncoding = DEFAULT_RESOURCE_ENCODING;

    @Override
    public String loadTemplate(String templatePath) {

        if (templatePath.startsWith("http://") || templatePath.startsWith("https://")) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Accept", "text/html;charset=" + resourceEncoding);

                ResponseEntity<String> responseEntity =
                        restTemplate.exchange(templatePath, HttpMethod.GET,
                                new HttpEntity<String>(headers), String.class);

                if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
                    throw new DustViewException("Failed load template source!(status code: " +
                            responseEntity.getStatusCode() + ", reason: " + responseEntity.getBody());
                }

                String templateSource = responseEntity.getBody();
                if (StringUtils.hasText(fromEncoding) && StringUtils.hasText(toEncoding)) {
                    templateSource = new String(templateSource.getBytes(Charset.forName(fromEncoding)), toEncoding);
                }

                return templateSource;
            } catch (Exception e) {
                throw new DustViewException("Failed to load Dust Template", e);
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

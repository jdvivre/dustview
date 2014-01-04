package framewise.dustview;

import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author chanwook
 */
public class HttpConnectDustTemplateLoaderTest {

    HttpConnectDustTemplateLoader loader = new HttpConnectDustTemplateLoader();


    public HttpConnectDustTemplateLoaderTest() {
        loader.setRestTemplate(new MockRestTemplate());
    }

    @Test
    public void success() {
        String template = loader.loadTemplate("http://test.com/test.html");
        assertThat(template, is("Hello!{name}!"));
    }

    static class MockRestTemplate extends RestTemplate {

        @Override
        public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables) throws RestClientException {
            if ("http://test.com/test.html".equals(url)) {
                return new ResponseEntity("Hello!{name}!", HttpStatus.OK);
            }
            throw new RuntimeException("test fail!");
        }
    }

}

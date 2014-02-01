package framewise.dustview;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;


/**
 * @author chanwook
 */
public class SimpleDustTemplateViewTest {

    SimpleDustTemplateView v = new SimpleDustTemplateView();

    @Test
    public void createViewTemplate() {
        assertThat(v.getViewTemplateLoader(), nullValue());

        MockTemplateLoader mockTemplateLoader = new MockTemplateLoader();
        HashMap<String, Object> attributeMap = new HashMap<String, Object>();
        attributeMap.put(SimpleDustTemplateView.TEMPLATE_LOADER, mockTemplateLoader);
        v.setAttributesMap(attributeMap);

        v.initApplicationContext();

        assertThat(v.getViewTemplateLoader(), CoreMatchers.<DustTemplateLoader>equalTo(mockTemplateLoader));
    }

    @Test
    public void configViewPath() {
        assertThat(v.getViewPrefixPath(), is(""));
        assertThat(v.getViewSuffixPath(), is(""));

        HashMap<String, Object> attributeMap = new HashMap<String, Object>();
        attributeMap.put(SimpleDustTemplateView.VIEW_PATH_PREFIX, "http://...");
        attributeMap.put(SimpleDustTemplateView.VIEW_PATH_SUFFIX, "/markup.js");
        v.setAttributesMap(attributeMap);

        v.initApplicationContext();

        assertThat(v.getViewPrefixPath(), is("http://..."));
        assertThat(v.getViewSuffixPath(), is("/markup.js"));
    }

    @Test
    public void sendRefreshParam() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String templateKey = "test";

        boolean refreshParam = v.getRefreshParam(templateKey, request);
        assertThat(refreshParam, is(false));

        request.setParameter("_refresh", "y");
        refreshParam = v.getRefreshParam(templateKey, request);
        assertThat(refreshParam, is(true));

        request.setParameter("_refresh", "Y");
        refreshParam = v.getRefreshParam(templateKey, request);
        assertThat(refreshParam, is(true));

        request.setParameter("_refresh", "n");
        refreshParam = v.getRefreshParam(templateKey, request);
        assertThat(refreshParam, is(false));
    }

    @Test
    public void refreshViewWhenSendParam() {
        final String refreshSrc = "<html>refresh</html>";
        final String cacheSrc = "<html>cache</html>";
        final String viewPath = "view1";
        final String templateKey = "templateKey1";

        v.setViewTemplateLoader(new DustTemplateLoader() {
            @Override
            public String loadTemplate(String templatePath) {
                return refreshSrc;
            }
        });
        v.setViewSourceCacheProvider(new InMemoryViewSourceCacheProvider() {
            @Override
            public void add(String viewPath, String templateSource) {
                super.add(viewPath, cacheSrc);
            }
        });
        // new - first call
        boolean result = v.loadTemplateSource(templateKey, viewPath, false);
        assertThat(result, is(false));

        // cached src
        result = v.loadTemplateSource(templateKey, viewPath, false);
        assertThat(result, is(true));

        // new - refresh call
        result = v.loadTemplateSource(templateKey, viewPath, true);
        assertThat(result, is(false));

        // cached src
        result = v.loadTemplateSource(templateKey, viewPath, false);
        assertThat(result, is(true));

        // disable cache
        v.setViewCacheable(false);
        result = v.loadTemplateSource(templateKey, viewPath, true);
        assertThat(result, is(false));

        v.setViewCacheable(false);
        result = v.loadTemplateSource(templateKey, viewPath, false);
        assertThat(result, is(false));
    }

    @Test
    public void loadTemplateRealAndCache() {
        DustTemplateEngine e = new DustTemplateEngine();

        String key1 = "t1";
        String source1 = "<html>1</html>";
        String key2 = "t2";
        String source2 = "<html>2</html>";
        String key3 = "t3";
        String source3 = "<html>3</html>";
        boolean result = e.load(key1, source1);
        assertThat(true, is(result));
        result = e.load(key1, source1);
        assertThat(false, is(result));//이제부터는 캐시!
        result = e.load(key1, source1);
        assertThat(false, is(result));
        result = e.load(key1, source1);
        assertThat(false, is(result));
        result = e.load(key1, source1);
        assertThat(false, is(result));
        result = e.load(key1, source1);
        assertThat(false, is(result));
        result = e.load(key1, source1);
        assertThat(false, is(result));
        result = e.load(key1, "<html>1</html>");
        assertThat(false, is(result));
        result = e.load(key1, "<html>1</html>");
        assertThat(false, is(result));

        // 두 번째 view
        result = e.load(key2, source2);
        assertThat(true, is(result));
        result = e.load(key2, source2);
        assertThat(false, is(result));
        result = e.load(key2, source2);
        assertThat(false, is(result));
        result = e.load(key2, "<html>2</html>");
        assertThat(false, is(result));
        result = e.load(key2, "<html>2</html>");
        assertThat(false, is(result));

        // 세 번째 view
        result = e.load(key3, source3);
        assertThat(true, is(result));
        result = e.load(key3, source3);
        assertThat(false, is(result));
        result = e.load(key3, source3);
        assertThat(false, is(result));
        result = e.load(key3, "<html>3</html>");
        assertThat(false, is(result));
        result = e.load(key3, "<html>3</html>");
        assertThat(false, is(result));
    }

    static class MockTemplateLoader implements DustTemplateLoader {
        @Override
        public String loadTemplate(String templatePath) {
            return null;
        }
    }
}

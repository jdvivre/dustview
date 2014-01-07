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

        v.resolvePropertyByViewAttribute();

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

        v.resolvePropertyByViewAttribute();

        assertThat(v.getViewPrefixPath(), is("http://..."));
        assertThat(v.getViewSuffixPath(), is("/markup.js"));
    }

    @Test
    public void sendRefreshParam() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        boolean refreshParam = v.getRefreshParam(request);
        assertThat(refreshParam, is(false));

        request.setParameter("_refresh", "y");
        refreshParam = v.getRefreshParam(request);
        assertThat(refreshParam, is(true));

        request.setParameter("_refresh", "Y");
        refreshParam = v.getRefreshParam(request);
        assertThat(refreshParam, is(true));

        request.setParameter("_refresh", "n");
        refreshParam = v.getRefreshParam(request);
        assertThat(refreshParam, is(false));
    }

    @Test
    public void refreshViewWhenSendParam() {
        final String refreshSrc = "<html>refresh</html>";
        final String cacheSrc = "<html>cache</html>";
        final String viewPath = "view1";

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
        String src = v.loadTemplateSource(viewPath, false);
        assertThat(src, is(refreshSrc));

        // cached src
        src = v.loadTemplateSource(viewPath, false);
        assertThat(src, is(cacheSrc));

        // new - refresh call
        src = v.loadTemplateSource(viewPath, true);
        assertThat(src, is(refreshSrc));

        // cached src
        src = v.loadTemplateSource(viewPath, false);
        assertThat(src, is(cacheSrc));

        // disable cache
        v.setViewCacheable(false);
        src = v.loadTemplateSource(viewPath, true);
        assertThat(src, is(refreshSrc));

        v.setViewCacheable(false);
        src = v.loadTemplateSource(viewPath, false);
        assertThat(src, is(refreshSrc));
    }

    static class MockTemplateLoader implements DustTemplateLoader {
        @Override
        public String loadTemplate(String templatePath) {
            return null;
        }
    }
}

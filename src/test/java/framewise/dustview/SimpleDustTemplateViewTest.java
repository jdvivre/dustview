package framewise.dustview;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;


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
        final String cacheKey = "cachekey1";

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
        String src = v.loadTemplateSource(viewPath, cacheKey, false);
        assertThat(src, is(refreshSrc));

        // cached src
        src = v.loadTemplateSource(viewPath, cacheKey, false);
        assertThat(src, is(cacheSrc));

        // new - refresh call
        src = v.loadTemplateSource(viewPath, cacheKey, true);
        assertThat(src, is(refreshSrc));

        // cached src
        src = v.loadTemplateSource(viewPath, cacheKey, false);
        assertThat(src, is(cacheSrc));

        // disable cache
        v.setViewCacheable(false);
        src = v.loadTemplateSource(viewPath, cacheKey, true);
        assertThat(src, is(refreshSrc));

        v.setViewCacheable(false);
        src = v.loadTemplateSource(viewPath, cacheKey, false);
        assertThat(src, is(refreshSrc));
    }
    
    @Test
	public void cacheKey() throws Exception {
    	
    	v.setViewSuffixPath("/markup.js");
    	Map<String, String> model = new HashMap<String, String>();
    	
    	model.put(SimpleDustTemplateView.VIEW_FILE_PATH, "path1/path2");
    	String dustViewCacheKey = v.getDustViewCacheKey(model);
    	assertThat("path1/path2/markup.js", is(dustViewCacheKey));
    	
    	model.put(SimpleDustTemplateView.VIEW_FILE_PATH, "path1/path2/");
    	dustViewCacheKey = v.getDustViewCacheKey(model);
    	assertThat("path1/path2/markup.js", is(dustViewCacheKey));

    	model.put(SimpleDustTemplateView.VIEW_FILE_PATH, "/path1/path2/");
    	dustViewCacheKey = v.getDustViewCacheKey(model);
    	assertThat("path1/path2/markup.js", is(dustViewCacheKey));
		
	}

    static class MockTemplateLoader implements DustTemplateLoader {
        @Override
        public String loadTemplate(String templatePath) {
            return null;
        }
    }
}

package framewise.dustview.support.springmvc;

import framewise.dustview.core.DustTemplateEngine;
import framewise.dustview.support.DustTemplateLoader;
import framewise.dustview.support.DustViewConstants;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;


/**
 * @author chanwook
 */
public class SimpleDustTemplateViewTest {

    SimpleDustTemplateView v = new SimpleDustTemplateView(true);

    @Test
    public void createViewTemplate() throws Exception {
        assertThat(v.getViewTemplateLoader(), nullValue());

        MockTemplateLoader mockTemplateLoader = new MockTemplateLoader();
        HashMap<String, Object> attributeMap = new HashMap<String, Object>();
        attributeMap.put(DustViewConstants.TEMPLATE_LOADER, mockTemplateLoader);
        v.setAttributesMap(attributeMap);

        v.afterPropertiesSet();

        assertThat(v.getViewTemplateLoader(), CoreMatchers.<DustTemplateLoader>equalTo(mockTemplateLoader));
    }

    @Test
    public void configViewPath() throws Exception {
        assertThat(v.getViewPrefixPath(), is(""));
        assertThat(v.getViewSuffixPath(), is(""));

        HashMap<String, Object> attributeMap = new HashMap<String, Object>();
        attributeMap.put(DustViewConstants.VIEW_PATH_PREFIX, "http://...");
        attributeMap.put(DustViewConstants.VIEW_PATH_SUFFIX, "/markup.js");
        v.setAttributesMap(attributeMap);

        v.afterPropertiesSet();

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
        boolean result = v.loadSingleTemplateSource(templateKey, viewPath, false);
        assertThat(result, is(false));

        // cached src
        result = v.loadSingleTemplateSource(templateKey, viewPath, false);
        assertThat(result, is(true));

        // new - refresh call
        result = v.loadSingleTemplateSource(templateKey, viewPath, true);
        assertThat(result, is(false));

        // cached src
        result = v.loadSingleTemplateSource(templateKey, viewPath, false);
        assertThat(result, is(true));

        // disable cache
        v.setViewCacheable(false);
        result = v.loadSingleTemplateSource(templateKey, viewPath, true);
        assertThat(result, is(false));

        v.setViewCacheable(false);
        result = v.loadSingleTemplateSource(templateKey, viewPath, false);
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

    @Test
    public void compiled() {
        String key = "key";
        String source = "<html></html>";
        String compiled = "function()";


        DustTemplateEngine mock = mock(DustTemplateEngine.class);
        v.setDustEngine(mock);
        v.setViewCacheable(false);
        // default
        when(mock.compile(key, source)).thenReturn(compiled);
        v.loadResourceToScriptEngine(key, "path", source);
        verify(mock).load(key, source);

        // compiled false, then do compile in runtime
        v.setCompiled(false);
        v.loadResourceToScriptEngine(key, "path", source);
        verify(mock).load(key, compiled);

        // compiled true, then do not compile in runtime
        mock = mock(DustTemplateEngine.class);
        when(mock.compile(key, source)).thenReturn(compiled);
        v.setDustEngine(mock);
        v.setCompiled(true);
        v.loadResourceToScriptEngine(key, "path", source);
        verify(mock).load(key, source);
    }

    @Test
    public void loadMultipleTemplate() throws IOException {
        //init
        v.setViewTemplateLoader(new ClasspathSupportFileSystemDustTemplateLoader());
        v.setCompiled(false);

        String masterKey = "master";
        String path = "/template/multiple";

        v.loadMultiTemplateSource(path, false);
        String view = v.renderingView(masterKey, "{}");

        assertEquals("<h1>master</h1><h1>partial1</h1><h1>partial2</h1>", view);
    }

    @Test
    public void loadSingleAndMultiTemplate() {
        //init
        v.setViewTemplateLoader(new ClasspathSupportFileSystemDustTemplateLoader());
        v.setCompiled(false);
        v.setMultiLoad(true);

        MockHttpServletRequest request = new MockHttpServletRequest();

        // multi load
        String templateKey = "master";
        String path = "/template/multiple";

        v.loadTemplateSource(request, templateKey, path);
        String view = v.renderingView(templateKey, "{}");

        assertEquals("<h1>master</h1><h1>partial1</h1><h1>partial2</h1>", view);

        // single load
        templateKey = "partial1";
        path = "/template/multiple/partial1.html";

        v.loadTemplateSource(request, templateKey, path);
        view = v.renderingView(templateKey, "{}");

        assertEquals("<h1>partial1</h1>", view);

    }

    static class MockTemplateLoader implements DustTemplateLoader {
        @Override
        public String loadTemplate(String templatePath) {
            return null;
        }
    }
}

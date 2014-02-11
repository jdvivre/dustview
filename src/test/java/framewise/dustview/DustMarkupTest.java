package framewise.dustview;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ModelMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by chanwook on 2014. 2. 11..
 */
public class DustMarkupTest {

    SimpleDustTemplateView v = new SimpleDustTemplateView();
    ObjectMapper m = new ObjectMapper();

    public DustMarkupTest() throws Exception {
        // init for 2.x
        v.getDustEngine().setDustJsFilePath("/dust/dust-full-2.2.3.js");
        v.getDustEngine().initializeContext();

        v.setViewTemplateLoader(new ClasspathSupportFileSystemDustTemplateLoader());
        v.setCompiled(false);
    }

    @Test
    public void basic1() {
        ModelMap reqModel = createModelMap("basic1");
        Map<String, Object> m = rendering(reqModel);

        assertThat(getView(m), is("Famous People<ul><li>Larry</li><li>Curly</li><li>Moe</li></ul>"));
    }

    //TODO {_json|jp} 추가 필요
    @Test
    public void escape() {
        ModelMap reqModel = createModelMap("escape");
        Map<String, Object> m = rendering(reqModel);

        assertThat(getView(m), is("0.name is me 1.name is me 2.name is me 3.name is me 4.http://github.com?p=%EA%B0%92 5.http%3A%2F%2Fgithub.com%3Fp%3D%EA%B0%92 6.&quot;name is me&quot; 6.&quot;http://github.com?p=값&quot; "));
    }

    @Test
    public void section() {
        ModelMap reqModel = createModelMap("section");
        Map<String, Object> m = rendering(reqModel);

        assertThat(getView(m), is("<h1>Title</h1><span>name1-value1</span>"));
    }

    @Test
    public void context() {
        ModelMap reqModel = createModelMap("context");
        Map<String, Object> m = rendering(reqModel);

        assertThat(getView(m), is("<h1>Title</h1><span>name1-value1</span>/* walk up to parent of context */<span>name2</span>"));
    }

    /**
     * https://github.com/linkedin/dustjs/wiki/Dust-Tutorial#wiki-paths
     */
    @Test
    public void context2() {
        ModelMap reqModel = createModelMap("context2");
        Map<String, Object> m = rendering(reqModel);

        assertThat(getView(m), is("<h1>Title</h1><span>b: name1</span><span>c: name3</span><span>a: name2</span>"));
    }

    protected String getView(Map<String, Object> m) {
        return (String) m.get(SimpleDustTemplateView.DEFAULT_EXPORT_VIEW_SOURCE_KEY);
    }

    protected Map<String, Object> rendering(ModelMap reqModel) {
        return v.createMergedOutputModel(reqModel, new MockHttpServletRequest(), new MockHttpServletResponse());
    }

    protected ModelMap createModelMap(String key) {
        ModelMap reqModel = new ModelMap();
        reqModel.put(SimpleDustTemplateView.TEMPLATE_KEY, key);
        reqModel.put(SimpleDustTemplateView.CONTENT_TEXT_KEY, loadJson(key));
        reqModel.put(SimpleDustTemplateView.VIEW_FILE_PATH, "/template/" + key + ".html");
        return reqModel;
    }

    private String loadJson(String path) {
        ClassPathResource resource = new ClassPathResource("/json/" + path + ".json");
        try {
            return new String(Files.readAllBytes(Paths.get(resource.getURI())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

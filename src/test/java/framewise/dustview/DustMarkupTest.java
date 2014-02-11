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
    public void basic1() throws IOException {
        ModelMap reqModel = createModelMap("basic1");

        Map<String, Object> m = rendering(reqModel);

        assertThat(getView(m), is("Famous People<ul><li>Larry</li><li>Curly</li><li>Moe</li></ul>"));
    }

    protected String getView(Map<String, Object> m) {
        return (String) m.get(SimpleDustTemplateView.DEFAULT_EXPORT_VIEW_SOURCE_KEY);
    }

    protected Map<String, Object> rendering(ModelMap reqModel) {
        return v.createMergedOutputModel(reqModel, new MockHttpServletRequest(), new MockHttpServletResponse());
    }

    protected ModelMap createModelMap(String key) throws IOException {
        ModelMap reqModel = new ModelMap();
        reqModel.put(SimpleDustTemplateView.TEMPLATE_KEY, key);
        reqModel.put(SimpleDustTemplateView.CONTENT_TEXT_KEY, loadJson(key));
        reqModel.put(SimpleDustTemplateView.VIEW_FILE_PATH, "/template/" + key + ".html");
        return reqModel;
    }

    private String loadJson(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource("/json/" + path + ".json");
        return new String(Files.readAllBytes(Paths.get(resource.getURI())));
    }
}

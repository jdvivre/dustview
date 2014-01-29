package framewise.dustview;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.ui.ModelMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by chanwook on 2014. 1. 27..
 */
@ContextConfiguration(locations = {"classpath:applicationContext-test.xml"})
public class SimpleDustTemplateViewIntegrationTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    SimpleDustTemplateView v;

    @Autowired
    Environment env;

    @Test
    public void loadViewByPropertiesPath() {
//        final ModelMap modelMap = new ModelMap();
//        modelMap.put(SimpleDustTemplateView.VIEW_PATH_KEY, "viewkey");
//        String viewPath = v.getViewPath(modelMap);
//        String viewPath = env.getProperty("viewkey");
//        assertThat("/ui/path/markup.js", is(viewPath));
    }
}

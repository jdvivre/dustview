package framewise.dustview.support.springmvc;

import framewise.dustview.DustViewException;
import framewise.dustview.support.springmvc.ClasspathSupportFileSystemDustTemplateLoader;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * @author chanwook
 */
public class ClasspathSupportFileSystemDustTemplateLoaderTest {

    ClasspathSupportFileSystemDustTemplateLoader l = new ClasspathSupportFileSystemDustTemplateLoader();

    @Test
    public void success() {
        String template = l.loadTemplate("/testTemplate01.html");
        assertThat(template, is("Hello!{name}!"));
    }

    @Test(expected = DustViewException.class)
    public void fail() {
        l.loadTemplate("/hasNotFile.html");
    }



}

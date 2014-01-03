package framewise.dustview;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author chanwook
 */
public class FileSystemDustTemplateLoaderTests {

    FileSystemDustTemplateLoader l = new FileSystemDustTemplateLoader();

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

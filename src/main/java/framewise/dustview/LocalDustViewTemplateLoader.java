package framewise.dustview;

import org.springframework.core.io.ClassPathResource;

import java.io.DataInputStream;
import java.io.FileInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: chanwook
 * Date: 2013. 12. 8.
 * Time: 오후 4:12
 * To change this template use File | Settings | File Templates.
 */
public class LocalDustViewTemplateLoader implements DustViewTemplateLoader {
    @Override
    public String loadFile(String viewFile) {
        ClassPathResource fileResource = new ClassPathResource(viewFile);
        byte[] bytes = null;
        try {
            bytes = new byte[(int) fileResource.contentLength()];
            new DataInputStream(new FileInputStream(fileResource.getFile())).readFully(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String viewSource = new String(bytes);
        viewSource = viewSource.replaceAll("\r", "");

        return viewSource;

    }
}

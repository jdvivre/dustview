package framewise.dustview;

import org.springframework.core.io.ClassPathResource;

import java.io.DataInputStream;
import java.io.FileInputStream;

/**
 * This class support to loading template file where is local file system.
 *
 * @author chanwook
 */
public class LocalDustViewTemplateLoader implements DustViewTemplateLoader {
    @Override
    public String loadFile(String viewFile) {
        ClassPathResource fileResource = new ClassPathResource(viewFile);

        if (fileResource == null) {
            throw new DustViewException("Template File Not Found in local system!!");
        }

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

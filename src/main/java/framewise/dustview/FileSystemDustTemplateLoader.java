package framewise.dustview;

import org.springframework.core.io.ClassPathResource;

import java.io.DataInputStream;
import java.io.FileInputStream;

/**
 * This class support to loading template file where is local file system.
 *
 * @author chanwook
 */
public class FileSystemDustTemplateLoader implements DustTemplateLoader {

    @Override
    public String loadTemplate(String templatePath) {
        ClassPathResource fileResource = new ClassPathResource(templatePath);

        if (fileResource == null) {
            throw new DustViewException("Template File Not Found in local system!!");
        }


        try {
            byte[] bytes = new byte[(int) fileResource.contentLength()];
            new DataInputStream(new FileInputStream(fileResource.getFile())).readFully(bytes);

            String viewSource = new String(bytes);
            viewSource = viewSource.replaceAll("\r", "");

            return viewSource;
        } catch (Exception e) {
            throw new DustViewException("File Access Exeption to loading Template!", e);
        }
    }
}

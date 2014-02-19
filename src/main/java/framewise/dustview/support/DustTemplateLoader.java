package framewise.dustview.support;

/**
 *
 * This interface is defined to loading template file, HTML.
 *
 * @author chanwook
 */
public interface DustTemplateLoader {

    /**
     * Load Dust Template File.
     * This method is implemented by loading methodologies.
     *
     * @param templatePath
     * @return
     */
    String loadTemplate(String templatePath);

}

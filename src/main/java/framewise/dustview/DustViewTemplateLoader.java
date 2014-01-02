package framewise.dustview;

/**
 *
 * This interface is defined to loading template file, HTML.
 *
 * @author chanwook
 */
public interface DustViewTemplateLoader {

    /**
     * Load Dust Template File.
     * This method is implemented by loading methodologies.
     *
     * @param viewFile
     * @return
     */
    String loadFile(String viewFile);

}

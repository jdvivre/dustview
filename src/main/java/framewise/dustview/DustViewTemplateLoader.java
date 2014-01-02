package framewise.dustview;

/**
 * Created with IntelliJ IDEA.
 * User: chanwook
 * Date: 2013. 12. 8.
 * Time: 오후 3:47
 * To change this template use File | Settings | File Templates.
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

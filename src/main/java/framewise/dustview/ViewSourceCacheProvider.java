package framewise.dustview;

/**
 * This interface is cache interface for view resource.
 *
 * @author chanwook
 */
public interface ViewSourceCacheProvider {

    /**
     * check cached view
     *
     * @param viewPath
     * @return
     */
    boolean isCached(String viewPath);

    /**
     * get cached view
     *
     * @param viewPath
     * @return
     */
    String get(String viewPath);

    /**
     * add to cache
     * @param viewPath
     * @param templateSource
     */
    void add(String viewPath, String templateSource);

}

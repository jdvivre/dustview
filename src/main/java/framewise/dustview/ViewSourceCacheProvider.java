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
     * @param key
     * @return
     */
    boolean isCached(String key);

    /**
     * get cached view
     *
     * @param key
     * @return
     */
    String get(String key);

    /**
     * add to cache
     *
     * @param key
     * @param templateSource
     */
    void add(String key, String templateSource);

    /**
     * remove resource at cache
     *
     * @param key
     * @return
     */
    boolean remove(String key);

}

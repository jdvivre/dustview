package framewise.dustview;

/**
 * Created by chanwook on 2014. 1. 14..
 */
public interface ContentCacheProvider {

    boolean isCached(String templateKey, String json);

    String get(String templateKey);

    void add(String templateKey, String json, String renderedView);
}

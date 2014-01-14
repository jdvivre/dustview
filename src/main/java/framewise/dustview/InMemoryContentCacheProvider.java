package framewise.dustview;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chanwook on 2014. 1. 14..
 */
public class InMemoryContentCacheProvider implements ContentCacheProvider {
    private Map<String, String[]/*JSON, RenderedView*/> cache = new HashMap<String, String[]>();

    @Override
    public boolean isCached(String templateKey, String json) {
        if (cache.containsKey(templateKey)) {
            String cacheJson = cache.get(templateKey)[0];
            if (cacheJson.equals(json)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String get(String templateKey) {
        return cache.get(templateKey)[1];
    }

    @Override
    public void add(String templateKey, String json, String renderedView) {
        if (cache.containsKey(templateKey)) {
            cache.remove(templateKey);
        }
        cache.put(templateKey, new String[]{json, renderedView});
    }
}

package framewise.dustview;

import java.util.HashMap;
import java.util.Map;

/**
 * This class will refresh when send to specific parameter.
 *
 * @author chanwook
 */
public class InMemoryViewSourceCacheProvider implements ViewSourceCacheProvider {

    private Map<String, String> cache = new HashMap<String, String>();

    @Override
    public boolean isCached(String viewPath) {
        return cache.containsKey(viewPath);
    }

    @Override
    public String get(String viewPath) {
        return cache.get(viewPath);
    }

    @Override
    public void add(String viewPath, String templateSource) {
        cache.put(viewPath, templateSource);
    }
}

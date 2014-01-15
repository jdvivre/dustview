package framewise.dustview;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Did not save dust compiled resource to cache when dust engine was loading at first call.
 *
 * @author chanwook
 */
public class PreloadViewSourceCacheProvider implements ViewSourceCacheProvider {

    private final Logger logger = LoggerFactory.getLogger(InMemoryViewSourceCacheProvider.class);

    private List<String> cache = new ArrayList<String>();

    @Override
    public boolean isCached(String key) {
        return cache.contains(key);
    }

    @Override
    public String get(String key) {
        if (logger.isDebugEnabled()) {
            logger.debug("Call cache operation, but PreloadViewSourceCacheProvider class is not return data!(not error!)");
        }
        return "";
    }

    @Override
    public void add(String key, String templateSource) {
        if (logger.isDebugEnabled()) {
            logger.debug("Add dust compiled resource to in-memory cache!(key: " + key + "), but don't save template source.");
        }
        this.cache.add(key);
    }

    @Override
    public boolean remove(String key) {
        return this.cache.remove(key);
    }
}

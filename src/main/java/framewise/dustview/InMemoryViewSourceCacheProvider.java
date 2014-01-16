package framewise.dustview;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * This class will refresh when send to specific parameter.
 *
 * @author chanwook
 */
public class InMemoryViewSourceCacheProvider implements ViewSourceCacheProvider {

    private final Logger logger = LoggerFactory.getLogger(InMemoryViewSourceCacheProvider.class);

    private Map<String, String> cache = new HashMap<String, String>();

    @Override
    public boolean isCached(String key) {
        return cache.containsKey(key);
    }

    @Override
    public String get(String key) {
        if (logger.isDebugEnabled()) {
            logger.debug("Read dust compiled resource in cache!(key: " + key + ")");
        }
        return cache.get(key);
    }

    @Override
    public void add(String key, String templateSource) {
        if (logger.isDebugEnabled()) {
            logger.debug("Add dust compiled resource to in-memory cache!(key: " + key + ", value: " + templateSource);
        }
        cache.put(key, templateSource);
    }

    @Override
    public boolean remove(String key) {
        if (isCached(key)) {
            cache.remove(key);

            if (logger.isDebugEnabled()) {
                logger.debug("Delete cached resource!(key: " + key + ")");
            }

            return true;
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Request for delete cached resource, but not present cache element!(key: " + key + ")");
            }

            return false;
        }
    }

    @Override
    public boolean isReload() {
        return true;
    }
}

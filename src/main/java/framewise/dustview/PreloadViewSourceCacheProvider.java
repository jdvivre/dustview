package framewise.dustview;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chanwook on 2014. 1. 15..
 */
public class PreloadViewSourceCacheProvider implements ViewSourceCacheProvider {
    private List<String> cache = new ArrayList<String>();

    @Override
    public boolean isCached(String viewPath) {
        return cache.contains(viewPath);
    }

    @Override
    public String get(String viewPath) {
        return "";
    }

    @Override
    public void add(String viewPath, String templateSource) {
        this.cache.add(viewPath);
    }

    @Override
    public boolean remove(String viewPath) {
        return this.cache.remove(viewPath);
    }
}

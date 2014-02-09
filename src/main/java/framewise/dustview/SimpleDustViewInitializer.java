package framewise.dustview;

import java.util.Map;

import static framewise.dustview.SimpleDustTemplateView.*;

/**
 * @author chanwook
 */
public class SimpleDustViewInitializer implements DustViewInitializer {

    /**
     * initializing method.
     * Caution: Must not call runtime!!
     *
     * @param attributesMap
     * @param view
     */
    public void initializeViewProperty(Map<String, Object> attributesMap, SimpleDustTemplateView view) {
        if (attributesMap.get(TEMPLATE_LOADER) != null && attributesMap.get(TEMPLATE_LOADER) instanceof DustTemplateLoader) {
            view.setViewTemplateLoader((DustTemplateLoader) attributesMap.get(TEMPLATE_LOADER));
        }

        if (attributesMap.get(VIEW_PATH_PREFIX) != null && attributesMap.get(VIEW_PATH_PREFIX) instanceof String) {
            view.setViewPrefixPath((String) attributesMap.get(VIEW_PATH_PREFIX));
        }

        if (attributesMap.get(VIEW_PATH_SUFFIX) != null && attributesMap.get(VIEW_PATH_SUFFIX) instanceof String) {
            view.setViewSuffixPath((String) attributesMap.get(VIEW_PATH_SUFFIX));
        }

        if (attributesMap.get(VIEW_SOURCE) != null && attributesMap.get(VIEW_SOURCE) instanceof String) {
            view.setExportViewSourceKey((String) attributesMap.get(VIEW_SOURCE));
        }

        if (attributesMap.get(CACHE_PROVIDER) != null && attributesMap.get(CACHE_PROVIDER) instanceof ViewSourceCacheProvider) {
            view.setViewSourceCacheProvider((ViewSourceCacheProvider) attributesMap.get(CACHE_PROVIDER));
        }

        if (attributesMap.get(VIEW_CACHEABLE) != null && attributesMap.get(VIEW_CACHEABLE) instanceof String) {
            String cacheable = (String) attributesMap.get(VIEW_CACHEABLE);
            if ("true".equalsIgnoreCase(cacheable) || "false".equalsIgnoreCase(cacheable)) {
                view.setViewCacheable(Boolean.valueOf(cacheable.toLowerCase()));
            }
        }

        if (attributesMap.get(DUST_COMPILED) != null && attributesMap.get(DUST_COMPILED) instanceof String) {
            String compiled = (String) attributesMap.get(DUST_COMPILED);
            if ("true".equalsIgnoreCase(compiled) || "false".equalsIgnoreCase(compiled)) {
                view.setCompiled(Boolean.valueOf(compiled.toLowerCase()));
            }
        }


        if (attributesMap.get(DUST_JS_CORE_FILE_PATH) != null &&
                attributesMap.get(DUST_JS_CORE_FILE_PATH) instanceof String) {
            String filePath = (String) attributesMap.get(DUST_JS_CORE_FILE_PATH);
            view.getDustEngine().setDustJsFilePath(filePath);
        }

        if (attributesMap.get(DUST_JS_HELPER_FILE_PATH) != null &&
                attributesMap.get(DUST_JS_HELPER_FILE_PATH) instanceof String) {
            String filePath = (String) attributesMap.get(DUST_JS_HELPER_FILE_PATH);
            view.getDustEngine().setDustJsHelperFilePath(filePath);
        }

        if (attributesMap.get(DUST_JS_EXTENSION_FILE_PATH) != null &&
                attributesMap.get(DUST_JS_EXTENSION_FILE_PATH) instanceof String) {
            String filePath = (String) attributesMap.get(DUST_JS_EXTENSION_FILE_PATH);
            view.getDustEngine().loadExtensionFunction(filePath);
        }

        if (attributesMap.get(DUST_ENGINE_OBJECT) != null &&
                attributesMap.get(DUST_ENGINE_OBJECT) instanceof DustTemplateEngine) {
            DustTemplateEngine engine = (DustTemplateEngine) attributesMap.get(DUST_ENGINE_OBJECT);
            view.setDustEngine(engine);
        }
    }
}

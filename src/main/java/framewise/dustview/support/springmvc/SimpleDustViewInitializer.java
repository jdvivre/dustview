package framewise.dustview.support.springmvc;

import framewise.dustview.support.DustTemplateLoader;
import framewise.dustview.support.DustViewConstants;
import framewise.dustview.support.ViewSourceCacheProvider;
import framewise.dustview.core.DustTemplateEngine;
import framewise.dustview.support.DustViewInitializer;

import java.util.Map;

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
        if (attributesMap.get(DustViewConstants.TEMPLATE_LOADER) != null && attributesMap.get(DustViewConstants.TEMPLATE_LOADER) instanceof DustTemplateLoader) {
            view.setViewTemplateLoader((DustTemplateLoader) attributesMap.get(DustViewConstants.TEMPLATE_LOADER));
        }

        if (attributesMap.get(DustViewConstants.VIEW_PATH_PREFIX) != null && attributesMap.get(DustViewConstants.VIEW_PATH_PREFIX) instanceof String) {
            view.setViewPrefixPath((String) attributesMap.get(DustViewConstants.VIEW_PATH_PREFIX));
        }

        if (attributesMap.get(DustViewConstants.VIEW_PATH_SUFFIX) != null && attributesMap.get(DustViewConstants.VIEW_PATH_SUFFIX) instanceof String) {
            view.setViewSuffixPath((String) attributesMap.get(DustViewConstants.VIEW_PATH_SUFFIX));
        }

        if (attributesMap.get(DustViewConstants.VIEW_SOURCE) != null && attributesMap.get(DustViewConstants.VIEW_SOURCE) instanceof String) {
            view.setExportViewSourceKey((String) attributesMap.get(DustViewConstants.VIEW_SOURCE));
        }

        if (attributesMap.get(DustViewConstants.CACHE_PROVIDER) != null && attributesMap.get(DustViewConstants.CACHE_PROVIDER) instanceof ViewSourceCacheProvider) {
            view.setViewSourceCacheProvider((ViewSourceCacheProvider) attributesMap.get(DustViewConstants.CACHE_PROVIDER));
        }

        if (attributesMap.get(DustViewConstants.VIEW_CACHEABLE) != null && attributesMap.get(DustViewConstants.VIEW_CACHEABLE) instanceof String) {
            String cacheable = (String) attributesMap.get(DustViewConstants.VIEW_CACHEABLE);
            if ("true".equalsIgnoreCase(cacheable) || "false".equalsIgnoreCase(cacheable)) {
                view.setViewCacheable(Boolean.valueOf(cacheable.toLowerCase()));
            }
        }

        if (attributesMap.get(DustViewConstants.DUST_COMPILED) != null && attributesMap.get(DustViewConstants.DUST_COMPILED) instanceof String) {
            String compiled = (String) attributesMap.get(DustViewConstants.DUST_COMPILED);
            if ("true".equalsIgnoreCase(compiled) || "false".equalsIgnoreCase(compiled)) {
                view.setCompiled(Boolean.valueOf(compiled.toLowerCase()));
            }
        }

        if (attributesMap.get(DustViewConstants.DUST_JS_CORE_FILE_PATH) != null &&
                attributesMap.get(DustViewConstants.DUST_JS_CORE_FILE_PATH) instanceof String) {
            String filePath = (String) attributesMap.get(DustViewConstants.DUST_JS_CORE_FILE_PATH);
            view.getDustEngine().setDustJsFilePath(filePath);
        }

        if (attributesMap.get(DustViewConstants.DUST_JS_HELPER_FILE_PATH) != null &&
                attributesMap.get(DustViewConstants.DUST_JS_HELPER_FILE_PATH) instanceof String) {
            String filePath = (String) attributesMap.get(DustViewConstants.DUST_JS_HELPER_FILE_PATH);
            view.getDustEngine().setDustJsHelperFilePath(filePath);
        }

        if (attributesMap.get(DustViewConstants.DUST_JS_EXTENSION_FILE_PATH) != null &&
                attributesMap.get(DustViewConstants.DUST_JS_EXTENSION_FILE_PATH) instanceof String) {
            String filePath = (String) attributesMap.get(DustViewConstants.DUST_JS_EXTENSION_FILE_PATH);
            view.getDustEngine().setDustExtensionFilePath(filePath);
        }

        if (attributesMap.get(DustViewConstants.DUST_ENGINE_OBJECT) != null &&
                attributesMap.get(DustViewConstants.DUST_ENGINE_OBJECT) instanceof DustTemplateEngine) {
            DustTemplateEngine engine = (DustTemplateEngine) attributesMap.get(DustViewConstants.DUST_ENGINE_OBJECT);
            view.setDustEngine(engine);
        }
    }
}

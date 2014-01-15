package framewise.dustview;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.view.JstlView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is support to rendering with dust.js on server-side.
 *
 * @author chanwook
 */
public class SimpleDustTemplateView extends JstlView {

    private final Logger logger = LoggerFactory.getLogger(SimpleDustTemplateView.class);

    public static final String VIEW_PATH_OVERRIDE = "_VIEW_PATH_OVERRIDE";
    public static final String DEFAULT_VIEW_ENCODING = "UTF-8";
    public static final String DEFAULT_EXPORT_VIEW_SOURCE_KEY = "_view";
    public static final String DEFAULT_EXPORT_JSON_KEY = "_json";
    public static final String DUST_JS_EXTENSION_FILE_PATH = "_EXTENSION_JS_FILE_PATH";

    public static final String TEMPLATE_KEY = "_TEMPLATE_KEY";
    public static final String VIEW_FILE_PATH = "_VIEW_FILE_PATH";
    public static final String CONTENT_KEY = "_CONTENT_KEY";
    public static final String CONTENT_JSON = "_CONTENT_JSON";

    public static final String TEMPLATE_LOADER = "_TEMPLATE_LOADER";
    public static final String VIEW_PATH_PREFIX = "_VIEW_PATH_PREFIX";
    public static final String VIEW_PATH_SUFFIX = "_VIEW_PATH_SUFFIX";
    public static final String VIEW_SOURCE = "_VIEW_SOURCE";
    public static final String CACHE_PROVIDER = "_CACHE_PROVIDER";
    public static final String VIEW_CACHEABLE = "_VIEW_CACHE";

    private ObjectMapper jsonMapper = new ObjectMapper();
    private DustTemplateEngine dustEngine = new DustTemplateEngine();

    private DustTemplateLoader viewTemplateLoader;

    private String viewEncoding = DEFAULT_VIEW_ENCODING;
    private String exportViewSourceKey = DEFAULT_EXPORT_VIEW_SOURCE_KEY;
    private String exportJsonKey = DEFAULT_EXPORT_JSON_KEY;
    private String viewPrefixPath = "";
    private String viewSuffixPath = "";
    private ViewSourceCacheProvider viewSourceCacheProvider = new PreloadViewSourceCacheProvider();

    private DustViewErrorHandler errorHandler = new DefaultDustViewErrorHandler();

    private boolean viewCacheable = true;

    /**
     * Default Constructor
     */
    public SimpleDustTemplateView() {

    }

    @Override
    protected Map<String, Object> createMergedOutputModel(final Map<String, ? extends Object> model, HttpServletRequest request, HttpServletResponse res) {

        Map<String, Object> mergedOutputModel = prepareToRendering(model, request, res);

        // Compose Variable for Dust View
        String templateKey = getDustTemplateKey(mergedOutputModel);
        if (!StringUtils.hasText(templateKey)) {
            return mergedOutputModel;
        }

        // create JSON Object that used to model at Dust VIEW
        String json = createJsonObject(mergedOutputModel);

        // load template source
        loadTemplateSource(null, request, mergedOutputModel);

        // rendering view
        String renderView = renderingView(templateKey, json);

        addResponseMoreInformation(res);

        if (logger.isDebugEnabled()) {
//            logger.debug("Dust View Rendering Result = TemplateKey: " + templateKey + ", Template File Path: " + viewPath +
//                    ", JSON: " + json + ", View Source: " + viewSource);
        }

        bindingResult(mergedOutputModel, json, renderView);

        return mergedOutputModel;
    }

    private Map<String, Object> prepareToRendering(Map<String, ? extends Object> model, HttpServletRequest request, HttpServletResponse res) {
        Map<String, Object> mergedOutputModel = new HashMap<String, Object>();
        mergedOutputModel.putAll(super.createMergedOutputModel(model, request, res));

        return mergedOutputModel;
    }

    protected void bindingResult(Map<String, Object> mergedOutputModel, String json, String renderView) {
        mergedOutputModel.put(this.exportViewSourceKey, renderView);
        mergedOutputModel.put(this.exportJsonKey, json);
    }

    protected void loadTemplateSource(String templateKey, HttpServletRequest request, Map<String, Object> mergedOutputModel) {
        boolean isRefresh = getRefreshParam(request);
        String viewPath = getViewPath(mergedOutputModel);

        loadTemplateSource(isRefresh, viewPath, templateKey);
    }

    protected void loadTemplateSource(boolean isRefresh, String viewPath, String cacheKey) {
        if (isCaching(isRefresh, cacheKey)) {
            String cachedTemplateSource = viewSourceCacheProvider.get(cacheKey);
            // load to script engine when had to resource
            if (StringUtils.hasText(cachedTemplateSource)) {
                loadResourceToScriptEngine(viewPath, cachedTemplateSource);
            }

            logger.debug("using cached view resource");
        } else {
            String templateSource = viewTemplateLoader.loadTemplate(viewPath);

            logger.debug("load new view resource");

            if (viewCacheable) {
                viewSourceCacheProvider.add(cacheKey, templateSource);
            }
            loadResourceToScriptEngine(viewPath, templateSource);
        }
    }

    private void loadResourceToScriptEngine(String viewPath, String cachedTemplateSource) {
        if (logger.isInfoEnabled()) {
            logger.info("Compiled resource load to script engine!!(target: " + viewPath + "");
        }

        getDustEngine().load(cachedTemplateSource);
    }

    protected boolean isCaching(boolean isRefresh, String cacheKey) {
        return viewCacheable && viewSourceCacheProvider.isCached(cacheKey) && !isRefresh;
    }

    protected String createJsonObject(Map<String, Object> model) {
        Object jsonParam = model.get(CONTENT_KEY);
        if (jsonParam == null) {
            throw new IllegalArgumentException("JSON Object must require! param name is " + CONTENT_KEY);
        }

        try {
            String json = getJsonMapper().writeValueAsString(jsonParam);
            model.put(CONTENT_JSON, json);
            return json;
        } catch (JsonProcessingException e) {
            throw new DustViewException("Fail to create JSON Object[message: " + e.getMessage() + "]", e);
        }
    }

    /**
     * Create view source that using DustTemplateEngine
     *
     * @param templateKey
     * @param json
     * @return
     */
    protected String renderingView(String templateKey, String json) {
        try {
            StringWriter writer = new StringWriter();

            // Rendering by DustEngine
            getDustEngine().render(writer, templateKey, json);

            String renderedView = new String(writer.getBuffer().toString().getBytes(viewEncoding), viewEncoding);

            //will throw exception if occurred
            errorHandler.checkError(templateKey, renderedView);

            return renderedView;
        } catch (UnsupportedEncodingException e) {
            throw new DustViewException("Fail to create View Source", e);
        }
    }

    boolean getRefreshParam(HttpServletRequest request) {
        String param = request.getParameter("_refresh");
        if (param != null && "Y".equals(param.toUpperCase())) {
            return true;
        }
        return false;
    }

    /**
     * initializing method.
     * Caution: Must not call runtime!!
     */
    public void initializeViewProperty() {
        if (getAttributesMap().get(TEMPLATE_LOADER) != null && getAttributesMap().get(TEMPLATE_LOADER) instanceof DustTemplateLoader) {
            setViewTemplateLoader((DustTemplateLoader) getAttributesMap().get(TEMPLATE_LOADER));
        }

        if (getAttributesMap().get(VIEW_PATH_PREFIX) != null && getAttributesMap().get(VIEW_PATH_PREFIX) instanceof String) {
            setViewPrefixPath((String) getAttributesMap().get(VIEW_PATH_PREFIX));
        }

        if (getAttributesMap().get(VIEW_PATH_SUFFIX) != null && getAttributesMap().get(VIEW_PATH_SUFFIX) instanceof String) {
            setViewSuffixPath((String) getAttributesMap().get(VIEW_PATH_SUFFIX));
        }

        if (getAttributesMap().get(VIEW_SOURCE) != null && getAttributesMap().get(VIEW_SOURCE) instanceof String) {
            setExportViewSourceKey((String) getAttributesMap().get(VIEW_SOURCE));
        }

        if (getAttributesMap().get(CACHE_PROVIDER) != null && getAttributesMap().get(CACHE_PROVIDER) instanceof ViewSourceCacheProvider) {
            setViewSourceCacheProvider((ViewSourceCacheProvider) getAttributesMap().get(CACHE_PROVIDER));
        }

        if (getAttributesMap().get(VIEW_CACHEABLE) != null && getAttributesMap().get(VIEW_CACHEABLE) instanceof String) {
            String cacheable = (String) getAttributesMap().get(VIEW_CACHEABLE);
            if ("true".equalsIgnoreCase(cacheable) || "false".equalsIgnoreCase(cacheable)) {
                setViewCacheable(Boolean.valueOf(cacheable.toLowerCase()));
            }
        }

        if (getAttributesMap().get(DUST_JS_EXTENSION_FILE_PATH) != null && getAttributesMap().get(DUST_JS_EXTENSION_FILE_PATH) instanceof String) {
            String filePath = (String) getAttributesMap().get(DUST_JS_EXTENSION_FILE_PATH);
            getDustEngine().loadExtensionFunction(filePath);
        }

    }

    protected void addResponseMoreInformation(HttpServletResponse res) {
        res.addHeader("Accept-Charset", viewEncoding);
        res.setContentType(MediaType.TEXT_HTML_VALUE + ";charset=" + viewEncoding);
        res.setCharacterEncoding(viewEncoding);
    }

    protected String getViewPath(Map<String, ?> model) {
        Object viewPath = model.get(VIEW_PATH_OVERRIDE);
        if (viewPath != null) {
            return (String) viewPath;
        }
        viewPath = model.get(VIEW_FILE_PATH);
        if (viewPath != null) {
            return viewPrefixPath + viewPath + viewSuffixPath;
        } else {
            throw new IllegalArgumentException("View file path must require! param name is " + VIEW_FILE_PATH);
        }
    }

    protected String getDustTemplateKey(Map<String, ?> model) {
        Object templateKey = model.get(TEMPLATE_KEY);
        if (templateKey != null && templateKey instanceof String) {
            return (String) templateKey;
        } else {
            return null;
        }
    }

    /* -- Overriden -- */
    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.web.servlet.view.AbstractUrlBasedView#isUrlRequired()
     */
    @Override
    protected boolean isUrlRequired() {
        // Not using url attribute
        return false;
    }

    @Override
    protected RequestContext createRequestContext(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model) {
        return super.createRequestContext(request, response, model);
    }

    @Override
    protected void initApplicationContext() throws BeansException {
        super.initApplicationContext();
        initializeViewProperty();
    }

    /* -- Getter & Setter -- */
    public ObjectMapper getJsonMapper() {
        return jsonMapper;
    }

    public void setJsonMapper(ObjectMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public void setDustEngine(DustTemplateEngine dustEngine) {
        this.dustEngine = dustEngine;
    }

    public DustTemplateEngine getDustEngine() {
        return dustEngine;
    }

    public DustTemplateLoader getViewTemplateLoader() {
        return viewTemplateLoader;
    }

    public void setViewTemplateLoader(DustTemplateLoader viewTemplateLoader) {
        this.viewTemplateLoader = viewTemplateLoader;
    }

    public void setViewPrefixPath(String viewPrefixPath) {
        this.viewPrefixPath = viewPrefixPath;
    }

    public void setViewSuffixPath(String viewSuffixPath) {
        this.viewSuffixPath = viewSuffixPath;
    }

    public void setViewEncoding(String viewEncoding) {
        this.viewEncoding = viewEncoding;
    }

    public void setExportViewSourceKey(String exportViewSourceKey) {
        this.exportViewSourceKey = exportViewSourceKey;
    }

    public void setViewSourceCacheProvider(ViewSourceCacheProvider viewSourceCacheProvider) {
        this.viewSourceCacheProvider = viewSourceCacheProvider;
    }

    public String getViewEncoding() {
        return viewEncoding;
    }

    public String getExportViewSourceKey() {
        return exportViewSourceKey;
    }

    public String getViewPrefixPath() {
        return viewPrefixPath;
    }

    public String getViewSuffixPath() {
        return viewSuffixPath;
    }

    public boolean isViewCacheable() {
        return viewCacheable;
    }

    public void setViewCacheable(boolean viewCacheable) {
        this.viewCacheable = viewCacheable;
    }

    public String getExportJsonKey() {
        return exportJsonKey;
    }

    public void setExportJsonKey(String exportJsonKey) {
        this.exportJsonKey = exportJsonKey;
    }
}

package framewise.dustview.support.springmvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import framewise.dustview.DustViewException;
import framewise.dustview.core.DustTemplateEngine;
import framewise.dustview.support.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.view.JstlView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is support to rendering with dust.js on server-side.
 *
 * @author chanwook
 */
public class SimpleDustTemplateView extends JstlView {

    private final Logger logger = LoggerFactory.getLogger(SimpleDustTemplateView.class);

    public static final String DEFAULT_VIEW_ENCODING = "UTF-8";
    public static final String DEFAULT_EXPORT_VIEW_SOURCE_KEY = "_view";
    public static final String DEFAULT_EXPORT_JSON_KEY = "_json";

    private ObjectMapper jsonMapper = new ObjectMapper();
    private DustTemplateEngine dustEngine = new DustTemplateEngine(false);

    private DustTemplateLoader viewTemplateLoader;

    private String viewEncoding = DEFAULT_VIEW_ENCODING;
    private String exportViewSourceKey = DEFAULT_EXPORT_VIEW_SOURCE_KEY;
    private String exportJsonKey = DEFAULT_EXPORT_JSON_KEY;
    private String viewPrefixPath = "";
    private String viewSuffixPath = "";

    private boolean viewCacheable = true;
    private boolean mergePath = true;

    private boolean compiled = true;

    private ViewSourceCacheProvider viewSourceCacheProvider = new InMemoryViewSourceCacheProvider();

    private DustViewErrorHandler errorHandler = new DefaultDustViewErrorHandler();

    private DustViewInitializer initializer = new SimpleDustViewInitializer();
    private boolean multiLoad = false;

    /**
     * Default Constructor
     */
    public SimpleDustTemplateView() {
    }

    public SimpleDustTemplateView(boolean isInitializEngine) {
        if (isInitializEngine) {
            this.dustEngine.initializeContext();
        }
    }

    @Override
    protected Map<String, Object> createMergedOutputModel(final Map<String, ? extends Object> model, HttpServletRequest request, HttpServletResponse res) {

        Map<String, Object> mergedOutputModel = prepareToRendering(model, request, res);

        // Compose Variable for Dust View
        String templateKey = getDustTemplateKey(mergedOutputModel);
        if (!StringUtils.hasText(templateKey)) {
            if (logger.isDebugEnabled()) {
                logger.debug("TemplateKey not found! Then do not execute for dust binding!");
            }
            return mergedOutputModel;
        }

        // create JSON Object that used to model at Dust VIEW
        String json = createJson(templateKey, mergedOutputModel);

        // load template source
        String viewPath = getViewPath(mergedOutputModel);
        loadTemplateSource(request, templateKey, viewPath);

        // rendering view
        String renderHtml = renderingView(templateKey, json);

        addResponseMoreInformation(res);

        if (logger.isDebugEnabled()) {
            logger.debug("[Dust View Rendering Result] " +
                    "\n1) TemplateKey: " + templateKey +
                    "\n2) Template File Path: " + viewPath +
                    "\n3) Compiled HTML: " + viewPath +
                    "\n4) JSON: " + json +
                    "\n5) Final Rendering HTML: " + renderHtml
            );
        }

        bindingResult(mergedOutputModel, json, renderHtml);

        return mergedOutputModel;
    }

    void loadTemplateSource(HttpServletRequest request, String templateKey, String viewPath) {
        boolean isRefresh = getRefreshParam(templateKey, request);
        boolean isSuccessMultiLoad = false;
        if (isMultiLoad()) {
            isSuccessMultiLoad = loadMultiTemplateSource(templateKey, viewPath, isRefresh);
        }
        if (isSingleLoad(isSuccessMultiLoad)) {
            loadSingleTemplateSource(templateKey, viewPath, isRefresh);
        }
    }

    private boolean isSingleLoad(boolean isSuccessMultiLoad) {
        return !isMultiLoad() && !isSuccessMultiLoad;
    }

    /**
     * This method is pre step for rendering.
     * Commonly this contains for relation to Spring framework code.
     *
     * @param model
     * @param request
     * @param res
     * @return
     */
    private Map<String, Object> prepareToRendering(Map<String, ? extends Object> model, HttpServletRequest request, HttpServletResponse res) {
        Map<String, Object> mergedOutputModel = new HashMap<String, Object>();
        mergedOutputModel.putAll(super.createMergedOutputModel(model, request, res));

        return mergedOutputModel;
    }

    protected void bindingResult(Map<String, Object> mergedOutputModel, String json, String renderView) {
        mergedOutputModel.put(this.exportViewSourceKey, renderView);
        mergedOutputModel.put(this.exportJsonKey, json);
    }

    /**
     * Load dust template source!
     * This method has core logic.
     * If not dust compiled html, do compiling for plain HTML!
     *
     * @param templateKey
     * @param viewPath
     * @param isRefresh
     * @return
     */
    protected boolean loadSingleTemplateSource(String templateKey, String viewPath, boolean isRefresh) {
        String templateSource = null;
        boolean useCache = false;
        if (isCaching(isRefresh, templateKey)) {
            templateSource = viewSourceCacheProvider.get(templateKey);

            if (logger.isDebugEnabled()) {
                logger.debug("Using cached view resource(templateKey: " + templateKey + ", viewPath: " + viewPath + ")");
            }
            useCache = true;
        } else {
            templateSource = viewTemplateLoader.loadTemplate(viewPath);

            if (logger.isDebugEnabled()) {
                logger.debug("Load new view resource (templateKey: " + templateKey + ", viewPath: " + viewPath + ")");
            }

            if (viewCacheable) {
                viewSourceCacheProvider.add(templateKey, templateSource);
            }
        }
        loadResourceToScriptEngine(templateKey, viewPath, templateSource);
        return useCache;
    }

    boolean loadMultiTemplateSource(String templateKey, String viewPath, boolean isRefresh) {
        ClassPathResource resource = new ClassPathResource(viewPath);
        try {
            File directory = resource.getFile();
            if (directory.exists() && directory.isDirectory()) {
                logger.debug("Load multiple template source.");
                File[] files = directory.listFiles();

                for (int index = 0; index < files.length; index++) {
                    String name = files[index].getName();
                    loadSingleTemplateSource(name.replace(".html", ""), viewPath + File.separator + name, isRefresh);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Request to load to template(index: " + index + ", templateKey: " + name + ")");
                    }
                }
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            throw new DustViewException(e);
        }
    }

    void loadResourceToScriptEngine(String templateKey, String viewPath, String templateSource) {
        if (!compiled) {
            //need compile html by dust.js
            if (logger.isDebugEnabled()) {
                logger.debug("Compile HTML to Dust Markup(" + templateKey + "): [Raw HTML Source] " + templateSource);
            }
            templateSource = getDustEngine().compile(templateKey, templateSource);
        }
        getDustEngine().load(templateKey, templateSource);
    }

    protected boolean isCaching(boolean isRefresh, String cacheKey) {
        return viewCacheable && viewSourceCacheProvider.isCached(cacheKey) && !isRefresh;
    }

    /**
     * Create json that used object in model.
     * Object convert to json string..
     *
     * @param templateKey
     * @param model
     * @return
     */
    protected String createJson(String templateKey, Map<String, Object> model) {
        // first tyr for JSON Object!
        Object jsonParam = model.get(DustViewConstants.CONTENT_KEY);
        if (StringUtils.hasText(templateKey) && jsonParam != null) {
            return createJsonFromObject(templateKey, jsonParam);
        }

        // second try for JSON Text!
        Object jsonTextParam = model.get(DustViewConstants.CONTENT_TEXT_KEY);
        if (StringUtils.hasText(templateKey) && jsonTextParam != null) {
            return createJsonFromText(templateKey, jsonTextParam);
        }
        throw new IllegalArgumentException("JSON content must require! (request templteKey: " + templateKey + ")");
    }

    protected String createJsonFromText(String templateKey, Object jsonTextParam) {
        if (jsonTextParam instanceof String) {
            return (String) jsonTextParam;
        } else {
            throw new IllegalArgumentException("JSON Text content must java.lang.String type! (request templteKey: " + templateKey + ")");
        }
    }

    protected String createJsonFromObject(String templateKey, Object jsonParam) {
        try {
            String json = getJsonMapper().writeValueAsString(jsonParam);

            if (logger.isDebugEnabled()) {
                logger.debug("Generate JSON text(templateKey: " + templateKey + ", text: " + json + ")");
            }
            return json;
        } catch (JsonProcessingException e) {
            throw new DustViewException("Fail to create JSON Object[templateKey: " + templateKey + ", message: " + e.getMessage() + "]", e);
        }
    }

    /**
     * Create final view html that using DustTemplateEngine.
     *
     * @param templateKey
     * @param json
     * @return
     */
    protected String renderingView(String templateKey, String json) {
        try {
            StringWriter writer = new StringWriter();
            StringWriter errorWriter = new StringWriter();

            // Rendering by DustEngine
            getDustEngine().render(writer, errorWriter, templateKey, json);

            // will throw exception if occurred
            errorHandler.handleError(templateKey, errorWriter, viewEncoding);

            String renderedView = new String(writer.getBuffer().toString().getBytes(viewEncoding), viewEncoding);
            return renderedView;
        } catch (Exception e) {
            throw new DustViewException("Fail to create View Source(templateKey: " + templateKey + ")", e);
        }
    }

    boolean getRefreshParam(String templateKey, HttpServletRequest request) {
        String param = request.getParameter("_refresh");
        if (param != null && "Y".equals(param.toUpperCase())) {
            if (logger.isInfoEnabled()) {
                logger.info("Request to refresh cached compiled resource!(templateKey: " + templateKey + ")");
            }
            return true;
        }
        return false;
    }

    protected void addResponseMoreInformation(HttpServletResponse res) {
        res.addHeader("Accept-Charset", viewEncoding);
        res.setContentType(MediaType.TEXT_HTML_VALUE + ";charset=" + viewEncoding);
        res.setCharacterEncoding(viewEncoding);
    }

    /**
     * Resolve view file path with three step!
     * 1) full view path
     * 2) merge view path with prefix, suffix
     * 3) view path in properties
     *
     * @param model
     * @return
     */
    protected String getViewPath(Map<String, ?> model) {
        //Case 1. full view path
        Object viewPath = model.get(DustViewConstants.VIEW_PATH_OVERRIDE);
        if (viewPath != null) {
            return (String) viewPath;
        }

        // Case 2. merge view path with prefix, suffix
        viewPath = model.get(DustViewConstants.VIEW_FILE_PATH);
        if (viewPath != null) {
            return mergeViewPath(viewPath);
        }

        // Case 3. view path in properties
        Object viewPathKey = model.get(DustViewConstants.VIEW_PATH_KEY);
        if (viewPathKey != null && viewPathKey instanceof String) {
            viewPath = resolveViewPathInProperties((String) viewPathKey);
            return (String) viewPath;
        }
        throw new IllegalArgumentException("View file path must require! param name is " + DustViewConstants.VIEW_FILE_PATH);
    }

    protected Object resolveViewPathInProperties(String viewPathKey) {
        String viewPath = super.getApplicationContext().getEnvironment().getProperty(viewPathKey);

        if (!StringUtils.hasText(viewPath)) {
            throw new IllegalArgumentException("Not found view path with '" + viewPathKey + "'! Check properties file.");
        }
        return mergeViewPath(viewPath);
    }

    private String mergeViewPath(Object viewPath) {
        if (mergePath) {
            return viewPrefixPath + viewPath + viewSuffixPath;
        }
        return (String) viewPath;
    }

    /**
     * Resolve template key in model.
     * Model had return by controller to created by user
     *
     * @param model
     * @return
     */
    protected String getDustTemplateKey(Map<String, ?> model) {
        Object templateKey = model.get(DustViewConstants.TEMPLATE_KEY);
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
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        initializer.initializeViewProperty(getAttributesMap(), this);
        // re-initializing context because change attribute!
        getDustEngine().initializeContext();
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

    public ViewSourceCacheProvider getViewSourceCacheProvider() {
        return viewSourceCacheProvider;
    }

    public DustViewErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public void setErrorHandler(DustViewErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public boolean isMergePath() {
        return mergePath;
    }

    public void setMergePath(boolean mergePath) {
        this.mergePath = mergePath;
    }

    public DustViewInitializer getInitializer() {
        return initializer;
    }

    public void setInitializer(DustViewInitializer initializer) {
        this.initializer = initializer;
    }

    public boolean isCompiled() {
        return compiled;
    }

    public void setCompiled(boolean compiled) {
        this.compiled = compiled;
    }

    public boolean isMultiLoad() {
        return multiLoad;
    }

    public void setMultiLoad(boolean multiLoad) {
        this.multiLoad = multiLoad;
    }
}

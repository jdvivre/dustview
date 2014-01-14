package framewise.dustview;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final String DEFAULT_EXPORT_JSON_KEY = "_json";

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
    public static final String DUST_JS_EXTENSION_FILE_PATH = "_EXTENSION_JS_FILE_PATH";

    private ObjectMapper jsonMapper = new ObjectMapper();
    private DustTemplateEngine dustEngine = new DustTemplateEngine();

    private DustTemplateLoader viewTemplateLoader;

    private String viewEncoding = DEFAULT_VIEW_ENCODING;
    private String exportViewSourceKey = DEFAULT_EXPORT_VIEW_SOURCE_KEY;
    private String exportJsonKey = DEFAULT_EXPORT_JSON_KEY;
    private String viewPrefixPath = "";
    private String viewSuffixPath = "";
    private ViewSourceCacheProvider viewSourceCacheProvider = new InMemoryViewSourceCacheProvider();

//    private ContentCacheProvider contentCacheProvider = new InMemoryContentCacheProvider();

    private boolean viewCacheable = true;

    /**
     * Default Constructor
     */
    public SimpleDustTemplateView() {

    }

    @Override
    protected RequestContext createRequestContext(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model) {
        return super.createRequestContext(request, response, model);
    }

    @Override
    protected Map<String, Object> createMergedOutputModel(final Map<String, ? extends Object> model, HttpServletRequest request, HttpServletResponse res) {

        Map<String, Object> mergedOutputModel = new HashMap<String, Object>();
        mergedOutputModel.putAll(super.createMergedOutputModel(model, request, res));

        resolvePropertyByViewAttribute();

        // Compose Variable for Dust View
        String templateKey = getDustTemplateKey(mergedOutputModel);
        if (!StringUtils.hasText(templateKey)) {
            return mergedOutputModel;
        }

        // create JSON Object that used to model at Dust VIEW
        String json = createJsonObject(mergedOutputModel);

        // load template source
        boolean usedCacheView = loadTemplateSource(request, mergedOutputModel);

        // Dust.js compile ~ rendering
        String renderView = renderingView(templateKey, json, usedCacheView);

        addResponseMoreInformation(res);

        if (logger.isDebugEnabled()) {
//            logger.debug("Dust View Rendering Result = TemplateKey: " + templateKey + ", Template File Path: " + viewPath +
//                    ", JSON: " + json + ", View Source: " + viewSource);
        }

        // Binding Result
        mergedOutputModel.put(this.exportViewSourceKey, renderView);
        mergedOutputModel.put(this.exportJsonKey, json);

        return mergedOutputModel;
    }

    private boolean loadTemplateSource(HttpServletRequest request, Map<String, Object> mergedOutputModel) {
        boolean isRefresh = getRefreshParam(request);
        String viewPath = getViewPath(mergedOutputModel);
        String cacheKey = getViewCacheKey(mergedOutputModel);

        if (viewCacheable && viewSourceCacheProvider.isCached(cacheKey) && !isRefresh) {
            //cache에서 로딩한 소스는 이미 loader에 올라가 있으니 다시 올릴필요가 없다.
            logger.debug("using cache view source");
            return true;
        } else {
            logger.debug("loading new view source");
            String templateSource = viewTemplateLoader.loadTemplate(viewPath);

            getDustEngine().load(templateSource);

            if (viewCacheable) {
                viewSourceCacheProvider.add(cacheKey, templateSource);
            }
            return false;
        }

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
     * @param usedCacheView
     * @return
     */
    protected String renderingView(String templateKey, String json, boolean usedCacheView) {
        // view도 동일하고, JSON도 동일하다면 다시 렌더링하지 않고 저장해둔 값을 사용함
        /*
        if (usedCacheView && contentCacheProvider.isCached(templateKey, json)) {
            return contentCacheProvider.get(templateKey);
        }
        */

        StringWriter writer = new StringWriter();
        getDustEngine().render(writer, templateKey, json);

        try {
            String renderedView = new String(writer.getBuffer().toString().getBytes(viewEncoding), viewEncoding);
            /*
            if (viewCacheable) {
                contentCacheProvider.add(templateKey, json, renderedView);
            }
            */
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

    protected void resolvePropertyByViewAttribute() {
        if (viewTemplateLoader == null && getAttributesMap().get(TEMPLATE_LOADER) != null && getAttributesMap().get(TEMPLATE_LOADER) instanceof DustTemplateLoader) {
            setViewTemplateLoader((DustTemplateLoader) getAttributesMap().get(TEMPLATE_LOADER));
        }

        if (!StringUtils.hasText(viewPrefixPath) && getAttributesMap().get(VIEW_PATH_PREFIX) != null && getAttributesMap().get(VIEW_PATH_PREFIX) instanceof String) {
            setViewPrefixPath((String) getAttributesMap().get(VIEW_PATH_PREFIX));
        }

        if (!StringUtils.hasText(viewSuffixPath) && getAttributesMap().get(VIEW_PATH_SUFFIX) != null && getAttributesMap().get(VIEW_PATH_SUFFIX) instanceof String) {
            setViewSuffixPath((String) getAttributesMap().get(VIEW_PATH_SUFFIX));
        }

        if (getAttributesMap().get(VIEW_SOURCE) != null && getAttributesMap().get(VIEW_SOURCE) instanceof String) {
            setExportViewSourceKey((String) getAttributesMap().get(VIEW_SOURCE));
        }

        if (viewSourceCacheProvider != getAttributesMap().get(CACHE_PROVIDER) && getAttributesMap().get(CACHE_PROVIDER) != null && getAttributesMap().get(CACHE_PROVIDER) instanceof ViewSourceCacheProvider) {
            setViewSourceCacheProvider((ViewSourceCacheProvider) getAttributesMap().get(CACHE_PROVIDER));
        }

        if (getAttributesMap().get(VIEW_CACHEABLE) != null && getAttributesMap().get(VIEW_CACHEABLE) instanceof String) {

            String cacheable = (String) getAttributesMap().get(VIEW_CACHEABLE);
            if ("true".equalsIgnoreCase(cacheable) || "false".equalsIgnoreCase(cacheable)) {
                setViewCacheable(Boolean.valueOf(cacheable.toLowerCase()));
            }
        }

        if (getDustEngine().getDsutJsExtensionFilePath() == null && getAttributesMap().get(DUST_JS_EXTENSION_FILE_PATH) != null && getAttributesMap().get(DUST_JS_EXTENSION_FILE_PATH) instanceof String) {
            getDustEngine().setDsutJsExtensionFilePath((String) getAttributesMap().get(DUST_JS_EXTENSION_FILE_PATH));
            getDustEngine().initializeContext();
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

    protected String getViewCacheKey(Map<String, ?> model) {
        Object viewPath = model.get(VIEW_FILE_PATH);
        if (viewPath != null) {
            String cacheKey = viewPath + viewSuffixPath;
            cacheKey = cacheKey.replaceAll("//", "/");
            if (cacheKey.startsWith("/")) {
                cacheKey = cacheKey.substring(1);
            }

            return cacheKey;
        }else {
            return "";
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

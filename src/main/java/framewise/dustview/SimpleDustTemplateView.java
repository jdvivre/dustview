package framewise.dustview;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.view.JstlView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class is support to rendering with dust.js on server-side.
 *
 * @author chanwook
 */
public class SimpleDustTemplateView extends JstlView {

    private final Logger logger = LoggerFactory.getLogger(SimpleDustTemplateView.class);

    public static final String VIEW_PATH_OVERRIDE = "_VIEW_PATH_OVERRIDE";
    public static final String DEFAULT_VIEW_ENCODING = "UTF-8";
    public static final String DEFAULT_VIEW_SOURCE = "_view";
        
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
    private String viewSource = DEFAULT_VIEW_SOURCE;
    private String viewPrefixPath = "";
    private String viewSuffixPath = "";
    private ViewSourceCacheProvider viewSourceCacheProvider = new InMemoryViewSourceCacheProvider();
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
        boolean isRefresh = getRefreshParam(request);
        String viewPath = getDustViewPath(mergedOutputModel);
        String cacheKey = getDustViewCacheKey(mergedOutputModel);
        String templateSource = loadTemplateSource(viewPath, cacheKey, isRefresh);

        // Dust.js compile ~ rendering
        String viewSource = createViewSource(templateKey, json, templateSource);

        addResponseMoreInformation(res);

        if (logger.isDebugEnabled()) {
            logger.debug("Dust View Rendering Result = TemplateKey: " + templateKey + ", Template File Path: " + viewPath +
                    ", JSON: " + json + ", View Source: " + viewSource);
        }

        mergedOutputModel.put(this.viewSource, viewSource);
        Object object = mergedOutputModel.get(CONTENT_JSON);
        System.out.println(object);

        return mergedOutputModel;
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
     * @param templateSource
     * @return
     */
    protected String createViewSource(String templateKey, String json, String templateSource) {
        StringWriter writer = new StringWriter();
        getDustEngine().load(templateSource);
        getDustEngine().render(writer, templateKey, json);

        try {
            return new String(writer.getBuffer().toString().getBytes(viewEncoding), viewEncoding);
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
        
        if(getAttributesMap().get(VIEW_SOURCE) != null && getAttributesMap().get(VIEW_SOURCE) instanceof String) {
            setViewSource((String) getAttributesMap().get(VIEW_SOURCE));
        }

        if (viewSourceCacheProvider != getAttributesMap().get(CACHE_PROVIDER) && getAttributesMap().get(CACHE_PROVIDER) != null && getAttributesMap().get(CACHE_PROVIDER) instanceof ViewSourceCacheProvider) {
        	setViewSourceCacheProvider((ViewSourceCacheProvider) getAttributesMap().get(CACHE_PROVIDER));
        }
        
        if(getAttributesMap().get(VIEW_CACHEABLE) != null && getAttributesMap().get(VIEW_CACHEABLE) instanceof String) {
        	
        	String cacheable = (String) getAttributesMap().get(VIEW_CACHEABLE);
        	if("true".equalsIgnoreCase(cacheable) || "false".equalsIgnoreCase(cacheable)) {
        		setViewCacheable(Boolean.valueOf(cacheable) );
        	}
        }
        
        if(getDustEngine().getDsutJsExtensionFilePath() == null && getAttributesMap().get(DUST_JS_EXTENSION_FILE_PATH) != null && getAttributesMap().get(DUST_JS_EXTENSION_FILE_PATH) instanceof String) {
            getDustEngine().setDsutJsExtensionFilePath((String) getAttributesMap().get(DUST_JS_EXTENSION_FILE_PATH));
            getDustEngine().initializeContext();
        }
    }

    protected void addResponseMoreInformation(HttpServletResponse res) {
        res.addHeader("Accept-Charset", viewEncoding);
        res.setContentType(MediaType.TEXT_HTML_VALUE + ";charset=" + viewEncoding);
        res.setCharacterEncoding(viewEncoding);
    }

    protected String loadTemplateSource(String viewPath, String cacheKey, boolean isRefresh) {
        String templateSource = "";
        if (viewCacheable && viewSourceCacheProvider.isCached(cacheKey) && !isRefresh) {
            templateSource = viewSourceCacheProvider.get(cacheKey);
        } else {
            templateSource = viewTemplateLoader.loadTemplate(viewPath);

            if (viewCacheable) {
                viewSourceCacheProvider.add(cacheKey, templateSource);
            }
        }
        return templateSource;
    }

    protected String getDustViewPath(Map<String, ?> model) {
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
    
    protected String getDustViewCacheKey(Map<String, ?> model) {

    	Object viewPath = model.get(VIEW_FILE_PATH);
    	if (viewPath != null) {
    		return viewPath + viewSuffixPath;
    	} else {
    		throw new IllegalArgumentException("View Cache Key must require! param name is " + VIEW_FILE_PATH);
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

    public void setViewSource(String viewSource) {
    	this.viewSource = viewSource;
    }
    
    public void setViewSourceCacheProvider(ViewSourceCacheProvider viewSourceCacheProvider) {
        this.viewSourceCacheProvider = viewSourceCacheProvider;
    }

    public String getViewEncoding() {
        return viewEncoding;
    }
    
	public String getViewSource() {
		return viewSource;
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
}

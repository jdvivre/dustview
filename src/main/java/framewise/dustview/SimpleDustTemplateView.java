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
import java.util.Map;

/**
 * This class is support to rendering with dust.js on server-side.
 *
 * @author chanwook
 */
public class SimpleDustTemplateView extends JstlView {

    private final Logger logger = LoggerFactory.getLogger(SimpleDustTemplateView.class);

    private static final String DEFAULT_VIEW_ENCODING = "UTF-8";
    private static final String TEMPLATE_KEY = "_TEMPLATE_KEY";
    private static final String VIEW_PATH = "_VIEW_PATH";
    private static final String DATA_KEY = "_DATA_KEY";
    private static final String VIEW_SOURCE = "_view";
    private static final String TEMPLATE_LOADER = "_TEMPLATE_LOADER";
    private static final String VIEW_PATH_PREFIX = "_VIEW_PATH_PREFIX";
    private static final String VIEW_PATH_SUFFIX = "_VIEW_PATH_SUFFIX";

    private ObjectMapper jsonMapper = new ObjectMapper();
    private DustTemplateEngine dustEngine = new DustTemplateEngine();

    private DustViewTemplateLoader viewTemplateLoader;

    private String viewEncoding = DEFAULT_VIEW_ENCODING;
    private String viewPrefixPath = "";
    private String viewSuffixPath = "";

    /**
     * Default Constructor
     */
    public SimpleDustTemplateView() {
        dustEngine.initializeContext();
    }

    @Override
    protected RequestContext createRequestContext(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model) {
        return super.createRequestContext(request, response, model);
    }

    @Override
    protected Map<String, Object> createMergedOutputModel(final Map<String, ? extends Object> model, HttpServletRequest request, HttpServletResponse res) {

        Map<String, Object> mergedOutputModel = super.createMergedOutputModel(model, request, res);

        initViewAttribute();

        // Compose Variable for Dust View
        String templateKey = getDustTemplateKey(mergedOutputModel);
        if (!StringUtils.hasText(templateKey)) {
            return mergedOutputModel;
        }

        Object jsonData = getJsonData(mergedOutputModel);

        String viewFile = getDustViewPath(mergedOutputModel);

        if (StringUtils.hasText(templateKey) && (jsonData == null || !StringUtils.hasText(viewFile))) {
            throw new IllegalArgumentException("Insufficiently Argument to DustView. [templateKey: " + templateKey + ", jsonDataObject: " + jsonData + ", DustView File Path: " + viewFile + "]");
        }

        // create JSON Object that used to model at Dust VIEW
        String json = createJsonObject(jsonData);

        String templateSource = loadViewTemplateSource(viewFile);

        // TODO add caches
        StringWriter writer = new StringWriter();
        // Dust.js compile ~ rendering
        getDustEngine().load(templateSource);
        getDustEngine().render(writer, templateKey, json);

        String viewSource = createView(writer);

        setResponseMoreInformation(res);

        if (logger.isDebugEnabled()) {
            logger.debug("Dust View Rendering Result=> TemplateKey: " + templateKey + ", Template File: " + viewFile + ", JSON: " + json + ", View Source: " + viewSource);
        }

        mergedOutputModel.put(VIEW_SOURCE, viewSource);

        return mergedOutputModel;
    }

    protected void initViewAttribute() {
        if (viewTemplateLoader == null && getAttributesMap().get(TEMPLATE_LOADER) != null && getAttributesMap().get(TEMPLATE_LOADER) instanceof DustViewTemplateLoader) {
            setViewTemplateLoader((DustViewTemplateLoader) getAttributesMap().get(TEMPLATE_LOADER));
        }

        if (!StringUtils.hasText(viewPrefixPath) && getAttributesMap().get(VIEW_PATH_PREFIX) != null && getAttributesMap().get(VIEW_PATH_PREFIX) instanceof String) {
            setViewPrefixPath((String) getAttributesMap().get(VIEW_PATH_PREFIX));
        }

        if (!StringUtils.hasText(viewSuffixPath) && getAttributesMap().get(VIEW_PATH_SUFFIX) != null && getAttributesMap().get(VIEW_PATH_SUFFIX) instanceof String) {
            setViewSuffixPath((String) getAttributesMap().get(VIEW_PATH_SUFFIX));
        }
    }

    protected void setResponseMoreInformation(HttpServletResponse res) {
        res.addHeader("Accept-Charset", viewEncoding);
        res.setContentType(MediaType.TEXT_HTML_VALUE + ";charset=" + viewEncoding);
        res.setCharacterEncoding(viewEncoding);
    }

    protected String createView(StringWriter writer) {

        try {
            return new String(writer.getBuffer().toString().getBytes(viewEncoding), viewEncoding);
        } catch (UnsupportedEncodingException e) {
            throw new DustViewException("Fail to create View Source", e);
        }

    }

    protected String loadViewTemplateSource(String viewFile) {
        return viewTemplateLoader.loadFile(viewFile);
    }

    protected String createJsonObject(Object jsonData) {
        try {
            return getJsonMapper().writeValueAsString(jsonData);
        } catch (JsonProcessingException e) {
            throw new DustViewException("Fail to create JSON Object[message: " + e.getMessage() + "]", e);
        }
    }

    protected Object getJsonData(Map<String, ?> model) {
        return model.get(DATA_KEY);
    }

    protected String getDustViewPath(Map<String, ?> model) {
        Object viewPath = model.get(VIEW_PATH);
        if (viewPath != null && viewPath instanceof String) {
            return viewPrefixPath + viewPath + viewSuffixPath;
        } else {
            return null;
        }
    }

    protected String getDustTemplateKey(Map<String, ?> model) {
        Object templateKey = model.get(TEMPLATE_KEY);
        if (templateKey != null && templateKey instanceof String) {
            return (String) templateKey;
        } else if (templateKey != null && !(templateKey instanceof String)) {
            throw new IllegalArgumentException("Template key must be java.lang.String!");
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

    public DustViewTemplateLoader getViewTemplateLoader() {
        return viewTemplateLoader;
    }

    public void setViewTemplateLoader(DustViewTemplateLoader viewTemplateLoader) {
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
}

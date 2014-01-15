package framewise.dustview;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;

/**
 * Support server-side dust rendering Function. This class load by Rhino JavaScript Engine.
 *
 * @author chanwook
 */
public class DustTemplateEngine {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String DEFAULT_COMPILE_SOURCE_NAME = "ServerSideDustCompiler";
    private static final String DEFAULT_DUST_JS_FILE_PATH = "/dust/dust-full-1.1.1.js";
    private static final String DEFAULT_DUST_HELPER_JS_FILE_PATH = "/dust/dust-helpers-1.1.0.js";
    private static final String DEFAULT_COMPILE_SCRIPT = "(dust.compile(source, templateKey))";
    private static final String DEFAULT_LOAD_SCRIPT = "(dust.loadSource(compiledSource))";
    private static final String DEFAULT_RENDER_SCRIPT = (
            "{   dust.render( templateKey, JSON.parse(json), "
                    + "function(error, data) { if(error) { writer.write(error);} else { writer.write( data );} } );}"
    );

    private static final String NEW_RENDERING_SCRIPT = "function dustRender(templateKey, json, writer) {return dust.render(templateKey, JSON.parse(json) ,function(err, out){writer.write(out);});}";

    private static final String NEW_LOADING_SCRIPT = "function dustLoad(source) { dust.loadSource(source); }";

    private static final String DEFAULT_ENCODING = "UTF-8";

    private static Scriptable globalScope;

    private String compileSourceName = DEFAULT_COMPILE_SOURCE_NAME;
    private String dustJsFilePath = DEFAULT_DUST_JS_FILE_PATH;
    private String dustJsHelperFilePath = DEFAULT_DUST_HELPER_JS_FILE_PATH;
    private String encoding = DEFAULT_ENCODING;
    private String compileScript = DEFAULT_COMPILE_SCRIPT;
    private String loadScript = DEFAULT_LOAD_SCRIPT;
    private String renderScript = DEFAULT_RENDER_SCRIPT;
    private String dsutJsExtensionFilePath;

    public DustTemplateEngine() {
        initializeContext();
    }

    /**
     * dust context initialize method. must call before running dust
     */
    public void initializeContext() {
        InputStream dustJsStream = getDustJsStream(getDustJsFilePath());
        InputStream dustHelperJsStream = getDustJsStream(getDustJsHelperFilePath());

        if (StringUtils.hasText(dsutJsExtensionFilePath)) {
            loadDustJsEngine(dustJsStream, dustHelperJsStream, getDustJsStream(dsutJsExtensionFilePath));
        } else {
            loadDustJsEngine(dustJsStream, dustHelperJsStream);
        }
    }


    /**
     * Initialize Dust JS Context
     *
     * @param dustJsStream
     * @param dustHelperJsStream
     */
    protected void loadDustJsEngine(InputStream dustJsStream, InputStream dustHelperJsStream) {
        this.loadDustJsEngine(dustJsStream, dustHelperJsStream, null);
    }

    /**
     * Initialize Dust JS Context
     *
     * @param dustJsStream
     * @param dustHelperJsStream
     * @param dustExtentionJsStream
     */
    protected void loadDustJsEngine(InputStream dustJsStream, InputStream dustHelperJsStream, InputStream dustExtentionJsStream) {
        try {
            Reader dustJsReader = new InputStreamReader(dustJsStream, encoding);
            Reader dustJsHelperReader = new InputStreamReader(dustHelperJsStream, encoding);

            Context context = Context.enter();
//            context.setOptimizationLevel(9);
            context.setOptimizationLevel(-1);

            globalScope = context.initStandardObjects();
            context.evaluateReader(globalScope, dustJsReader, dustJsFilePath, dustJsStream.available(), null);
            context.evaluateReader(globalScope, dustJsHelperReader, dustJsHelperFilePath, dustHelperJsStream.available(), null);

            if (dustExtentionJsStream != null) {
                Reader dustJsExtentionReader = new InputStreamReader(dustExtentionJsStream, encoding);
                context.evaluateReader(globalScope, dustJsExtentionReader, "/dust/dust.helpers.extension.js", dustExtentionJsStream.available(), null);
            }

            context.evaluateString(globalScope, NEW_LOADING_SCRIPT, compileSourceName, 0, null);
            context.evaluateString(globalScope, NEW_RENDERING_SCRIPT, compileSourceName, 0, null);


        } catch (Exception e) {
            logger.error("thrown exception when initialize step!", e);
            throw new DustViewException(e);
        } finally {
            Context.exit();
        }
    }

    /**
     * Compile HTML Markup that used by Dust.js
     *
     * @param source      HTML Markup Source
     * @param templateKey
     * @return Compiled HTML Markup(JavaScript Format)
     */
    public String compile(String source, String templateKey) {
        final Context context = Context.enter();
        try {

//            Scriptable compileScope = context.newObject(globalScope);
//            compileScope.setParentScope(globalScope);
            /*
            Scriptable compileScope = globalScope;

            compileScope.put("source", compileScope, source);
            compileScope.put("templateKey", compileScope, templateKey);

            return (String) context.evaluateString(compileScope, compileScript, compileSourceName, 0, null);Scriptable compileScope = globalScope;
            */

            globalScope.put("source", globalScope, source);
            globalScope.put("templateKey", globalScope, templateKey);

            return (String) context.evaluateString(globalScope, compileScript, compileSourceName, 0, null);
        } catch (JavaScriptException e) {
            throw new DustViewException("thrown error when compile Dust JS Source", e);
        } finally {
            Context.exit();
        }
    }

    /**
     * Load Compiled Markup Source to JavaScript Object
     *
     * @param compiledSource load target HTML Markup
     */
    public void load(String compiledSource) {
        final Context context = Context.enter();
        try {

//            Scriptable loadScope = context.newObject(globalScope);
//            loadScope.setParentScope(globalScope);
            /*
            Scriptable loadScope = globalScope;

            loadScope.put("compiledSource", loadScope, compiledSource);

            context.evaluateString(loadScope, loadScript, compileSourceName, 0, null);
            */

            /*
            globalScope.put("compiledSource", globalScope, compiledSource);

            context.evaluateString(globalScope, loadScript, compileSourceName, 0, null);
            */

//            Scriptable scriptableObject = context.initStandardObjects();
            context.setOptimizationLevel(-1);
            Function fct = (Function) globalScope.get("dustLoad", globalScope);
            fct.call(context, globalScope, globalScope, new Object[]{compiledSource});
        } catch (JavaScriptException e) {
            throw new DustViewException("thrown error when load Dust JS Source", e);
        } finally {
            Context.exit();
        }
    }

    /**
     * Rendering Markup. result is binded to Markup with JSON data.
     * Result is plain text HTML markup, then will write to {@link Writer} object.
     *
     * @param writer
     * @param templateKey
     * @param json
     */
    public void render(Writer writer, String templateKey, String json) {
        final Context context = Context.enter();
        try {
//            Scriptable renderScope = context.newObject(globalScope);
//            renderScope.setParentScope(globalScope);

            /*
            Scriptable renderScope = globalScope;

            renderScope.put("writer", renderScope, writer);
            renderScope.put("json", renderScope, json);
            renderScope.put("templateKey", renderScope, templateKey);

            context.evaluateString(renderScope, renderScript, compileSourceName, 0, null);
            */

            /*
            globalScope.put("writer", globalScope, writer);
            globalScope.put("json", globalScope, json);
            globalScope.put("templateKey", globalScope, templateKey);

            context.evaluateString(globalScope, renderScript, compileSourceName, 0, null);
            */

//            Scriptable scriptableObject = context.initStandardObjects();
            context.setOptimizationLevel(-1);
            Function fct = (Function) globalScope.get("dustRender", globalScope);
            fct.call(context, globalScope, globalScope, new Object[]{templateKey, json, writer});
        } catch (JavaScriptException e) {
            throw new DustViewException("thrown error when Rendering Dust JS Source", e);
        } finally {
            Context.exit();
        }
    }

    /**
     * Resolve File InputStream by Path
     *
     * @param filePath
     * @return
     */
    public InputStream getDustJsStream(String filePath) {
        InputStream resourceStream = getClass().getResourceAsStream(filePath);
        if (resourceStream == null) {
            throw new IllegalArgumentException("Incorrectly filePath! '" + filePath + "' does not exist!");
        }
        return resourceStream;
    }

    /*
     * Getter & Setter Method
	 */
    public String getDustJsFilePath() {
        return dustJsFilePath;
    }

    public void setDustJsFilePath(String dustJsFilePath) {
        this.dustJsFilePath = dustJsFilePath;
    }

    public String getDustJsHelperFilePath() {
        return dustJsHelperFilePath;
    }

    public String getDsutJsExtensionFilePath() {
        return dsutJsExtensionFilePath;
    }

    public void setDustJsHelperFilePath(String dustJsHelperFilePath) {
        this.dustJsHelperFilePath = dustJsHelperFilePath;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setCompileScript(String compileScript) {
        this.compileScript = compileScript;
    }

    public void setLoadScript(String loadScript) {
        this.loadScript = loadScript;
    }

    public void setRenderScript(String renderScript) {
        this.renderScript = renderScript;
    }

    public void setCompileSourceName(String compileSourceName) {
        this.compileSourceName = compileSourceName;
    }


    public void setDsutJsExtensionFilePath(String dsutJsExtensionFilePath) {
        this.dsutJsExtensionFilePath = dsutJsExtensionFilePath;
    }
}

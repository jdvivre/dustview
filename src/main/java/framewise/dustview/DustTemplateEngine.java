package framewise.dustview;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Support server-side dust rendering Function. This class load by Rhino JavaScript Engine.
 *
 * @author chanwook
 */
public class DustTemplateEngine {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final int DEFAULT_OPTIMIZATION_LEVEL = -1;
    private static final String DEFAULT_COMPILE_SOURCE_NAME = "ServerSideDustCompiler";
    private static final String DEFAULT_DUST_JS_FILE_PATH = "/dust/dust-full-1.1.1.js";
    private static final String DEFAULT_DUST_HELPER_JS_FILE_PATH = "/dust/dust-helpers-1.1.1.js";
    private static final String DEFAULT_COMPILE_SCRIPT = "function dustCompile(templateKey, source){ return dust.compile(source, templateKey); }";
    private static final String DEFAULT_LOAD_SCRIPT = "function dustLoad(source) { dust.loadSource(source); }";
    private static final String DEFAULT_RENDER_SCRIPT =
            "function dustRender(templateKey,_writer,_error, json) {" +
                    "return dust.render(templateKey,JSON.parse(json)," +
                    "function(err, out){" +
                    "if(out){ _writer.write(out); }" +
                    "if(err){ _error.write(err); }" +
                    "}" +
                    ");" +
                    "}";

    private static final String DEFAULT_ENCODING = "UTF-8";

    private Scriptable globalScope;

    private String compileSourceName = DEFAULT_COMPILE_SOURCE_NAME;
    private String dustJsFilePath = DEFAULT_DUST_JS_FILE_PATH;
    private String dustJsHelperFilePath = DEFAULT_DUST_HELPER_JS_FILE_PATH;
    private String encoding = DEFAULT_ENCODING;
    private String compileScript = DEFAULT_COMPILE_SCRIPT;
    private String loadScript = DEFAULT_LOAD_SCRIPT;
    private String renderScript = DEFAULT_RENDER_SCRIPT;
    // value: -1 ~ 9
    private int optimizationLevel = DEFAULT_OPTIMIZATION_LEVEL;

    private Map<String, String> compiledSourceCache = new HashMap<String, String>();

    /**
     * Create dust engine object with initialization
     */
    public DustTemplateEngine() {
        initializeContext();
    }

    /**
     * Create dust engine object with initialization if needs
     *
     * @param isInitialize
     */
    public DustTemplateEngine(boolean isInitialize) {
        if (isInitialize) {
            initializeContext();
        }
    }

    /**
     * dust context initialize method. must call before running dust
     */
    public void initializeContext() {
        loadScriptFile(getDustJsFilePath());

        // file exist
        if (getDustJsHelperFilePath().length() > 0) {
            loadScriptFile(getDustJsHelperFilePath());
        }

        // loading dust load & rendering script
        loadingScriptToEngine();
    }

    public void loadScriptFile(String filePath) {
        InputStream fileStream = null;
        Reader fileReader = null;

        Context context = Context.enter();
        try {
            fileStream = getDustJsStream(filePath);
            fileReader = new InputStreamReader(fileStream, encoding);

            context.setOptimizationLevel(optimizationLevel);
            if (globalScope == null) {
                globalScope = context.initStandardObjects();
            }
            context.evaluateReader(globalScope, fileReader, filePath, fileStream.available(), null);

        } catch (Exception e) {
            throw new DustViewException("Throwing exception when initialize step for core engine!", e);
        } finally {
            Context.exit();

            try {
                if (fileStream != null) {
                    fileStream.close();
                }

                if (fileReader != null) {
                    fileReader.close();
                }

            } catch (Exception e) {
                logger.error("Fail to dust engine loading!!", e);
                throw new DustViewException(e);
            }

        }
    }

    /**
     * Loading dust execution script. (compile-load-render script)
     */
    protected void loadingScriptToEngine() {
        Context context = Context.enter();
        try {
            context.setOptimizationLevel(optimizationLevel);

            // loading dust execution script
            context.evaluateString(globalScope, compileScript, compileSourceName, 0, null);
            context.evaluateString(globalScope, loadScript, compileSourceName, 0, null);
            context.evaluateString(globalScope, renderScript, compileSourceName, 0, null);

        } catch (Exception e) {
            throw new DustViewException("Throwing exception when initialize step for core engine!", e);
        } finally {
            Context.exit();
        }
    }

    public void loadExtensionFunction(String filePath) {
        Reader dustJsExtensionReader = null;
        InputStream stream = null;
        Context context = Context.enter();
        try {
            stream = getDustJsStream(filePath);
            dustJsExtensionReader = new InputStreamReader(stream, encoding);
            context.evaluateReader(globalScope, dustJsExtensionReader, filePath, stream.available(), null);
        } catch (Exception e) {
            throw new DustViewException("thrown exception when initialize step for extension!", e);
        } finally {
            Context.exit();

            try {
                if (stream != null) {
                    stream.close();
                }
                if (dustJsExtensionReader != null) {
                    dustJsExtensionReader.close();
                }
            } catch (IOException e) {
                logger.error("Fail to load dust extension file!!", e);
                throw new DustViewException(e);
            }
        }
    }

    /**
     * Compile HTML Markup that used by Dust.js
     *
     * @param templateKey
     * @param source      HTML Markup Source
     * @return Compiled HTML Markup(JavaScript Format)
     */
    public String compile(String templateKey, String source) {
        final Context context = Context.enter();
        try {
            context.setOptimizationLevel(optimizationLevel);
            Function fct = (Function) globalScope.get("dustCompile", globalScope);
            String compiled = (String) fct.call(context, globalScope, globalScope, new Object[]{templateKey, source});
            return compiled;
        } catch (JavaScriptException e) {
            throw new DustViewException("thrown error when compile Dust JS Source", e);
        } finally {
            Context.exit();
        }
    }

    /**
     * Load Compiled Markup Source to JavaScript Object
     *
     * @param templateKey
     * @param compiledSource load target HTML Markup
     */
    public boolean load(String templateKey, String compiledSource) {
        if (isLoad(templateKey, compiledSource)) {
            if (logger.isDebugEnabled()) {
                logger.info("Not load to browser engine (because using compiled source cache!! (templateKey: " + templateKey + ")");
            }
            return false;
        }

        final Context context = Context.enter();
        try {
            if (logger.isInfoEnabled()) {
                logger.info("Compiled resource load to script engine!! (templateKey: " + templateKey + ")");
            }

            context.setOptimizationLevel(optimizationLevel);
            Function fct = (Function) globalScope.get("dustLoad", globalScope);
            fct.call(context, globalScope, globalScope, new Object[]{compiledSource});

            if (logger.isInfoEnabled()) {
                logger.info("Add to compiled resource to cache!! (templateKey: " + templateKey + ")");
            }
            compiledSourceCache.put(templateKey, compiledSource);
            return true;
        } catch (JavaScriptException e) {
            throw new DustViewException("thrown error when load Dust JS Source", e);
        } finally {
            Context.exit();
        }
    }

    protected boolean isLoad(String templateKey, String compiledSource) {
        if (compiledSourceCache.containsKey(templateKey) && compiledSourceCache.containsValue(compiledSource)) {
            return true;
        }
        return false;
    }

    /**
     * Rendering Markup. result is binded to Markup with JSON data.
     * Result is plain text HTML markup, then will write to {@link Writer} object.
     *
     * @param writer
     * @param errorWriter
     * @param templateKey
     * @param json
     */
    public void render(Writer writer, StringWriter errorWriter, String templateKey, String json) {
        final Context context = Context.enter();
        try {
            context.setOptimizationLevel(optimizationLevel);

            Function fct = (Function) globalScope.get("dustRender", globalScope);
            fct.call(context, globalScope, globalScope, new Object[]{templateKey, writer, errorWriter, json});
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

    public void setOptimizationLevel(int optimizationLevel) {
        this.optimizationLevel = optimizationLevel;
    }

    public int getOptimizationLevel() {
        return optimizationLevel;
    }

    public String getLoadScript() {
        return loadScript;
    }

    public String getRenderScript() {
        return renderScript;
    }

    public String getCompileScript() {
        return compileScript;
    }

    public String getCompileSourceName() {
        return compileSourceName;
    }

    public String getEncoding() {
        return encoding;
    }
}

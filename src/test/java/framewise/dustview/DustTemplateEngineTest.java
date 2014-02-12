package framewise.dustview;

import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import java.io.InputStream;
import java.io.StringWriter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Test for DustTemplateEngine class
 *
 * @author chanwook
 */
public class DustTemplateEngineTest {

    @Test
    public void findDustJsFile() throws Exception {
        /** 1.x file */
        DustTemplateEngine engine = new DustTemplateEngine();
        // Load Dust Js file
        InputStream dustJsFileStream = engine.getDustJsStream(engine.getDustJsFilePath());
        assertThat(dustJsFileStream, notNullValue());
        assertThat(dustJsFileStream.available(), is(102697));

        // Load Dust Helper Js file
        InputStream dustJsHelperFileStream = engine.getDustJsStream(engine.getDustJsHelperFilePath());
        assertThat(dustJsHelperFileStream, notNullValue());
        assertThat(dustJsHelperFileStream.available(), is(19780));

        /** 2.x file */
        engine.setDustJsFilePath("/dust/dust-full-2.2.3.js");
        InputStream dust2FileStream = engine.getDustJsStream(engine.getDustJsFilePath());
        assertThat(dust2FileStream, notNullValue());
        assertThat(dust2FileStream.available(), is(122481));
    }

    @Test
    public void isNotLoadHelperScriptWhen2DustVersion() {
        DustTemplateEngine e = new DustTemplateEngine(false);

        e.setDustJsFilePath("/dust/dust-full-2.2.3.js");
        e.setDustJsHelperFilePath("");
        e.initializeContext();
    }

    @Test
    public void compile2load2renderinDustJsSource() throws Exception {
        DustTemplateEngine engine = new DustTemplateEngine();
        String source = "Hello World!";
        // compile
        String compiled = engine.compile("test1", source);
        assertThat(
                compiled,
                is("(function(){dust.register(\"test1\",body_0);function body_0(chk,ctx){return chk.write(\"Hello World!\");}return body_0;})();"));

        // load
        engine.load("t1", compiled);

        // render
        StringWriter writer = new StringWriter();
        StringWriter errorWriter = new StringWriter();
        engine.render(writer, errorWriter, "test1", "{}");
        assertThat(writer.getBuffer().toString(), is("Hello World!"));
    }

    @Test
    public void withCache() throws Exception {
        DustTemplateEngine engine = new DustTemplateEngine();
        String source = "Hello World!";
        // compile
        String compiled1 = engine.compile("test1", source);
        assertThat(
                compiled1,
                is("(function(){dust.register(\"test1\",body_0);function body_0(chk,ctx){return chk.write(\"Hello World!\");}return body_0;})();"));

        // compile
        String compiled2 = engine.compile("test2", source);
        assertThat(
                compiled2,
                is("(function(){dust.register(\"test2\",body_0);function body_0(chk,ctx){return chk.write(\"Hello World!\");}return body_0;})();"));

        // load
        engine.load("t1", compiled1);
        engine.load("t1", compiled2);

        // render
        StringWriter writer1 = new StringWriter();
        StringWriter errorWriter = new StringWriter();
        engine.render(writer1, errorWriter, "test1", "{}");
        assertThat(writer1.getBuffer().toString(), is("Hello World!"));

        // rerender
        StringWriter writer2 = new StringWriter();
        engine.render(writer2, errorWriter, "test2", "{}");
        assertThat(writer2.getBuffer().toString(), is("Hello World!"));

        StringWriter writer3 = new StringWriter();
        engine.render(writer3, errorWriter, "test1", "{}");
        assertThat(writer3.getBuffer().toString(), is("Hello World!"));

    }

    @Test
    public void renderWithJson() throws Exception {
        DustTemplateEngine engine = new DustTemplateEngine();
        String source = "Hello {name} World!";
        // compile
        String compiled = engine.compile("test2", source);
        assertThat(
                compiled,
                is("(function(){dust.register(\"test2\",body_0);function body_0(chk,ctx){return chk.write(\"Hello \").reference(ctx.get(\"name\"),ctx,\"h\").write(\" World!\");}return body_0;})();"));

        // load
        engine.load("t1", compiled);

        // render
        StringWriter writer = new StringWriter();
        StringWriter errorWriter = new StringWriter();
        engine.render(writer, errorWriter, "test2", "{\"name\":\"chanwook\"}");
        assertThat(writer.getBuffer().toString(), is("Hello chanwook World!"));
    }

    @Test
    public void templateKeyNotFound() throws Exception {
        DustTemplateEngine engine = new DustTemplateEngine();
        String source = "Hello {name} World!";
        // compile
        String compiled = engine.compile("test", source);
        assertThat(
                compiled,
                is("(function(){dust.register(\"test\",body_0);function body_0(chk,ctx){return chk.write(\"Hello \").reference(ctx.get(\"name\"),ctx,\"h\").write(\" World!\");}return body_0;})();"));

        // load
        engine.load("t1", compiled);

        // render
        StringWriter writer = new StringWriter();
        StringWriter errorWriter = new StringWriter();
        engine.render(writer, errorWriter, "not-found", "{\"name\":\"chanwook\"}");
        assertThat(errorWriter.getBuffer().toString(), is("Error: Template Not Found: not-found"));
    }

    @Test
    public void loadExtensionFile() {
        DustTemplateEngine e = new DustTemplateEngine(false);
        e.setDustExtensionFilePath("/dust/dust-extension-test.js");
        e.initializeContext();

        final Context context = Context.enter();
        try {
            Scriptable globalScope = e.getGlobalScope();

            Function fct = (Function) globalScope.get("extFunction", globalScope);
            String result = (String) fct.call(context, globalScope, globalScope, new Object[]{});

            assertThat(result, is("success!"));
        } finally {
            Context.exit();
        }

    }
}

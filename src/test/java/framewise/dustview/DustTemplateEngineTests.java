package framewise.dustview;

import org.junit.Test;

import java.io.InputStream;
import java.io.StringWriter;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 *
 * Test for DustTemplateEngine class
 *
 * @author chanwook
 */
class DustTemplateEngineTests {

	@Test
	public void findDustJsFile() throws Exception {
		DustTemplateEngine engine = new DustTemplateEngine();
		// Load Dust Js file
		InputStream dustJsFileStream = engine.getDustJsStream(engine.getDustJsFilePath());
		assertThat(dustJsFileStream, notNullValue());
		assertThat(dustJsFileStream.available(), is(102697));

		// Load Dust Helper Js file
		InputStream dustJsHelperFileStream = engine.getDustJsStream(engine.getDustJsHelperFilePath());
		assertThat(dustJsHelperFileStream, notNullValue());
		assertThat(dustJsHelperFileStream.available(), is(18324));
	}

	@Test
	public void compile2load2renderinDustJsSource() throws Exception {
		DustTemplateEngine engine = new DustTemplateEngine();
		String source = "Hello World!";
		// compile
		String compiled = engine.compile(source, "test1");
		assertThat(
				compiled,
				is("(function(){dust.register(\"test1\",body_0);function body_0(chk,ctx){return chk.write(\"Hello World!\");}return body_0;})();"));

		// load
		engine.load(compiled);

		// render
		StringWriter writer = new StringWriter();
		engine.render(writer, "test1", "{}");
		assertThat(writer.getBuffer().toString(), is("Hello World!"));
	}

	@Test
	public void renderWithJson() throws Exception {
		DustTemplateEngine engine = new DustTemplateEngine();
		String source = "Hello {name} World!";
		// compile
		String compiled = engine.compile(source, "test2");
		assertThat(
				compiled,
				is("(function(){dust.register(\"test2\",body_0);function body_0(chk,ctx){return chk.write(\"Hello \").reference(ctx.get(\"name\"),ctx,\"h\").write(\" World!\");}return body_0;})();"));

		// load
		engine.load(compiled);

		// render
		StringWriter writer = new StringWriter();
		engine.render(writer, "test2", "{\"name\":\"chanwook\"}");
		assertThat(writer.getBuffer().toString(), is("Hello chanwook World!"));
	}

	@Test
	public void thrownExceptionAtUncorrenctPath() throws Exception {
		// TODO ..
	}
}

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
		DustTemplateEngine loader = new DustTemplateEngine();
		// Load Dust Js file
		InputStream dustJsFileStream = loader.getDustJsStream(loader.getDustJsFilePath());
		assertThat(dustJsFileStream, notNullValue());
		assertThat(dustJsFileStream.available(), is(102697));

		// Load Dust Helper Js file
		InputStream dustJsHelperFileStream = loader.getDustJsStream(loader.getDustJsHelperFilePath());
		assertThat(dustJsHelperFileStream, notNullValue());
		assertThat(dustJsHelperFileStream.available(), is(18324));
	}

	@Test
	public void loadJsContextToDustJs() throws Exception {
		DustTemplateEngine loader = new DustTemplateEngine();
		loader.initializeContext();
	}

	@Test
	public void compile2load2renderinDustJsSource() throws Exception {
		DustTemplateEngine loader = new DustTemplateEngine();
		loader.initializeContext();
		String source = "Hello World!";
		// compile
		String compiled = loader.compile(source, "test1");
		assertThat(
				compiled,
				is("(function(){dust.register(\"test1\",body_0);function body_0(chk,ctx){return chk.write(\"Hello World!\");}return body_0;})();"));

		// load
		loader.load(compiled);

		// render
		StringWriter writer = new StringWriter();
		loader.render(writer, "test1", "{}");
		assertThat(writer.getBuffer().toString(), is("Hello World!"));
	}

	@Test
	public void renderWithJson() throws Exception {
		DustTemplateEngine loader = new DustTemplateEngine();
		loader.initializeContext();
		String source = "Hello {name} World!";
		// compile
		String compiled = loader.compile(source, "test2");
		assertThat(
				compiled,
				is("(function(){dust.register(\"test2\",body_0);function body_0(chk,ctx){return chk.write(\"Hello \").reference(ctx.get(\"name\"),ctx,\"h\").write(\" World!\");}return body_0;})();"));

		// load
		loader.load(compiled);

		// render
		StringWriter writer = new StringWriter();
		loader.render(writer, "test2", "{\"name\":\"chanwook\"}");
		assertThat(writer.getBuffer().toString(), is("Hello chanwook World!"));
	}

	@Test
	public void thrownExceptionAtUncorrenctPath() throws Exception {
		// TODO ..
	}
}

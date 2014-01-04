DustView
========

Server-Side Dust Rendering Support Library 

# Using DustTemplateEigine

        DustTemplateEngine engine = new DustTemplateEngine();
        
        // Compile Template
        String source = "Hello {name} World!";
        String compiled = engine.compile(source, "templateKey");
        
        // Load to Context
        engine.load(compiled);
        
        // Rendering to Text
        StringWriter writer = new StringWriter();
        engine.render(writer, "templateKey", "{\"name\":\"chanwook\"}");
        
        writer.getBuffer().toString()
        
        '''

# Using DustTemplateView for Spring framework

## Java Configuration
   
        @Configuration
        @EnableWebMvc
        @ComponentScan(basePackages = {"packageName"}, useDefaultFilters = false,
                includeFilters = {@ComponentScan.Filter(value = {Controller.class})})
        public class WebContextConfig extends WebMvcConfigurerAdapter {
        
            @Bean
            public ViewResolver getDustViewResolver() {
                InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
                viewResolver.setPrefix("/WEB-INF/pages/");
                viewResolver.setSuffix(".jsp");
                viewResolver.setViewClass(SimpleDustTemplateView.class);
        
                // set attribute for View instance
                HashMap<String, Object> attributes = new HashMap<String, Object>();
                attributes.put(SimpleDustTemplateView.TEMPLATE_LOADER, new HttpConnectDustViewTemplateLoader());
                attributes.put(SimpleDustTemplateView.VIEW_PATH_PREFIX, "http://webresource_path/..");
                attributes.put(SimpleDustTemplateView.VIEW_PATH_SUFFIX, "/markup.js");
                viewResolver.setAttributesMap(attributes);
        
                return viewResolver;
            }
        
        }

## XML Configuration


## Refresh cache to template source 
DustTemplateView is cache template file source for one time access to file. 
If you want to refresh template source, maybe redeploy your view source(dust compiled HTML), you call server with specific parameter(_refresh). Parameter value is "y" or "Y" (case insensitive).

        http://...?_refresh=y&.. or
        http://...?_refresh=Y&..
        
If you want that don't using caching, maybe development or test environment that frequently deploy view source(html), you possibly disable function.
Default value is true(=caching)!

    SimpleDustTemplateView.setViewCacheable(false);


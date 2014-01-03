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

DustView
========

Server-Side Dust Rendering Support Library

> DustView 1.0.M1 support dust 1.x.
> if using dust 2.x is DustView 1.0.M2 version or more.

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
            public ViewResolver getCnvr() {
                ContentNegotiatingViewResolver viewResolver = new ContentNegotiatingViewResolver();
        
                // Setting to ViewResolver List
                ArrayList<ViewResolver> viewResolvers = new ArrayList<ViewResolver>();
                viewResolvers.add(getDustViewResolver());
                viewResolvers.add(new BeanNameViewResolver());
                viewResolver.setViewResolvers(viewResolvers);
        
                // Setting to Default View
                ArrayList<View> defaultViews = new ArrayList<View>();
                defaultViews.add(new MappingJackson2JsonView());
                viewResolver.setDefaultViews(defaultViews);
        
                return viewResolver;
            }
            
            @Bean
            public ViewResolver getDustViewResolver() {
                LOGGER.debug(">>> Setup View Resolver for JSP");
        
                InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
                viewResolver.setPrefix("/WEB-INF/pages/");
                viewResolver.setSuffix(".jsp");
                viewResolver.setViewClass(SimpleDustTemplateView.class);
        
                // set attribute for View instance
                HashMap<String, Object> attributes = new HashMap<String, Object>();
                HttpConnectDustTemplateLoader dustTemplateLoader = new HttpConnectDustTemplateLoader();
                attributes.put(SimpleDustTemplateView.TEMPLATE_LOADER, dustTemplateLoader);
                attributes.put(SimpleDustTemplateView.VIEW_PATH_PREFIX, "http://image.server/ui/");
                attributes.put(SimpleDustTemplateView.VIEW_PATH_SUFFIX, "/markup.js");
                attributes.put(SimpleDustTemplateView.VIEW_CACHEABLE, "false");
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


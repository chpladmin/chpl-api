package gov.healthit.chpl;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import gov.healthit.chpl.api.dao.ApiKeyDAO;
import gov.healthit.chpl.filter.APIKeyAuthenticationFilter;
import gov.healthit.chpl.ratelimiting.RateLimitingInterceptor;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.CacheControlHandlerInterceptor;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.log4j.Log4j2;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.ConstructorDetector;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableWebMvc
@EnableAsync
@EnableAspectJAutoProxy
@EnableScheduling
@PropertySources({
    @PropertySource("classpath:/environment.properties"),
    @PropertySource(value = "classpath:/environment-override.properties", ignoreResourceNotFound = true),
    @PropertySource("classpath:/lookup.properties"),
    @PropertySource(value = "classpath:/lookup-override.properties", ignoreResourceNotFound = true),
    @PropertySource("classpath:/email.properties"),
    @PropertySource(value = "classpath:/email-override.properties", ignoreResourceNotFound = true),
})
@ComponentScan(basePackages = {
        "gov.healthit.chpl.**"
})
@Log4j2
public class CHPLConfig implements WebMvcConfigurer, EnvironmentAware {
    private static final int MAX_COOKIE_AGE_SECONDS = 3600;
    private String chplServiceUrl;
    private String apiLicenseUrl;
    private String apiVersion;
    private String apiDescriptionHtml;
    private String feedbackFormUrl;
    private Boolean tryItOutEnabled;

    @Autowired
    private IgnorableResponseFieldAnnotationIntrospector ignorableResponseFieldAnnotationIntrospector;

    @Lazy
    @Autowired
    private ApiKeyDAO apiKeyDAO;

    @Lazy
    @Autowired
    private ErrorMessageUtil errorUtil;

    private Integer rateLimitRequestCount;
    private Integer rateLimitTimePeriod;

    @Override
    public void setEnvironment(Environment e) {
        this.chplServiceUrl = e.getProperty("chplUrlBegin") + e.getProperty("basePath");
        this.apiLicenseUrl = e.getProperty("api.licenseUrl");
        this.apiVersion = e.getProperty("api.version");
        this.apiDescriptionHtml = e.getProperty("api.description");
        this.feedbackFormUrl = e.getProperty("contact.publicUrl");
        this.tryItOutEnabled = BooleanUtils.toBooleanObject(e.getProperty("api.tryItOutEnabled"));

        this.rateLimitRequestCount = Integer.parseInt(e.getProperty("rateLimitRequestCount"));
        this.rateLimitTimePeriod = Integer.parseInt(e.getProperty("rateLimitTimePeriod"));
    }

    @Bean
    @Primary
    public JsonMapper jsonMapper() {
        JsonMapper mapper = JsonMapper.builder()
                .annotationIntrospector(ignorableResponseFieldAnnotationIntrospector)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                        DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES,
                        DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                //Jackson 3.x changed the default rendering of java.util.Date objects to a formatted string.
                //This setting is required to force them to be a milliseconds "long" value
                //Until we convert everything to LocalDateTime or whatever.
                .enable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .findAndAddModules()
                //Jackson 3.x started detecting constructors with args in POJOs and trying to select
                //a constructor that matches the fields they have.
                //All of our Lombok code provides no-arg and all-args constructors, when, if used
                //by Jackson and a field is MISSING (not present at all) in the request and the all-args
                //constructor is detected/used that field ends up being null because it's setter is never called.
                //The initializing expression that we had in the object with @Builder.Default
                //seemed to be recognized and used in Jackson 2.x but is no longer when the field is
                //missing in the JSON input.
                //I tried a lot of different ways to get that to be recognized, but it seems like a
                //special situation where the field is totally missing that nothing was working.
                //This constructorDetector setting forces the Jackson 3
                //deserialization to use Lombok's no-args constructor which WILL have the Builder.Default
                //initialized values.
                .constructorDetector(ConstructorDetector.DEFAULT
                        // Disables auto-detection of constructors with arguments
                        // unless they have @JsonCreator or @JsonProperty annotations
                        .withAllowImplicitWithDefaultConstructor(false))
                .build();
        return mapper;
    }

    @Bean
    public JacksonJsonHttpMessageConverter jsonConverter() {
        JacksonJsonHttpMessageConverter bean = new JacksonJsonHttpMessageConverter(jsonMapper());
        bean.setPrefixJson(false);
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        bean.setSupportedMediaTypes(mediaTypes);
        return bean;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        //We are overriding the entire set of available message converters in Spring.
        //This is the only way I could find to get our custom JsonMapper recognized and
        //used in the application.
        //There is a known issue with OpenAPI where it will return the "api" JSON as
        //a Base-64 encoded string if the ByteArray and String message converters are not
        //available. So, below we first add those (and they have to be first so OpenAPI
        //doesn't choose our JSON Converter). Then we add our own custom JSON Converter
        //for our API responses.
        converters.add(new ByteArrayHttpMessageConverter());
        converters.add(new StringHttpMessageConverter());
        converters.add(jsonConverter());
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("errors-override");

        ResourceBundleMessageSource parentMessageSource = new ResourceBundleMessageSource();
        parentMessageSource.setBasename("errors");

        messageSource.setParentMessageSource(parentMessageSource);
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public CookieLocaleResolver localeResolver() {
        CookieLocaleResolver localeResolver = new CookieLocaleResolver("my-locale-cookie");
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        localeResolver.setCookieMaxAge(Duration.ofSeconds(MAX_COOKIE_AGE_SECONDS));
        return localeResolver;
    }

    @Bean
    public LocaleChangeInterceptor localeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Bean
    public RateLimitingInterceptor rateLimitingInterceptor() {
        RateLimitingInterceptor interceptor = new RateLimitingInterceptor(apiKeyDAO, errorUtil, rateLimitRequestCount, rateLimitTimePeriod);
        return interceptor;
    }

    @Bean
    public CacheControlHandlerInterceptor cacheControlHandlerInterceptor() {
        CacheControlHandlerInterceptor interceptor = new CacheControlHandlerInterceptor();
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeInterceptor());
        registry.addInterceptor(rateLimitingInterceptor())
        .addPathPatterns("/**")
        .excludePathPatterns(APIKeyAuthenticationFilter.ALLOWED_REQUEST_PATHS);
        registry.addInterceptor(cacheControlHandlerInterceptor());
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        LOGGER.info("Get BCryptPasswordEncoder");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public OpenAPI chplOpenAPI() {
        OpenAPI api = new OpenAPI()
                .info(new Info().title("Certified Health IT Product Listing API")
                .version(apiVersion)
                .description(String.format(apiDescriptionHtml, feedbackFormUrl, feedbackFormUrl))
                .license(new License().name("BSD License").url(apiLicenseUrl)))
                .addServersItem(new Server().url(chplServiceUrl));
        if (BooleanUtils.isTrue(tryItOutEnabled)) {
            api.setComponents(new Components()
                .addSecuritySchemes(SwaggerSecurityRequirement.API_KEY,
                        new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(In.HEADER).name("API-Key").scheme("API-Key"))
                .addSecuritySchemes(SwaggerSecurityRequirement.BEARER,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).in(In.HEADER).name("Bearer").scheme("Bearer").bearerFormat("JWT")));
        }
        return api;
    }

    @Bean
    public OpenApiCustomizer sortTagsAlphabetically() {
        return openApi -> openApi.setTags(openApi.getTags()
                .stream()
                .sorted(Comparator.comparing(tag -> StringUtils.stripAccents(tag.getName())))
                .collect(Collectors.toList()));
    }

    @Bean
    public OpenApiCustomizer sortSchemasAlphabetically() {
        return openApi -> {
            Map<String, Schema> schemas = openApi.getComponents().getSchemas();
            openApi.getComponents().setSchemas(new TreeMap<>(schemas));
        };
    }
}

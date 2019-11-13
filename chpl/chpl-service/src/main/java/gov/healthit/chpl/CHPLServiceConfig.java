package gov.healthit.chpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.castor.CastorMarshaller;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import gov.healthit.chpl.job.ExportQuarterlySurveillanceReportJob;
import gov.healthit.chpl.job.MeaningfulUseUploadJob;

@Configuration
@Import(ChplCacheConfig.class)
@EnableWebMvc
@EnableTransactionManagement(proxyTargetClass = true)
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
@EnableAsync
@EnableAspectJAutoProxy
@EnableScheduling
@EnableCaching
@PropertySources({
        @PropertySource("classpath:/environment.properties"),
        @PropertySource(value = "classpath:/environment-override.properties", ignoreResourceNotFound = true),
        @PropertySource("classpath:/lookup.properties"),
        @PropertySource(value = "classpath:/lookup.properties", ignoreResourceNotFound = true),
})
@ComponentScan(basePackages = {
        "org.springframework.security.**", "org.springframework.core.env.**", "gov.healthit.chpl.**"
})
public class CHPLServiceConfig extends WebMvcConfigurerAdapter implements EnvironmentAware {

    private static final Logger LOGGER = LogManager.getLogger(CHPLServiceConfig.class);
    private static final int MAX_UPLOAD_SIZE_BYTES = 5242880; // 5MB
    private static final int MAX_COOKIE_AGE_SECONDS = 3600; // 1 hour
    private static final int CORE_POOL_SIZE = 10;
    private static final int MAX_POOL_SIZE = 100;
    private static final int JOB_CORE_POOL_SIZE = 3;
    private static final int JOB_MAX_POOL_SIZE = 6;

    @Autowired
    private Environment env;

    @Override
    public void setEnvironment(final Environment environment) {
        this.env = environment;
    }

    @Bean
    public MappingJackson2HttpMessageConverter jsonConverter() {
        MappingJackson2HttpMessageConverter bean = new MappingJackson2HttpMessageConverter();
        bean.setPrefixJson(false);
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        bean.setSupportedMediaTypes(mediaTypes);
        return bean;
    }

    @Bean
    public org.springframework.orm.jpa.LocalEntityManagerFactoryBean entityManagerFactory() {
        LOGGER.info("get LocalEntityManagerFactoryBean");
        org.springframework.orm.jpa.LocalEntityManagerFactoryBean bean = new org.springframework.orm.jpa.LocalEntityManagerFactoryBean();
        bean.setPersistenceUnitName(env.getRequiredProperty("persistenceUnitName"));
        return bean;
    }

    @Bean
    public org.springframework.orm.jpa.JpaTransactionManager transactionManager() {
        LOGGER.info("get JpaTransactionManager");
        org.springframework.orm.jpa.JpaTransactionManager bean = new org.springframework.orm.jpa.JpaTransactionManager();
        bean.setEntityManagerFactory(entityManagerFactory().getObject());
        return bean;
    }

    @Bean
    public org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor persistenceAnnotationBeanPostProcessor() {
        LOGGER.info("get PersistenceAnnotationBeanPostProcessor");
        return new org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor();
    }

    @Bean(name = "multipartResolver")
    public CommonsMultipartResolver getResolver() throws IOException {
        LOGGER.info("get CommonsMultipartResolver");
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();

        // Set the maximum allowed size (in bytes) for each individual file.
        resolver.setMaxUploadSize(MAX_UPLOAD_SIZE_BYTES);

        // You may also set other available properties.

        return resolver;
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
        te.setCorePoolSize(CORE_POOL_SIZE);
        te.setMaxPoolSize(MAX_POOL_SIZE);
        return te;
    }

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(5);
        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
        return threadPoolTaskScheduler;
    }

    @Bean
    public Marshaller marshaller() {
        LOGGER.info("get Marshaller");
        return new CastorMarshaller();
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
        CookieLocaleResolver localeResolver = new CookieLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        localeResolver.setCookieName("my-locale-cookie");
        localeResolver.setCookieMaxAge(MAX_COOKIE_AGE_SECONDS);
        return localeResolver;
    }

    @Bean
    public LocaleChangeInterceptor localeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Bean
    public InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setViewClass(JstlView.class);
        viewResolver.setPrefix("/webapp/WEB-INF/jsp/");
        viewResolver.setSuffix(".jsp");
        return viewResolver;
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(localeInterceptor());
    }

    @Bean
    public MeaningfulUseUploadJob meaningfulUseUploadJob() {
        return new MeaningfulUseUploadJob();
    }

    @Bean
    public ExportQuarterlySurveillanceReportJob exportQuarterlySurveillanceReportJob() {
        return new ExportQuarterlySurveillanceReportJob();
    }

    /**
     * Get a task executor.
     * 
     * @return TaskExecutor object
     */
    @Bean(name = "jobAsyncDataExecutor")
    public TaskExecutor specificTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(JOB_CORE_POOL_SIZE);
        executor.setMaxPoolSize(JOB_MAX_POOL_SIZE);
        // executor.setCorePoolSize(Integer.parseInt(props.getProperty("corePoolSize")));
        // executor.setMaxPoolSize(Integer.parseInt(props.getProperty("maxPoolSize")));
        // executor.setQueueCapacity(11);
        executor.setThreadNamePrefix("jobDataThread");
        executor.initialize();
        return executor;
    }

    @Bean
    public MethodInvokingFactoryBean methodInvokingFactoryBean() {
        MethodInvokingFactoryBean methodInvokingFactoryBean = new MethodInvokingFactoryBean();
        methodInvokingFactoryBean.setTargetClass(SecurityContextHolder.class);
        methodInvokingFactoryBean.setTargetMethod("setStrategyName");
        methodInvokingFactoryBean.setArguments(new String[] {
                SecurityContextHolder.MODE_INHERITABLETHREADLOCAL
        });
        return methodInvokingFactoryBean;
    }
}

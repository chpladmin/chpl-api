package gov.healthit.chpl.scheduler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

/**
 * Configuration used to make various Spring managed things available to Quartz Jobs.
 * @author alarned
 *
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@ComponentScan(basePackages = {
        "org.springframework.security.**", "org.springframework.core.env.**", "gov.healthit.chpl.util.**",
        "gov.healthit.chpl.auth.**", "gov.healthit.chpl.dao.**", "gov.healthit.chpl.entity.**",
        "gov.healthit.chpl.auth.manager.**", "gov.healthit.chpl.manager.**", "gov.healthit.chpl.upload.**",
        "gov.healthit.chpl.validation.**", "gov.healthit.chpl.scheduler.**"
}, lazyInit = true, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ANNOTATION, value = Configuration.class)
})
public class JobConfig {

    private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";

    private Properties props;

    protected void loadProperties() throws IOException {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE);

        if (in == null) {
            props = null;
            throw new FileNotFoundException("Environment Properties File not found in class path.");
        } else {
            props = new Properties();
            props.load(in);
            in.close();
        }
    }

    /**
     * Get properties.
     * @return the properties
     */
    @Bean
    public Properties properties() {
        if (props == null) {
            try {
                loadProperties();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return props;
    }

    /**
     * Get the entity manager factory.
     * @return the factory
     */
    @Bean
    public org.springframework.orm.jpa.LocalEntityManagerFactoryBean entityManagerFactory() {

        if (props == null) {
            try {
                loadProperties();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        org.springframework.orm.jpa.LocalEntityManagerFactoryBean bean =
                new org.springframework.orm.jpa.LocalEntityManagerFactoryBean();
        bean.setPersistenceUnitName(props.getProperty("persistenceUnitName"));
        return bean;
    }

    /**
     * Get the transaction manager.
     * @return the manager
     */
    @Bean/*(name="txMgr")*/
    public org.springframework.orm.jpa.JpaTransactionManager transactionManager() {
        org.springframework.orm.jpa.JpaTransactionManager bean =
                new org.springframework.orm.jpa.JpaTransactionManager();
        bean.setEntityManagerFactory(entityManagerFactory().getObject());
        return bean;
    }

    /**
     * Get the persistence annotation bean post processor.
     * @return the processor
     */
    @Bean
    public org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor
    persistenceAnnotationBeanPostProcessor() {
        return new org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor();
    }

    /**
     * Get a message source.
     * @return the message source
     */
    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:/errors");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    /**
     * Get the locale resolver.
     * @return the locale resolver
     */
    @Bean
    public CookieLocaleResolver localeResolver() {
        final int maxAge = 3600;
        CookieLocaleResolver localeResolver = new CookieLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        localeResolver.setCookieName("my-locale-cookie");
        localeResolver.setCookieMaxAge(maxAge);
        return localeResolver;
    }

    /**
     * Set a parameter on the interceptor.
     * @return the interceptor
     */
    @Bean
    public LocaleChangeInterceptor localeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    /**
     * Get a view resolver.
     * @return the resolver
     */
    @Bean
    public InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setViewClass(JstlView.class);
        viewResolver.setPrefix("/webapp/WEB-INF/jsp/");
        viewResolver.setSuffix(".jsp");
        return viewResolver;
    }
    
    @Bean(name = "jobAsyncDataExecutor")
    public TaskExecutor specificTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        //executor.setQueueCapacity(11);
        executor.setThreadNamePrefix("chartDataThread");
        executor.initialize();
        return executor;
    }
}

package gov.healthit.chpl.app;

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

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@ComponentScan(basePackages = {
        "org.springframework.security.**", "org.springframework.core.env.**", "gov.healthit.chpl.util.**",
        "gov.healthit.chpl.auth.**", "gov.healthit.chpl.dao.**", "gov.healthit.chpl.entity.**",
        "gov.healthit.chpl.auth.manager.**", "gov.healthit.chpl.manager.**", "gov.healthit.chpl.upload.**",
        "gov.healthit.chpl.validation.**", "gov.healthit.chpl.app.**"
}, lazyInit = true, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ANNOTATION, value = Configuration.class)
})
public class AppConfig {

    public static final String DEFAULT_PROPERTIES_FILE = "environment.properties";

    protected Properties props;

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

    @Bean
    public org.springframework.orm.jpa.LocalEntityManagerFactoryBean entityManagerFactory() {

        if (props == null) {
            try {
                loadProperties();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        org.springframework.orm.jpa.LocalEntityManagerFactoryBean bean = new org.springframework.orm.jpa.LocalEntityManagerFactoryBean();
        bean.setPersistenceUnitName(props.getProperty("persistenceUnitName"));
        return bean;
    }

    @Bean/*(name="txMgr")*/
    public org.springframework.orm.jpa.JpaTransactionManager transactionManager() {
        org.springframework.orm.jpa.JpaTransactionManager bean = new org.springframework.orm.jpa.JpaTransactionManager();
        bean.setEntityManagerFactory(entityManagerFactory().getObject());
        return bean;
    }

    @Bean
    public org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor persistenceAnnotationBeanPostProcessor() {
        return new org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor();
    }

    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:/errors");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public CookieLocaleResolver localeResolver() {
        CookieLocaleResolver localeResolver = new CookieLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        localeResolver.setCookieName("my-locale-cookie");
        localeResolver.setCookieMaxAge(3600);
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
}

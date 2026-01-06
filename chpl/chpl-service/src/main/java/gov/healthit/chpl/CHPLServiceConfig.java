package gov.healthit.chpl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
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
        @PropertySource("classpath:/errors.properties"),
        @PropertySource(value = "classpath:/errors-override.properties", ignoreResourceNotFound = true),
})
@ComponentScan(basePackages = {
        "gov.healthit.chpl.**"
})
public class CHPLServiceConfig implements EnvironmentAware {

    private static final Logger LOGGER = LogManager.getLogger(CHPLServiceConfig.class);
    private static final int MAX_COOKIE_AGE_SECONDS = 3600; // 1 hour
    private static final int CORE_POOL_SIZE = 10;
    private static final int MAX_POOL_SIZE = 100;
    private static final int JOB_CORE_POOL_SIZE = 3;
    private static final int JOB_MAX_POOL_SIZE = 6;
    private static final int DEFAULT_REQUEST_TIMEOUT = 10000;
    private static final int THREAD_POOL_TASK_THREAD = 5;

    @Autowired
    private Environment env;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void setEnvironment(final Environment environment) {
        this.env = environment;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LOGGER.info("get LocalContainerEntityManagerFactoryBean");
        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setDataSource(dataSource());
        bean.setPackagesToScan("gov.healthit.chpl");
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        bean.setJpaVendorAdapter(vendorAdapter); // Use Hibernate as the JPA provider
        bean.setJpaProperties(additionalProperties()); // Set JPA/Hibernate specific properties

        return bean;
    }

    @Bean
    public DataSource dataSource() {
        JndiDataSourceLookup lookup = new JndiDataSourceLookup();
        // The JNDI name must match the resource name in Tomcat
        return lookup.getDataSource("java:/comp/env/jdbc/openchpl");
    }

    private Properties additionalProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.flush_before_completion", "true");
        properties.setProperty("hibernate.c3p0.min_size", "5");
        properties.setProperty("hibernate.c3p0.max_size", "20");
        properties.setProperty("hibernate.c3p0.timeout", "300");
        properties.setProperty("hibernate.c3p0.max_statements", "50");
        properties.setProperty("hibernate.c3p0.idle_test_period", "3000");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

        // Set the two below properties to true to see the generated SQL
        // Very useful for debugging
        properties.setProperty("hibernate.show_sql", "false");
        properties.setProperty("hibernate.format_sql", "false");
        return properties;
    }

    @Bean
    public org.springframework.orm.jpa.JpaTransactionManager transactionManager() {
        LOGGER.info("get JpaTransactionManager");
        org.springframework.orm.jpa.JpaTransactionManager bean = new org.springframework.orm.jpa.JpaTransactionManager();
        bean.setEntityManagerFactory(entityManagerFactory().getObject());
        return bean;
    }

    @Bean
    public static org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor persistenceAnnotationBeanPostProcessor() {
        LOGGER.info("get PersistenceAnnotationBeanPostProcessor");
        return new org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor();
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
        threadPoolTaskScheduler.setPoolSize(THREAD_POOL_TASK_THREAD);
        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
        return threadPoolTaskScheduler;
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
        methodInvokingFactoryBean.setArguments(
                SecurityContextHolder.MODE_INHERITABLETHREADLOCAL
        );
        return methodInvokingFactoryBean;
    }

    @Bean
    public RestTemplate jiraAuthenticatedRestTemplate()
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                        .setDefaultSocketConfig(SocketConfig.custom()
                                .setSoTimeout(getRequestTimeout(), TimeUnit.MILLISECONDS)
                                .build())
                        .setTlsSocketStrategy(new DefaultClientTlsStrategy(
                                SSLContexts.custom().loadTrustMaterial(TrustAllStrategy.INSTANCE).build(),
                                NoopHostnameVerifier.INSTANCE))
                        .build())
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        requestFactory.setConnectionRequestTimeout(getRequestTimeout());

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.getInterceptors().add(
                new ClientHttpRequestInterceptor() {
                    private String jiraUsername = env.getRequiredProperty("jira.username");
                    private String jiraPassword = env.getRequiredProperty("jira.password");

                    @Override
                    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
                            throws IOException {
                        String plainCredentials = jiraUsername + ":" + jiraPassword;
                        String base64Credentials = new String(Base64.encodeBase64(plainCredentials.getBytes()));
                        request.getHeaders().add("Authorization", "Basic " + base64Credentials);
                        request.getHeaders().setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
                        request.getHeaders().setAcceptCharset(Arrays.asList(StandardCharsets.UTF_8));
                        //This header was not needed in Spring 6, but in Spring 7, we must specify it here
                        //otherwise a gzip encoding seems to be assumed either on our end of the Jira end
                        //(not sure which even after much digging around).
                        //Without this header, Jira is sending back a content-length header that is shorter than
                        //the actual response content byte length on some requests.
                        //When initially loading all of the direct reviews, the responses from Jira are large and do NOT specify a
                        //content-length header, so Spring just reads all of the bytes in the response.
                        //When requesting direct reviews for a specific developer that has none, the Jira response does include a response header
                        //of content-length. When there are no direct reviews the length is given as 111 bytes (the gzipped length),
                        //but it is not gzipped, or I can't make Spring handle it as though it is, and the value should actually be 116 bytes.
                        //If only 111 bytes out of 116 bytes are read from the response, this ends up being an
                        //incomplete JSON string and gives an error.
                        request.getHeaders().put(HttpHeaders.ACCEPT_ENCODING, Arrays.asList("UTF-8"));
                        return execution.execute(request, body);
                    }
                });
        return restTemplate;
    }

    private int getRequestTimeout() {
        int requestTimeout = DEFAULT_REQUEST_TIMEOUT;
        String requestTimeoutProperty = env.getProperty("jira.requestTimeoutMillis");
        if (!StringUtils.isEmpty(requestTimeoutProperty)) {
            try {
                requestTimeout = Integer.parseInt(requestTimeoutProperty);
            } catch (NumberFormatException ex) {
                LOGGER.warn("Cannot parse " + requestTimeoutProperty + " as an integer. "
                        + "Using the default value " + DEFAULT_REQUEST_TIMEOUT);
            }
        }
        return requestTimeout;
    }

    @Bean
    public JobFactory jobFactory() {
        QuartzJobFactory jobFactory = new QuartzJobFactory(applicationContext);
        return jobFactory;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactory() {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setAutoStartup(true);
        factory.setConfigLocation(new ClassPathResource("quartz.properties"));
        factory.setJobFactory(jobFactory());

        return factory;
    }

}

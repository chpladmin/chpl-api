package gov.healthit.chpl;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.castor.CastorMarshaller;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import gov.healthit.chpl.manager.ApiKeyManager;
import gov.healthit.chpl.registration.APIKeyAuthenticationFilter;

@Configuration
@EnableWebMvc
@EnableTransactionManagement(proxyTargetClass=true)
@EnableWebSecurity
@PropertySource("classpath:/environment.properties")
@ComponentScan(basePackages = {"gov.healthit.chpl.**"})
public class CHPLConfig implements EnvironmentAware {
	
	private static final Logger logger = LogManager.getLogger(CHPLConfig.class);
	
	@Autowired private ApiKeyManager apiKeyManager;
	
	@Autowired private Environment env;
	
	@Override
    public void setEnvironment(final Environment environment) {
		logger.info("setEnvironment");
        this.env = environment;
    }
	
	@Bean
	public org.springframework.orm.jpa.LocalEntityManagerFactoryBean entityManagerFactory(){
		logger.info("get LocalEntityManagerFactoryBean");
		org.springframework.orm.jpa.LocalEntityManagerFactoryBean bean = new org.springframework.orm.jpa.LocalEntityManagerFactoryBean();
		bean.setPersistenceUnitName(env.getRequiredProperty("persistenceUnitName"));
		return bean;
	}
	 
	@Bean
	public org.springframework.orm.jpa.JpaTransactionManager transactionManager(){
		logger.info("get JpaTransactionManager");
		org.springframework.orm.jpa.JpaTransactionManager bean = new org.springframework.orm.jpa.JpaTransactionManager();
		bean.setEntityManagerFactory(entityManagerFactory().getObject());
		return bean;
	}
	
	@Bean
	public org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor persistenceAnnotationBeanPostProcessor(){
		logger.info("get PersistenceAnnotationBeanPostProcessor");
		return new org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor();
	}
	
	@Bean(name="multipartResolver")
	public CommonsMultipartResolver getResolver() throws IOException{
		logger.info("get CommonsMultipartResolver");
	        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
	         
	        //Set the maximum allowed size (in bytes) for each individual file.
	        resolver.setMaxUploadSize(5242880);//5MB
	         
	        //You may also set other available properties.
	        
	        return resolver;
	}
	
	@Bean
	public APIKeyAuthenticationFilter apiKeyAuthenticationFilter()
	{
		logger.info("get APIKeyAuthenticationFilter");
		return new APIKeyAuthenticationFilter(apiKeyManager);
	}
	
	@Bean
	public Marshaller marshaller()
	{
		logger.info("get Marshaller");
		return new CastorMarshaller();
	}
	
	@Bean
	public CacheManager cacheManager() {
		logger.info("get CacheManager");
		return new EhCacheCacheManager(ehCacheCacheManager().getObject());
	}

	@Bean
	public EhCacheManagerFactoryBean ehCacheCacheManager() {
		logger.info("get EhCacheManagerFactoryBean");
		EhCacheManagerFactoryBean cmfb = new EhCacheManagerFactoryBean();
		cmfb.setConfigLocation(new ClassPathResource("ehCache.xml"));
		cmfb.setShared(true);
		return cmfb;
	}
	
}

package gov.healthit.chpl;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
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
@ComponentScan(basePackages = {"gov.healthit.chpl.**"}, excludeFilters = {@ComponentScan.Filter(type = FilterType.ANNOTATION, value = Configuration.class)})
public class CHPLConfig implements EnvironmentAware {
	
	@Autowired private ApiKeyManager apiKeyManager;
	
	@Autowired private Environment env;
	
	@Override
    public void setEnvironment(final Environment environment) {
        this.env = environment;
    }
	
	@Bean
	public org.springframework.orm.jpa.LocalEntityManagerFactoryBean entityManagerFactory(){
		org.springframework.orm.jpa.LocalEntityManagerFactoryBean bean = new org.springframework.orm.jpa.LocalEntityManagerFactoryBean();
		bean.setPersistenceUnitName(env.getRequiredProperty("persistenceUnitName"));
		return bean;
	}
	 
	@Bean
	public org.springframework.orm.jpa.JpaTransactionManager transactionManager(){
		org.springframework.orm.jpa.JpaTransactionManager bean = new org.springframework.orm.jpa.JpaTransactionManager();
		bean.setEntityManagerFactory(entityManagerFactory().getObject());
		return bean;
	}
	
	@Bean
	public org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor persistenceAnnotationBeanPostProcessor(){
		return new org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor();
	}
	
	@Bean(name="multipartResolver")
	public CommonsMultipartResolver getResolver() throws IOException{
	        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
	         
	        //Set the maximum allowed size (in bytes) for each individual file.
	        resolver.setMaxUploadSize(5242880);//5MB
	         
	        //You may also set other available properties.
	        
	        return resolver;
	}
	
	@Bean
	public APIKeyAuthenticationFilter apiKeyAuthenticationFilter()
	{
		return new APIKeyAuthenticationFilter(apiKeyManager);
	}
	
	@Bean
	public Marshaller marshaller()
	{
		return new CastorMarshaller();
	}
}

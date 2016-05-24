package gov.healthit.chpl.app;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.transaction.annotation.EnableTransactionManagement;

//NOTE: WE ONLY NEED THE DAO METHODS FOR THIS 

@Configuration
@EnableTransactionManagement(proxyTargetClass=true)
@ComponentScan(basePackages = {
		"org.springframework.security.**",
		"gov.healthit.chpl.util.**",
		"gov.healthit.chpl.auth.dao.**",
		"gov.healthit.chpl.dao.**", 
		"gov.healthit.chpl.entity.**",
		"gov.healthit.chpl.auth.manager.**",
		"gov.healthit.chpl.manager.**"}, 
	lazyInit=true,
	excludeFilters = {@ComponentScan.Filter(type = FilterType.ANNOTATION, value = Configuration.class)})
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
		if(props == null) {
			try {
				loadProperties();
			} catch (IOException e) {
				e.printStackTrace();
			}		
		}
		return props;
	}
	
	@Bean
	public org.springframework.orm.jpa.LocalEntityManagerFactoryBean entityManagerFactory(){
		
		if (props == null){
			try {
				loadProperties();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		org.springframework.orm.jpa.LocalEntityManagerFactoryBean bean = new org.springframework.orm.jpa.LocalEntityManagerFactoryBean();
		bean.setPersistenceUnitName(props.getProperty("persistenceUnitName"));
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
}

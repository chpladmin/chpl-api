package gov.healthit.chpl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;


@Configuration
@EnableWebSecurity
@ComponentScan(basePackages = {"gov.healthit.chpl.**"}, excludeFilters = {@ComponentScan.Filter(type = FilterType.ANNOTATION, value = Configuration.class)})
public class CHPLTestConfig  {
	
	
	public static final String DEFAULT_PROPERTIES_FILE = "environment.test.properties";
	
	protected Properties props;
	
	protected void loadProperties() throws IOException {
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE);
		
		if (in == null)
		{
			props = null;
			throw new FileNotFoundException("Environment Properties File not found in class path.");
		}
		else
		{
			props = new Properties();
			props.load(in);
		}
	}
	
	
	@Bean
	public DataSource dataSource() {

		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		EmbeddedDatabase db = builder.setType(EmbeddedDatabaseType.DERBY).addScript("db/sql/create-db.sql").addScript("db/sql/insert-data.sql").build();
		return db;

	}
	
	
	@Bean
	public org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean entityManagerFactory(){
		
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

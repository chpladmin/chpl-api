package gov.healthit.chpl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import gov.healthit.chpl.auth.authentication.JWTUserConverter;
import gov.healthit.chpl.auth.filter.JWTAuthenticationFilter;

import org.junit.BeforeClass;
import org.postgresql.ds.PGConnectionPoolDataSource;
import org.postgresql.ds.PGPoolingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.orm.jpa.LocalEntityManagerFactoryBean;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.acls.AclPermissionCacheOptimizer;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.AclAuthorizationStrategyImpl;
import org.springframework.security.acls.domain.ConsoleAuditLogger;
import org.springframework.security.acls.domain.DefaultPermissionGrantingStrategy;
import org.springframework.security.acls.domain.EhCacheBasedAclCache;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;



@Configuration
@EnableWebSecurity
@ComponentScan(basePackages = {"gov.healthit.chpl.**"}, excludeFilters = {@ComponentScan.Filter(type = FilterType.ANNOTATION, value = Configuration.class)})
public class ChplTestConfig extends
		WebSecurityConfigurerAdapter {
	
	
	@Autowired
	private JWTUserConverter userConverter;
	
	public static final String DEFAULT_AUTH_PROPERTIES_FILE = "environment.auth.properties";
	
	protected Properties props;
	
	
	public ChplTestConfig() {
		super(true);
	}
	
	protected void loadProperties() throws IOException {
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(DEFAULT_AUTH_PROPERTIES_FILE);
		
		if (in == null)
		{
			props = null;
			throw new FileNotFoundException("Auth Environment Properties File not found in class path.");
		}
		else
		{
			props = new Properties();
			props.load(in);
		}
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http
				.exceptionHandling().and()
				.anonymous().and()
				.servletApi().and()
				//.headers().cacheControl().and()
				.authorizeRequests()
				.antMatchers("/favicon.ico").permitAll()
				.antMatchers("/resources/**").permitAll()
				
				//defined Admin only API area
				//.antMatchers("/admin/**").hasRole("ADMIN")
				
				//allow anonymous resource requests
				.antMatchers("/").permitAll().and()
				// custom Token based authentication based on the header previously given to the client
				.addFilterBefore(new JWTAuthenticationFilter(userConverter), UsernamePasswordAuthenticationFilter.class)
			.headers().cacheControl();
		
	}
	
	
	@Bean
	public LocalEntityManagerFactoryBean entityManagerFactory(){
		
		LocalEntityManagerFactoryBean bean = new org.springframework.orm.jpa.LocalEntityManagerFactoryBean();
		
		if (this.props == null){
			try {
				this.loadProperties();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		Properties jpaProps = new Properties();
		jpaProps.put("persistenceUnitName", this.props.getProperty("authPersistenceUnitName"));
		
		bean.setJpaProperties(jpaProps);
		
		return bean;
	}
	
	
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder(){
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public UserDetailsChecker userDetailsChecker(){
		return new AccountStatusUserDetailsChecker();
	}
	
	@Bean
	public MappingJackson2HttpMessageConverter jsonConverter(){
		
		MappingJackson2HttpMessageConverter bean = new MappingJackson2HttpMessageConverter();
		
		bean.setPrefixJson(false);
		
		List<MediaType> mediaTypes = new ArrayList<MediaType>();
		mediaTypes.add(MediaType.APPLICATION_JSON);
		
		bean.setSupportedMediaTypes(mediaTypes);
		
		return bean;
	}
	
	/*
	@Bean
	public JndiObjectFactoryBean aclDataSource(){
		
		JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
		
		if (this.props == null){
			try {
				this.loadProperties();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		bean.setJndiName(this.props.getProperty("authJndiName"));
		return bean;
	}
	*/
	
	@Bean
	public EhCacheManagerFactoryBean ehCacheManagerFactoryBean(){
		EhCacheManagerFactoryBean bean = new EhCacheManagerFactoryBean();
		bean.setShared(true);
		return bean;
	}
	
	@Bean
	public EhCacheFactoryBean ehCacheFactoryBean(){
		
		
		if (this.props == null){
			try {
				this.loadProperties();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		EhCacheFactoryBean bean = new EhCacheFactoryBean();
		bean.setCacheManager(ehCacheManagerFactoryBean().getObject());
		//bean.setCacheName("aclCache");
		bean.setCacheName(this.props.getProperty("authAclCacheName"));
		
		return bean;
	}
	
	@Bean
	public ConsoleAuditLogger consoleAuditLogger(){
		ConsoleAuditLogger bean = new ConsoleAuditLogger();
		return bean;
	}
	
	@Bean
	public DefaultPermissionGrantingStrategy defaultPermissionGrantingStrategy(){
		DefaultPermissionGrantingStrategy bean = new DefaultPermissionGrantingStrategy(consoleAuditLogger());
		return bean;
	}
	
	@Bean
	public SimpleGrantedAuthority aclAdminGrantedAuthority(){
		SimpleGrantedAuthority bean = new SimpleGrantedAuthority("ROLE_ACL_ADMIN");
		return bean;
	}
	
	@Bean 
	public AclAuthorizationStrategyImpl aclAuthorizationStrategyImpl(){
		AclAuthorizationStrategyImpl bean = new AclAuthorizationStrategyImpl(aclAdminGrantedAuthority());
		return bean;
	}
	
	@Bean
	public EhCacheBasedAclCache aclCache(){
		
		EhCacheBasedAclCache bean = new EhCacheBasedAclCache(
				ehCacheFactoryBean().getObject(),
				defaultPermissionGrantingStrategy(), 
				aclAuthorizationStrategyImpl());
		return bean;
	}

	
	@Bean
	public SimpleGrantedAuthority roleAdminGrantedAuthority(){
		SimpleGrantedAuthority bean = new SimpleGrantedAuthority("ROLE_ADMINISTRATOR");
		return bean;
	}
	
	
	@Bean 
	public AclAuthorizationStrategyImpl aclAuthorizationStrategyImplAdmin(){
		AclAuthorizationStrategyImpl bean = new AclAuthorizationStrategyImpl(roleAdminGrantedAuthority());
		return bean;
	}
	
	
	@Bean
	public BasicLookupStrategy lookupStrategy() throws Exception{
		
		DataSource datasource = (DataSource) aclDataSource();//.getObject();
		
		BasicLookupStrategy bean = new BasicLookupStrategy(
				datasource,
				aclCache(),
				aclAuthorizationStrategyImplAdmin(),
				consoleAuditLogger());
		return bean;
	}
	
	
	@Bean
	public JdbcMutableAclService mutableAclService() throws Exception{
		
		DataSource datasource = (DataSource) aclDataSource();//.getObject();
		
		JdbcMutableAclService bean = new JdbcMutableAclService(datasource, 
				lookupStrategy(), 
				aclCache());
		
		return bean;
	}
	
	
	@Bean
	public AclPermissionEvaluator permissionEvaluator() throws Exception{
		AclPermissionEvaluator bean = new AclPermissionEvaluator(mutableAclService());
		return bean;
	}
	
	@Bean
	public AclPermissionCacheOptimizer aclPermissionCacheOptimizer() throws Exception{
		AclPermissionCacheOptimizer bean = new AclPermissionCacheOptimizer(mutableAclService());
		return bean;
	}
	
	
	@Bean
	public DefaultMethodSecurityExpressionHandler expressionHandler() throws Exception{
		
		DefaultMethodSecurityExpressionHandler bean = new DefaultMethodSecurityExpressionHandler();
		bean.setPermissionEvaluator(permissionEvaluator());
		bean.setPermissionCacheOptimizer(aclPermissionCacheOptimizer());
		return bean;
	}
	
	
	@Bean
	public PGPoolingDataSource aclDataSource() throws Exception {
		
		PGPoolingDataSource ds = new PGPoolingDataSource();
    	
        try {
            // Create initial context
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.naming.java.javaURLContextFactory");
            System.setProperty(Context.URL_PKG_PREFIXES, 
                "org.apache.naming");            
            InitialContext ic = new InitialContext();

            ic.createSubcontext("java:");
            ic.createSubcontext("java:/comp");
            ic.createSubcontext("java:/comp/env");
            ic.createSubcontext("java:/comp/env/jdbc");
           
            // Construct DataSource
        	
        	ds.setServerName("localhost/openchpl");
            ds.setUser("openchpl");
            ds.setPassword("openchpl1!");
            
            ic.bind("java:/comp/env/jdbc/openchpl", ds);
        } catch (NamingException ex) {
        	ex.printStackTrace();
        }
        return ds;
    }
	
	
}

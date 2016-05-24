package gov.healthit.chpl.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import gov.healthit.chpl.auth.authentication.JWTUserConverter;
import gov.healthit.chpl.auth.filter.JWTAuthenticationFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@PropertySource("classpath:/environment.properties")
@ComponentScan(basePackages = {"gov.healthit.chpl.auth.**"}, excludeFilters = {@ComponentScan.Filter(type = FilterType.ANNOTATION, value = Configuration.class)})
public class CHPLAuthenticationSecurityConfig extends
		WebSecurityConfigurerAdapter implements EnvironmentAware {
	
	@Autowired private JWTUserConverter userConverter;
	private Environment env;
	
	public CHPLAuthenticationSecurityConfig() {
		super(true);
	}
	
	public void setEnvironment(Environment env) {
		this.env = env;
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http
        		.sessionManagement()
        		.sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
		
				.exceptionHandling().and()
				.anonymous().and()
				.servletApi().and()
				//.headers().cacheControl().and()
				.authorizeRequests()
				.antMatchers("/favicon.ico").permitAll()
				.antMatchers("/resources/**").permitAll()
				
				//allow anonymous resource requests
				.antMatchers("/").permitAll().and()
				// custom Token based authentication based on the header previously given to the client
				.addFilterBefore(new JWTAuthenticationFilter(userConverter), UsernamePasswordAuthenticationFilter.class)
			.headers().cacheControl();
		
	}
	
	
	@Bean
	public LocalEntityManagerFactoryBean entityManagerFactory(){
		
		LocalEntityManagerFactoryBean bean = new org.springframework.orm.jpa.LocalEntityManagerFactoryBean();
		Properties jpaProps = new Properties();
		jpaProps.put("persistenceUnitName", this.env.getRequiredProperty("authPersistenceUnitName"));
		
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
	
	@Bean
	public JndiObjectFactoryBean aclDataSource(){
		
		JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
		bean.setJndiName(this.env.getRequiredProperty("authJndiName"));
		return bean;
	}
	
	@Bean
	public EhCacheManagerFactoryBean ehCacheManagerFactoryBean(){
		EhCacheManagerFactoryBean bean = new EhCacheManagerFactoryBean();
		bean.setShared(true);
		return bean;
	}
	
	@Bean
	public EhCacheFactoryBean ehCacheFactoryBean(){
		EhCacheFactoryBean bean = new EhCacheFactoryBean();
		bean.setCacheManager(ehCacheManagerFactoryBean().getObject());
		//bean.setCacheName("aclCache");
		bean.setCacheName(this.env.getRequiredProperty("authAclCacheName"));
		
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
	public BasicLookupStrategy lookupStrategy(){
		
		DataSource datasource = (DataSource) aclDataSource().getObject();
		
		BasicLookupStrategy bean = new BasicLookupStrategy(
				datasource,
				aclCache(),
				aclAuthorizationStrategyImplAdmin(),
				consoleAuditLogger());
		return bean;
	}
	
	
	@Bean
	public JdbcMutableAclService mutableAclService(){
		
		DataSource datasource = (DataSource) aclDataSource().getObject();
		
		JdbcMutableAclService bean = new JdbcMutableAclService(datasource, 
				lookupStrategy(), 
				aclCache());
		
		bean.setClassIdentityQuery("select currval('acl_class_id_seq')");
		bean.setSidIdentityQuery("select currval('acl_sid_id_seq')");
		
		return bean;
	}
	
	
	@Bean
	public AclPermissionEvaluator permissionEvaluator(){
		AclPermissionEvaluator bean = new AclPermissionEvaluator(mutableAclService());
		return bean;
	}
	
	@Bean
	public AclPermissionCacheOptimizer aclPermissionCacheOptimizer(){
		AclPermissionCacheOptimizer bean = new AclPermissionCacheOptimizer(mutableAclService());
		return bean;
	}
	
	
	@Bean
	public DefaultMethodSecurityExpressionHandler expressionHandler(){
		
		DefaultMethodSecurityExpressionHandler bean = new DefaultMethodSecurityExpressionHandler();
		bean.setPermissionEvaluator(permissionEvaluator());
		bean.setPermissionCacheOptimizer(aclPermissionCacheOptimizer());
		return bean;
	}
	
}

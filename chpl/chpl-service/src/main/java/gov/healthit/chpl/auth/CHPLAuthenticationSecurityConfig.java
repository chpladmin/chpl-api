package gov.healthit.chpl.auth;

import java.util.Properties;








import gov.healthit.chpl.auth.authentication.JWTUserConverter;
import gov.healthit.chpl.auth.filter.JWTAuthenticationFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalEntityManagerFactoryBean;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;



@Configuration
@EnableWebSecurity
@ComponentScan(basePackages = {"gov.healthit.chpl.auth.**"}, excludeFilters = {@ComponentScan.Filter(type = FilterType.ANNOTATION, value = Configuration.class)})
public class CHPLAuthenticationSecurityConfig extends
		WebSecurityConfigurerAdapter {
	
	@Autowired
	private JWTUserConverter userConverter;
	
	@Autowired
	private LocalEntityManagerFactoryBean entityManagerFactory;
	
	public CHPLAuthenticationSecurityConfig() {
		super(true);
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
				
				//allow anonymous POSTs to login
				.antMatchers(HttpMethod.POST, "/api/login").permitAll()
				
				//allow anonymous GETs to API
				.antMatchers(HttpMethod.GET, "/api/**").permitAll()
				
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
		
		Properties props = new Properties();
		props.put("persistenceUnitName", "chpl_acl");
		bean.setJpaProperties(props);
		
		return bean;
	}
	
	//<bean id="transactionManager" class="org.springframework.orm.jpa.JpaTransactionManager">
	//<property name="entityManagerFactory" ref="entityManagerFactory" />
	//</bean>
	
	
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder(){
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public UserDetailsChecker userDetailsChecker(){
		return new AccountStatusUserDetailsChecker();
	}
	
	
}

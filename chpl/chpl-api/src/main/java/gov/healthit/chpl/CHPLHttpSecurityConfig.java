package gov.healthit.chpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import gov.healthit.chpl.auth.authentication.JWTUserConverter;
import gov.healthit.chpl.filter.JWTAuthenticationFilter;
import lombok.extern.log4j.Log4j2;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@PropertySources({
    @PropertySource("classpath:/environment.properties"),
    @PropertySource(value = "classpath:/environment-override.properties", ignoreResourceNotFound = true),
    @PropertySource("classpath:/lookup.properties"),
    @PropertySource(value = "classpath:/lookup-override.properties", ignoreResourceNotFound = true),
    @PropertySource("classpath:/email.properties"),
    @PropertySource(value = "classpath:/email-override.properties", ignoreResourceNotFound = true),
})
@ComponentScan(basePackages = { "gov.healthit.chpl.auth.**" }, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ANNOTATION, value = Configuration.class) })
@Log4j2
public class CHPLHttpSecurityConfig extends WebSecurityConfigurerAdapter {
    private static final String FF4J_ROLE = "ff4jUser";

    @Autowired
    private JWTUserConverter userConverter;

    @Value("${ff4j.webconsole.username}")
    private String ff4jUsername;

    @Value("${ff4j.webconsole.password}")
    private String ff4jPassword;

    @Value("${ff4j.webconsole.url}")
    private String ff4jWebConsoleUrl;

    public CHPLHttpSecurityConfig() {
        super(true);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        LOGGER.info("get AuthenticationManager");
        return super.authenticationManagerBean();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
          .withUser(ff4jUsername).password(passwordEncoder().encode(ff4jPassword))
          .roles(FF4J_ROLE);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        LOGGER.info("configure AuthenticationManagerBuilder");
        auth.inMemoryAuthentication()
            .withUser(ff4jUsername).password("{bcrypt}" + passwordEncoder().encode(ff4jPassword)).roles(FF4J_ROLE);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        LOGGER.info("configure HttpSecurity");
        http
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and().exceptionHandling()
            .and().anonymous()
            .and().servletApi()
            .and().authorizeRequests()
                .antMatchers("/favicon.ico").permitAll()
                .antMatchers("/resources/**").permitAll()
                // allow anonymous resource requests
                .antMatchers("/").permitAll()
                .antMatchers(ff4jWebConsoleUrl + "/**").hasRole(FF4J_ROLE).and().httpBasic()
            // custom Token based authentication based on the header previously given to the client
            .and().addFilterBefore(new JWTAuthenticationFilter(userConverter), UsernamePasswordAuthenticationFilter.class)
            .headers().cacheControl();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

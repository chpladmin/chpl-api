package gov.healthit.chpl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import gov.healthit.chpl.auth.authentication.JWTUserConverter;
import gov.healthit.chpl.filter.JWTAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@PropertySource("classpath:/environment.properties")
@ComponentScan(basePackages = { "gov.healthit.chpl.auth.**" }, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ANNOTATION, value = Configuration.class) })
public class CHPLHttpSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger LOGGER = LogManager.getLogger(CHPLHttpSecurityConfig.class);

    @Autowired
    private JWTUserConverter userConverter;

    public CHPLHttpSecurityConfig() {
        super(true);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        LOGGER.info("get AuthenticationManager");
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        LOGGER.info("configure AuthenticationManagerBuilder");
        auth.inMemoryAuthentication().withUser("user").password("password").roles("USER").and().withUser("admin")
        .password("password").roles("USER", "ADMIN");
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        LOGGER.info("configure HttpSecurity");
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()

        .exceptionHandling().and().anonymous().and().servletApi().and()
        // .headers().cacheControl().and()
        .authorizeRequests().antMatchers("/favicon.ico").permitAll().antMatchers("/resources/**").permitAll()

        // allow anonymous resource requests
        .antMatchers("/").permitAll().and()
        // custom Token based authentication based on the header
        // previously given to the client
        .addFilterBefore(new JWTAuthenticationFilter(userConverter), UsernamePasswordAuthenticationFilter.class)
        .headers().cacheControl();

    }
}

package gov.healthit.chpl;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import gov.healthit.chpl.api.ApiKeyManager;
import gov.healthit.chpl.auth.authentication.JWTUserConverter;
import gov.healthit.chpl.filter.APIKeyAuthenticationFilter;
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
@Log4j2
public class CHPLHttpSecurityConfig extends WebSecurityConfigurerAdapter {
    private static final String FF4J_ROLE = "ff4jUser";

    @Configuration
    @Order(1)
    public static class FF4JSecurityConfig extends WebSecurityConfigurerAdapter {
        @Value("${ff4j.webconsole.username}")
        private String ff4jUsername;

        @Value("${ff4j.webconsole.password}")
        private String ff4jPassword;

        @Value("${ff4j.webconsole.url}")
        private String ff4jWebConsoleUrl;

        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
            auth.inMemoryAuthentication()
            .withUser(ff4jUsername).password("{bcrypt}" + passwordEncoder().encode(ff4jPassword)).roles(FF4J_ROLE);
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            LOGGER.info("configure AuthenticationManagerBuilder");
            auth.inMemoryAuthentication()
                .withUser(ff4jUsername).password("{bcrypt}" + passwordEncoder().encode(ff4jPassword)).roles(FF4J_ROLE);
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            LOGGER.info("configure FF4J Security for urls like " + ff4jWebConsoleUrl);
            http
                .csrf().disable()
                .antMatcher(ff4jWebConsoleUrl + "/**")
                .authorizeRequests().anyRequest().hasRole(FF4J_ROLE)
                .and().httpBasic()
                .and().sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        }

        @Bean
        public BCryptPasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }

    @Configuration
    @Order(2)
    public static class JwtSecurityConfig extends WebSecurityConfigurerAdapter {

        public JwtSecurityConfig() {
            super(true);
        }

        @Autowired
        private JWTUserConverter userConverter;

        @Autowired
        private ObjectFactory<ApiKeyManager> apiKeyManagerObjectFactory;

        @Bean
        public APIKeyAuthenticationFilter apiKeyAuthenticationFilter() {
            LOGGER.info("get APIKeyAuthenticationFilter");
            ApiKeyManager apiKeyManager = this.apiKeyManagerObjectFactory.getObject();
            return new APIKeyAuthenticationFilter(apiKeyManager);
        }

        @Bean
        @Override
        public AuthenticationManager authenticationManagerBean() throws Exception {
            LOGGER.info("Get AuthenticationManager");
            return super.authenticationManagerBean();
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            LOGGER.info("Configure AuthenticationManagerBuilder");
            auth.inMemoryAuthentication().withUser("user").password("password").roles("USER").and().withUser("admin")
            .password("password").roles("USER", "ADMIN");
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            LOGGER.info("Configure CHPL Security");
            http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()

            .exceptionHandling().and().anonymous().and().servletApi().and()
            // .headers().cacheControl().and()
            .authorizeRequests().antMatchers("/favicon.ico").permitAll().antMatchers("/resources/**").permitAll()

            // allow anonymous resource requests
            .antMatchers("/").permitAll().and()
            .addFilterBefore(apiKeyAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            // custom Token based authentication based on the header previously given to the client
            .addFilterBefore(new JWTAuthenticationFilter(userConverter), UsernamePasswordAuthenticationFilter.class)
            .headers().cacheControl();
        }
    }
}

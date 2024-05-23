package gov.healthit.chpl;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import gov.healthit.chpl.api.ApiKeyManager;
import gov.healthit.chpl.auth.authentication.JWTUserConverterFacade;
import gov.healthit.chpl.filter.APIKeyAuthenticationFilter;
import gov.healthit.chpl.filter.JWTAuthenticationFilter;
import lombok.extern.log4j.Log4j2;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@PropertySources({
    @PropertySource("classpath:/environment.properties"),
    @PropertySource(value = "classpath:/environment-override.properties", ignoreResourceNotFound = true),
    @PropertySource("classpath:/lookup.properties"),
    @PropertySource(value = "classpath:/lookup-override.properties", ignoreResourceNotFound = true),
    @PropertySource("classpath:/email.properties"),
    @PropertySource(value = "classpath:/email-override.properties", ignoreResourceNotFound = true),
})
@Log4j2
public class CHPLHttpSecurityConfig {
    private static final String FF4J_ROLE = "ff4jUser";

    @Value("${ff4j.webconsole.username}")
    private String ff4jUsername;

    @Value("${ff4j.webconsole.password}")
    private String ff4jPassword;

    @Value("${ff4j.webconsole.url}")
    private String ff4jWebConsoleUrl;

    @Autowired
    private ApiKeyManager apiKeyManager;

    @Autowired
    private JWTUserConverterFacade userConverterFacade;

    @Bean
    @Order(1)
    public SecurityFilterChain configureFf4j(HttpSecurity http) throws Exception {
        LOGGER.info("Configure CHPL Security");
        return http.securityMatcher(new AntPathRequestMatcher(ff4jWebConsoleUrl + "/**"))
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .anyRequest()
                        .hasAnyRole(FF4J_ROLE))
                .httpBasic(withDefaults())
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain configureApi(HttpSecurity http) throws Exception {
        LOGGER.info("Configure CHPL Security");
        return http.securityMatcher(new AntPathRequestMatcher("/**"))
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                       .requestMatchers(new AntPathRequestMatcher("/**"))
                       .permitAll())
                .addFilterBefore(apiKeyAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .headers(header -> header.cacheControl(ctrl -> ctrl.disable()))
                .build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
                .username(ff4jUsername)
                .password("{bcrypt}" + passwordEncoder().encode(ff4jPassword))
                .roles(FF4J_ROLE)
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private APIKeyAuthenticationFilter apiKeyAuthenticationFilter() {
        LOGGER.info("get APIKeyAuthenticationFilter");
        return new APIKeyAuthenticationFilter(apiKeyManager);
    }

    private JWTAuthenticationFilter jwtAuthenticationFilter() {
        LOGGER.info("get JWTAuthenticationFilter");
        return new JWTAuthenticationFilter(userConverterFacade);
    }
}

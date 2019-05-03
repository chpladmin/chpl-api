package gov.healthit.chpl.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import gov.healthit.chpl.auth.authentication.JWTUserConverter;
import gov.healthit.chpl.auth.filter.FF4jAuthenticationFilter;
import gov.healthit.chpl.auth.filter.JWTAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@PropertySource("classpath:/environment.properties")
@ComponentScan(basePackages = {
        "gov.healthit.chpl.auth.**"
}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ANNOTATION, value = Configuration.class)
})
public class CHPLAuthenticationSecurityConfig extends WebSecurityConfigurerAdapter implements EnvironmentAware {

    private static final Logger logger = LogManager.getLogger(CHPLAuthenticationSecurityConfig.class);

    @Autowired
    private JWTUserConverter userConverter;

    private Environment env;

    public CHPLAuthenticationSecurityConfig() {
        super(true);
    }

    @Override
    public void setEnvironment(Environment env) {
        logger.info("setEnvironment");
        this.env = env;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        logger.info("get AuthenticationManager");
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        logger.info("configure AuthenticationManagerBuilder");
        auth.inMemoryAuthentication().withUser("user").password("password").roles("USER").and().withUser("admin")
                .password("password").roles("USER", "ADMIN");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        logger.info("configure HttpSecurity");

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()

                .exceptionHandling().and().anonymous().and().servletApi().and()
                // .headers().cacheControl().and()
                .authorizeRequests().antMatchers("/favicon.ico").permitAll().antMatchers("/resources/**").permitAll()

                // allow anonymous resource requests
                .antMatchers("/").permitAll().and()
                // custom Token based authentication based on the header
                // previously given to the client
                .addFilterBefore(new JWTAuthenticationFilter(userConverter), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new FF4jAuthenticationFilter(), JWTAuthenticationFilter.class).headers().cacheControl();

    }

    @Bean
    public LocalEntityManagerFactoryBean entityManagerFactory() {
        logger.info("Get LocalEntityManagerFactoryBean");
        LocalEntityManagerFactoryBean bean = new org.springframework.orm.jpa.LocalEntityManagerFactoryBean();
        Properties jpaProps = new Properties();
        jpaProps.put("persistenceUnitName", this.env.getRequiredProperty("authPersistenceUnitName"));

        bean.setJpaProperties(jpaProps);

        return bean;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        logger.info("Get BCryptPasswordEncoder");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsChecker userDetailsChecker() {
        logger.info("Get UserDetailsChecker");
        return new AccountStatusUserDetailsChecker();
    }

    @Bean
    public MappingJackson2HttpMessageConverter jsonConverter() {
        logger.info("Get MappingJackson2HttpMessageConverter");
        MappingJackson2HttpMessageConverter bean = new MappingJackson2HttpMessageConverter();

        bean.setPrefixJson(false);

        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.APPLICATION_JSON);

        bean.setSupportedMediaTypes(mediaTypes);

        return bean;
    }

    @Bean
    public JndiObjectFactoryBean aclDataSource() {
        logger.info("Get JndiObjectFactoryBean");
        JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
        bean.setJndiName(System.getProperty("jndi.name"));
        return bean;
    }

    @Bean
    public ConsoleAuditLogger consoleAuditLogger() {
        logger.info("Get ConsoleAuditLogger");
        ConsoleAuditLogger bean = new ConsoleAuditLogger();
        return bean;
    }

    @Bean
    public DefaultPermissionGrantingStrategy defaultPermissionGrantingStrategy() {
        logger.info("Get DefaultPermissionGrantingStrategy");
        DefaultPermissionGrantingStrategy bean = new DefaultPermissionGrantingStrategy(consoleAuditLogger());
        return bean;
    }

    @Bean
    public SimpleGrantedAuthority aclAdminGrantedAuthority() {
        logger.info("Get SimpleGrantedAuthority");
        SimpleGrantedAuthority bean = new SimpleGrantedAuthority("ROLE_ACL_ADMIN");
        return bean;
    }

    @Bean
    public AclAuthorizationStrategyImpl aclAuthorizationStrategyImpl() {
        logger.info("Get AclAuthorizationStrategyImpl");
        AclAuthorizationStrategyImpl bean = new AclAuthorizationStrategyImpl(aclAdminGrantedAuthority());
        return bean;
    }

    @Bean
    public EhCacheManagerFactoryBean ehCacheManagerFactoryBean() {
        logger.info("get EhCacheManagerFactoryBean");
        EhCacheManagerFactoryBean bean = new EhCacheManagerFactoryBean();
        bean.setShared(true);
        return bean;
    }

    @Bean
    public EhCacheFactoryBean ehCacheFactoryBean() {
        logger.info("get EhCacheFactoryBean");
        EhCacheFactoryBean bean = new EhCacheFactoryBean();
        bean.setCacheManager(ehCacheManagerFactoryBean().getObject());
        bean.setCacheName("aclCache");

        return bean;
    }

    @Bean
    public EhCacheBasedAclCache aclCache() {
        logger.info("Get EhCacheBasedAclCache");
        EhCacheBasedAclCache bean = new EhCacheBasedAclCache(ehCacheFactoryBean().getObject(),
                defaultPermissionGrantingStrategy(), aclAuthorizationStrategyImpl());
        return bean;
    }

    @Bean
    public SimpleGrantedAuthority roleAdminGrantedAuthority() {
        logger.info("Get SimpleGrantedAuthority");
        SimpleGrantedAuthority bean = new SimpleGrantedAuthority("ROLE_ADMINISTRATOR");
        return bean;
    }

    @Bean
    public AclAuthorizationStrategyImpl aclAuthorizationStrategyImplAdmin() {
        logger.info("Get AclAuthorizationStrategyImpl");
        AclAuthorizationStrategyImpl bean = new AclAuthorizationStrategyImpl(roleAdminGrantedAuthority());
        return bean;
    }

    @Bean
    public BasicLookupStrategy lookupStrategy() {
        logger.info("Get BasicLookupStrategy");

        DataSource datasource = (DataSource) aclDataSource().getObject();

        BasicLookupStrategy bean = new BasicLookupStrategy(datasource, aclCache(), aclAuthorizationStrategyImplAdmin(),
                consoleAuditLogger());
        return bean;
    }

    @Bean
    public JdbcMutableAclService mutableAclService() {
        logger.info("Get JdbcMutableAclService");

        DataSource datasource = (DataSource) aclDataSource().getObject();

        JdbcMutableAclService bean = new JdbcMutableAclService(datasource, lookupStrategy(), aclCache());

        bean.setClassIdentityQuery("select currval('acl_class_id_seq')");
        bean.setSidIdentityQuery("select currval('acl_sid_id_seq')");

        return bean;
    }

    @Bean
    public AclPermissionEvaluator permissionEvaluator() {
        logger.info("Get AclPermissionEvaluator");
        AclPermissionEvaluator bean = new AclPermissionEvaluator(mutableAclService());
        return bean;
    }

    @Bean
    public AclPermissionCacheOptimizer aclPermissionCacheOptimizer() {
        logger.info("Get AclPermissionCacheOptimizer");
        AclPermissionCacheOptimizer bean = new AclPermissionCacheOptimizer(mutableAclService());
        return bean;
    }

    @Bean
    public DefaultMethodSecurityExpressionHandler expressionHandler() {
        logger.info("Get DefaultMethodSecurityExpressionHandler");
        DefaultMethodSecurityExpressionHandler bean = new DefaultMethodSecurityExpressionHandler();
        bean.setPermissionEvaluator(permissionEvaluator());
        // Commenting this out allows for our custom Postfilter'ing to work
        // bean.setPermissionCacheOptimizer(aclPermissionCacheOptimizer());
        return bean;
    }

    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:/errors.auth");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}

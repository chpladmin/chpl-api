package gov.healthit.chpl.auth;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;
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
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
@ComponentScan(basePackages = {
        "gov.healthit.chpl.auth.**"
}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ANNOTATION, value = Configuration.class)
})
public class CHPLAuthenticationSecurityConfig implements EnvironmentAware {

    private static final Logger LOGGER = LogManager.getLogger(CHPLAuthenticationSecurityConfig.class);

    private Environment env;

    public CHPLAuthenticationSecurityConfig() {
    }

    @Override
    public void setEnvironment(final Environment env) {
        LOGGER.info("setEnvironment");
        this.env = env;
    }

    @Bean
    public LocalEntityManagerFactoryBean entityManagerFactory() {
        LOGGER.info("Get LocalEntityManagerFactoryBean");
        LocalEntityManagerFactoryBean bean = new org.springframework.orm.jpa.LocalEntityManagerFactoryBean();
        Properties jpaProps = new Properties();
        jpaProps.put("persistenceUnitName", this.env.getRequiredProperty("authPersistenceUnitName"));

        bean.setJpaProperties(jpaProps);

        return bean;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        LOGGER.info("Get BCryptPasswordEncoder");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsChecker userDetailsChecker() {
        LOGGER.info("Get UserDetailsChecker");
        return new AccountStatusUserDetailsChecker();
    }

    @Bean
    public JndiObjectFactoryBean aclDataSource() {
        LOGGER.info("Get JndiObjectFactoryBean");
        JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
        bean.setJndiName(System.getProperty("jndi.name"));
        return bean;
    }

    @Bean
    public ConsoleAuditLogger consoleAuditLogger() {
        LOGGER.info("Get ConsoleAuditLogger");
        ConsoleAuditLogger bean = new ConsoleAuditLogger();
        return bean;
    }

    @Bean
    public DefaultPermissionGrantingStrategy defaultPermissionGrantingStrategy() {
        LOGGER.info("Get DefaultPermissionGrantingStrategy");
        DefaultPermissionGrantingStrategy bean = new DefaultPermissionGrantingStrategy(consoleAuditLogger());
        return bean;
    }

    @Bean
    public SimpleGrantedAuthority aclAdminGrantedAuthority() {
        LOGGER.info("Get SimpleGrantedAuthority");
        SimpleGrantedAuthority bean = new SimpleGrantedAuthority("ROLE_ACL_ADMIN");
        return bean;
    }

    @Bean
    public AclAuthorizationStrategyImpl aclAuthorizationStrategyImpl() {
        LOGGER.info("Get AclAuthorizationStrategyImpl");
        AclAuthorizationStrategyImpl bean = new AclAuthorizationStrategyImpl(aclAdminGrantedAuthority());
        return bean;
    }

    @Bean
    public EhCacheManagerFactoryBean ehCacheManagerFactoryBean() {
        LOGGER.info("get EhCacheManagerFactoryBean");
        EhCacheManagerFactoryBean bean = new EhCacheManagerFactoryBean();
        bean.setShared(true);
        return bean;
    }

    @Bean
    public EhCacheFactoryBean ehCacheFactoryBean() {
        LOGGER.info("get EhCacheFactoryBean");
        EhCacheFactoryBean bean = new EhCacheFactoryBean();
        bean.setCacheManager(ehCacheManagerFactoryBean().getObject());
        bean.setCacheName("aclCache");

        return bean;
    }

    @Bean
    public EhCacheBasedAclCache aclCache() {
        LOGGER.info("Get EhCacheBasedAclCache");
        EhCacheBasedAclCache bean = new EhCacheBasedAclCache(ehCacheFactoryBean().getObject(),
                defaultPermissionGrantingStrategy(), aclAuthorizationStrategyImpl());
        return bean;
    }

    @Bean
    public SimpleGrantedAuthority roleAdminGrantedAuthority() {
        LOGGER.info("Get SimpleGrantedAuthority");
        SimpleGrantedAuthority bean = new SimpleGrantedAuthority("ROLE_ADMINISTRATOR");
        return bean;
    }

    @Bean
    public AclAuthorizationStrategyImpl aclAuthorizationStrategyImplAdmin() {
        LOGGER.info("Get AclAuthorizationStrategyImpl");
        AclAuthorizationStrategyImpl bean = new AclAuthorizationStrategyImpl(roleAdminGrantedAuthority());
        return bean;
    }

    @Bean
    public BasicLookupStrategy lookupStrategy() {
        LOGGER.info("Get BasicLookupStrategy");

        DataSource datasource = (DataSource) aclDataSource().getObject();

        BasicLookupStrategy bean = new BasicLookupStrategy(datasource, aclCache(), aclAuthorizationStrategyImplAdmin(),
                consoleAuditLogger());
        return bean;
    }

    @Bean
    public JdbcMutableAclService mutableAclService() {
        LOGGER.info("Get JdbcMutableAclService");

        DataSource datasource = (DataSource) aclDataSource().getObject();

        JdbcMutableAclService bean = new JdbcMutableAclService(datasource, lookupStrategy(), aclCache());

        bean.setClassIdentityQuery("select currval('acl_class_id_seq')");
        bean.setSidIdentityQuery("select currval('acl_sid_id_seq')");

        return bean;
    }

    @Bean
    public AclPermissionEvaluator permissionEvaluator() {
        LOGGER.info("Get AclPermissionEvaluator");
        AclPermissionEvaluator bean = new AclPermissionEvaluator(mutableAclService());
        return bean;
    }

    @Bean
    public AclPermissionCacheOptimizer aclPermissionCacheOptimizer() {
        LOGGER.info("Get AclPermissionCacheOptimizer");
        AclPermissionCacheOptimizer bean = new AclPermissionCacheOptimizer(mutableAclService());
        return bean;
    }

    @Bean
    public DefaultMethodSecurityExpressionHandler expressionHandler() {
        LOGGER.info("Get DefaultMethodSecurityExpressionHandler");
        DefaultMethodSecurityExpressionHandler bean = new DefaultMethodSecurityExpressionHandler();
        bean.setPermissionEvaluator(permissionEvaluator());
        // Commenting this out allows for our custom Postfilter'ing to work
        // bean.setPermissionCacheOptimizer(aclPermissionCacheOptimizer());
        return bean;
    }
}

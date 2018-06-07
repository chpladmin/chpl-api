package gov.healthit.chpl.scheduler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@ComponentScan(basePackages = {
        "org.springframework.security.**", "org.springframework.core.env.**", "gov.healthit.chpl.util.**",
        "gov.healthit.chpl.auth.**", "gov.healthit.chpl.dao.**", "gov.healthit.chpl.entity.**",
        "gov.healthit.chpl.auth.manager.**", "gov.healthit.chpl.manager.**", "gov.healthit.chpl.upload.**",
        "gov.healthit.chpl.validation.**", "gov.healthit.chpl.app.**"
}, lazyInit = true, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ANNOTATION, value = Configuration.class)
})
public class AppConfig {

    public static final String DEFAULT_PROPERTIES_FILE = "environment.properties";

    protected Properties props;

    protected void loadProperties() throws IOException {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE);

        if (in == null) {
            props = null;
            throw new FileNotFoundException("Environment Properties File not found in class path.");
        } else {
            props = new Properties();
            props.load(in);
            in.close();
        }
    }

    @Bean
    public Properties properties() {
        if (props == null) {
            try {
                loadProperties();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return props;
    }
}

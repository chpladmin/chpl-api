package gov.healthit.chpl;

import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.ff4j.FF4j;
import org.ff4j.audit.repository.JdbcEventRepository;
import org.ff4j.property.store.JdbcPropertyStore;
import org.ff4j.store.JdbcFeatureStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FF4JConfiguration {

    @Bean
    public FF4j getFF4j() {
        try {
            FF4j ff4j = new FF4j();
            ff4j.setFeatureStore(new JdbcFeatureStore(ff4jDataSource()));
            ff4j.setPropertiesStore(new JdbcPropertyStore(ff4jDataSource()));
            ff4j.setEventRepository(new JdbcEventRepository(ff4jDataSource()));
            return ff4j;
        } catch (NamingException | SQLException e) {
            return null;
        }
    }

    private DataSource ff4jDataSource() throws NamingException, SQLException {
        Context ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup("java:/comp/env/jdbc/openchpl");
        ds.getConnection().setSchema("openchpl");
        return (DataSource) ds;
    }

}

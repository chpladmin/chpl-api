package gov.healthit.chpl;

import java.sql.Connection;
import java.sql.Statement;

import javax.naming.InitialContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.datasource.DataSourceUtils;

@WebListener
public class ChplStartupListener implements ServletContextListener {
    private static final Logger LOGGER = LogManager.getLogger(ChplStartupListener.class);
    
    @Override
    public void contextInitialized(ServletContextEvent event) {
        System.out.println("---- initialize servlet context -----");
        // add initialization code here
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        System.out.println("---- destroying servlet context -----");
        String sql = "SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE pg_stat_activity.application_name = 'MyApp' AND pid <> pg_backend_pid();";
        
        try {
            InitialContext cxt = new InitialContext();
            DataSource ds = (DataSource) cxt.lookup( "java:/comp/env/jdbc/openchpl" );
            Connection cn = DataSourceUtils.getConnection(ds);
            Statement stmt = cn.createStatement();
            stmt.execute(sql);
        } catch (Exception e) {
            //LOGGER.error(e);
            //eat it??
        }
//        
    }
}
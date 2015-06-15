package gov.healthit.chpl.auth.jwt;

import static org.junit.Assert.assertNotNull;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.postgresql.ds.PGConnectionPoolDataSource;
import org.postgresql.ds.PGPoolingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration(classes = { gov.healthit.chpl.auth.CHPLAuthenticationSecurityTestConfig.class })
//@ContextConfiguration(classes = { gov.healthit.chpl.auth.CHPLAuthenticationSecurityConfig.class })
public class JWTConsumerRsaJoseJImplTest {
	
	@Autowired
	JWTConsumer consumer;
	
	@Test
	public void consumerIsNotNull(){
		assertNotNull(consumer);
	}
	
	
    @BeforeClass
    public static void setUpClass() throws Exception {
        // rcarver - setup the jndi context and the datasource
        try {
            // Create initial context
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.naming.java.javaURLContextFactory");
            System.setProperty(Context.URL_PKG_PREFIXES, 
                "org.apache.naming");            
            InitialContext ic = new InitialContext();

            ic.createSubcontext("java:");
            ic.createSubcontext("java:/comp");
            ic.createSubcontext("java:/comp/env");
            ic.createSubcontext("java:/comp/env/jdbc");
           
            // Construct DataSource
            PGPoolingDataSource ds = new PGPoolingDataSource();
        	ds.setServerName("jdbc:postgresql://localhost/chpl_acl");
            
            //ds.setURL("jdbc:oracle:thin:@localhost:5432:chpl_acl");
            ds.setUser("chpl_acl");
            ds.setPassword("Audac1ous");
            
            ic.bind("java:/comp/env/jdbc/chpl_acl", ds);
        } catch (NamingException ex) {
            //Logger.getLogger(MyDAOTest.class.getName()).log(Level.SEVERE, null, ex);
        	ex.printStackTrace();
        }
        
    }
	

	
	
	/*
    @BeforeClass
    public static void setUpClass() throws Exception {
        // rcarver - setup the jndi context and the datasource
        try {
            // Create initial context
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.naming.java.javaURLContextFactory");
            System.setProperty(Context.URL_PKG_PREFIXES, 
                "org.apache.naming");            
            InitialContext ic = new InitialContext();

            ic.createSubcontext("java:");
            ic.createSubcontext("java:/comp");
            ic.createSubcontext("java:/comp/env");
            ic.createSubcontext("java:/comp/env/jdbc");
           
            // Construct DataSource
        	PGConnectionPoolDataSource ds = new PGConnectionPoolDataSource();
        	ds.setServerName("jdbc:postgresql://localhost/chpl_acl");
            
            //ds.setURL("jdbc:oracle:thin:@localhost:5432:chpl_acl");
            ds.setUser("chpl_acl");
            ds.setPassword("Audac1ous");
            
            ic.bind("java:/comp/env/jdbc/chpl_acl", ds);
        } catch (NamingException ex) {
            //Logger.getLogger(MyDAOTest.class.getName()).log(Level.SEVERE, null, ex);
        	ex.printStackTrace();
        }
        
    }
	*/


}

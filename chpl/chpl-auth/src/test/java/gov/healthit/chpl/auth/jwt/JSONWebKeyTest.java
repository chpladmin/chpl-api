package gov.healthit.chpl.auth.jwt;


import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration(classes = { gov.healthit.chpl.auth.CHPLAuthenticationSecurityTestConfig.class })
public class JSONWebKeyTest {
	
	
	public static final String DEFAULT_AUTH_PROPERTIES_FILE = "environment.auth.test.properties";
	
	protected Properties props;
	
	protected void loadProperties() throws IOException {
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(DEFAULT_AUTH_PROPERTIES_FILE);
		
		if (in == null)
		{
			props = null;
			throw new FileNotFoundException("Auth Environment Test Properties File not found in class path.");
		}
		else
		{
			props = new Properties();
			props.load(in);
		}
	}
	
	protected Properties getProps(){
		
		if (this.props == null){
			try {
				this.loadProperties();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return this.props;
	}
	
	private JSONWebKey jsonWebKey = new JSONWebKeyRsaJoseJImpl();
	
	@Test
	public void testSaveKey() throws ClassNotFoundException, IOException{
		
		jsonWebKey.saveKey(this.getProps().getProperty("keyLocation"));
		PublicKey originalPublicKey = jsonWebKey.getPublicKey();
		PrivateKey originalPrivateKey = jsonWebKey.getPrivateKey();
		
		jsonWebKey.loadSavedKey(this.getProps().getProperty("keyLocation"));
		assertEquals(originalPublicKey, jsonWebKey.getPublicKey());
		assertEquals(originalPrivateKey, jsonWebKey.getPrivateKey());
		
	}
	
	@Test
	public void testLoadSavedKey() throws ClassNotFoundException, IOException{
		
		jsonWebKey.saveKey(this.getProps().getProperty("keyLocation"));
		PublicKey originalPublicKey = jsonWebKey.getPublicKey();
		PrivateKey originalPrivateKey = jsonWebKey.getPrivateKey();
		
		jsonWebKey.loadSavedKey(this.getProps().getProperty("keyLocation"));
		assertEquals(originalPublicKey, jsonWebKey.getPublicKey());
		assertEquals(originalPrivateKey, jsonWebKey.getPrivateKey());
		
	}
	
	@Test
	public void testCreateOrLoadKey(){
		
		jsonWebKey.createOrLoadKey();
		
	}
	
}

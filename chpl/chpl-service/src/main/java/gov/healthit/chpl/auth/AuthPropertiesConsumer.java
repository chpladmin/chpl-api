package gov.healthit.chpl.auth;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AuthPropertiesConsumer {

	public static final String DEFAULT_AUTH_PROPERTIES_FILE = "environment.auth.properties";
	
	protected Properties props;
	
	protected void loadProperties() throws IOException {
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(DEFAULT_AUTH_PROPERTIES_FILE);
		
		if (in == null)
		{
			props = null;
			throw new FileNotFoundException("Auth Environment Properties File not found in class path.");
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
	
}
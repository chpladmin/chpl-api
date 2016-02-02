package gov.healthit.chpl.app;
import javax.naming.spi.NamingManager;

public class LocalContextFactory {
	/**
	 * do not instantiate this class directly. Use the factory method.
	 */
	private LocalContextFactory() {}
	
	public static LocalContext createLocalContext(String databaseDriver) throws Exception {

		try { 
			LocalContext ctx = new LocalContext();
			Class.forName(databaseDriver);	
			NamingManager.setInitialContextFactoryBuilder(ctx); 			
			return ctx;
		}
		catch(Exception e) {
			throw new Exception("Error Initializing Context: " + e.getMessage(),e);
		}
	}	
}

package gov.healthit.chpl.app;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;

class LocalContext extends InitialContext implements InitialContextFactoryBuilder, InitialContextFactory {

	Map<Object,Object> dataSources;
	
	LocalContext() throws NamingException {
		super();
		dataSources = new HashMap<Object,Object>();
	}
	
	public void addDataSource(String name, String connectionString, String username, String password) {
		this.
		dataSources.put(name, new LocalDataSource(connectionString,username,password));
	}

	public InitialContextFactory createInitialContextFactory(
			Hashtable<?, ?> hsh) throws NamingException {
		dataSources.putAll(hsh);
		return this;
	}

	public Context getInitialContext(Hashtable<?, ?> arg0)
			throws NamingException {
		return this;
	}

	@Override
	public Object lookup(String name) throws NamingException {
		Object ret = dataSources.get(name);
		return (ret != null) ? ret : super.lookup(name);
	}	
}
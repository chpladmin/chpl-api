package gov.healthit.chpl.scheduler;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;

public class LocalContext extends InitialContext implements InitialContextFactoryBuilder, InitialContextFactory {

    Map<Object, Object> dataSources;

    LocalContext() throws NamingException {
        super();
        dataSources = new HashMap<Object, Object>();
    }

    public void addDataSource(final String name, final String connectionString,
            final String username, final String password) {
        this.dataSources.put(name, new LocalDataSource(connectionString, username, password));
    }

    public InitialContextFactory createInitialContextFactory(final Hashtable<?, ?> hsh) throws NamingException {
        dataSources.putAll(hsh);
        return this;
    }

    public Context getInitialContext(final Hashtable<?, ?> arg0) throws NamingException {
        return this;
    }

    @Override
    public Object lookup(final String name) throws NamingException {
        Object ret = dataSources.get(name);
        return (ret != null) ? ret : super.lookup(name);
    }
}

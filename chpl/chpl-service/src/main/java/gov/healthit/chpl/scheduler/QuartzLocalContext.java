package gov.healthit.chpl.scheduler;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;

/**
 * Quartz local context builder.
 * @author alarned
 *
 */
public class QuartzLocalContext extends InitialContext implements InitialContextFactoryBuilder, InitialContextFactory {

    private Map<Object, Object> dataSources;

    QuartzLocalContext() throws NamingException {
        super();
        dataSources = new HashMap<Object, Object>();
    }

    /**
     * Add a data source to the local context.
     * @param name name of the data source
     * @param connectionString database connection string
     * @param username database user name
     * @param password database user password
     */
    public void addDataSource(final String name, final String connectionString,
            final String username, final String password) {
        this.dataSources.put(name, new QuartzLocalDataSource(connectionString, username, password));
    }

    /**
     * Create the initial context factory.
     * @param hsh a hash table
     * @throws NamingException if naming goes wrong
     * @return the factory
     */
    public InitialContextFactory createInitialContextFactory(final Hashtable<?, ?> hsh) throws NamingException {
        dataSources.putAll(hsh);
        return this;
    }

    /**
     * Get the initial context.
     * @param arg0 a hash table
     * @throws NamingException if naming goes wrong
     * @return the context
     */
    public Context getInitialContext(final Hashtable<?, ?> arg0) throws NamingException {
        return this;
    }

    @Override
    public Object lookup(final String name) throws NamingException {
        Object ret = dataSources.get(name);
        return (ret != null) ? ret : super.lookup(name);
    }
}

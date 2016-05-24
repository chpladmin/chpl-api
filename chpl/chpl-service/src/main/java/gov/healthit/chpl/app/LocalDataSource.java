package gov.healthit.chpl.app;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

class LocalDataSource implements DataSource , Serializable {
	
	private String connectionString;
    private String username;
    private String password;
    
    LocalDataSource(String connectionString, String username, String password) {
        this.connectionString = connectionString;
        this.username = username;
        this.password = password;
    }
    
    public Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(connectionString, username, password);
    }

	public Connection getConnection(String username, String password)
			throws SQLException {return null;}
	public PrintWriter getLogWriter() throws SQLException {return null;}
	public int getLoginTimeout() throws SQLException {return 0;}
	public void setLogWriter(PrintWriter out) throws SQLException {	}
	public void setLoginTimeout(int seconds) throws SQLException {}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}
}

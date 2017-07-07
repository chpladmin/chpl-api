package gov.healthit.chpl.entity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;

public class PostgresValidationMessageType extends PostgresEnumType {
	
	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
	    String name = rs.getString(names[0]);
	    if(rs.wasNull()) {
	    	return null;
	    }
	    return ValidationMessageType.getValue(name);
	}
}

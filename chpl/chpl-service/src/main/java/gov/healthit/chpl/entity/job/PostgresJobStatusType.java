package gov.healthit.chpl.entity.job;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.HibernateException;

import gov.healthit.chpl.entity.PostgresEnumType;

public class PostgresJobStatusType extends PostgresEnumType {

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
	    String name = rs.getString(names[0]);
	    if(rs.wasNull()) {
	    	return null;
	    }
	    return JobStatusType.getValue(name);
	    //return rs.wasNull() ? null: Enum.valueOf(enumClass,name);
	}
}

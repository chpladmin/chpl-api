package gov.healthit.chpl.entity;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

public class PostgresValidationMessageType extends PostgresEnumType {

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        String name = rs.getString(names[0]);
        if (rs.wasNull()) {
            return null;
        }
        return ValidationMessageType.getValue(name);
    }

    @Override
    public int getSqlType() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Object nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner)
            throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }
}

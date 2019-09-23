package gov.healthit.chpl.entity;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

public abstract class PostgresEnumType implements UserType, ParameterizedType {
    private Class<Enum> enumClass;

    public PostgresEnumType() {
        super();
    }

    public abstract Object nullSafeGet(ResultSet rs, String[] names, Object owner)
            throws HibernateException, SQLException;

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
            throws HibernateException, SQLException {
        return nullSafeGet(rs, names, owner);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session)
            throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            // previously used setString, but this causes postgresql to bark
            // about incompatible types.
            // now using setObject passing in the java type for the postgres
            // enum object
            // st.setString(index,((Enum) value).name());
            st.setObject(index, ((Enum) value), Types.OTHER);
        }
    }

    public void setParameterValues(final Properties parameters) {
        String enumClassName = parameters.getProperty("enumClassName");
        try {
            enumClass = (Class<Enum>) Class.forName(enumClassName);
        } catch (final ClassNotFoundException e) {
            throw new HibernateException("Enum class not found ", e);
        }

    }

    public int[] sqlTypes() {
        return new int[] {
                Types.VARCHAR
        };
    }

    public Class returnedClass() {
        return enumClass;
    }

    public boolean equals(Object x, Object y) throws HibernateException {
        return x == y;
    }

    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    public boolean isMutable() {
        return false; // To change body of implemented methods use File |
                      // Settings | File Templates.
    }

    public Serializable disassemble(Object value) throws HibernateException {
        return (Enum) value;
    }

    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

    public Object fromXMLString(String xmlValue) {
        return Enum.valueOf(enumClass, xmlValue);
    }

    public String objectToSQLString(Object value) {
        return '\'' + ((Enum) value).name() + '\'';
    }

    public String toXMLString(Object value) {
        return ((Enum) value).name();
    }
}

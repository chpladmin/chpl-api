package gov.healthit.chpl.upload.listing;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.HibernateException;

import gov.healthit.chpl.entity.PostgresEnumType;

public class PostgresListingUploadStatus extends PostgresEnumType {

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        String name = rs.getString(names[0]);
        if (rs.wasNull()) {
            return null;
        }
        return ListingUploadStatus.getValue(name);
    }
}

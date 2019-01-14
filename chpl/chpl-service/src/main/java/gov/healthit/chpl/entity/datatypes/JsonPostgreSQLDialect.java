package gov.healthit.chpl.entity.datatypes;

import java.sql.Types;

import org.hibernate.dialect.PostgreSQLDialect;

public class JsonPostgreSQLDialect extends PostgreSQLDialect {
    public JsonPostgreSQLDialect() {
        super();
        this.registerColumnType(Types.JAVA_OBJECT, "json");
    }
}

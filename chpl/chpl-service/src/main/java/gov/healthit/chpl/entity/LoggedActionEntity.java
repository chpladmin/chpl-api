package gov.healthit.chpl.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "audit.logged_actions")
@Data
public class LoggedActionEntity {

    @Column(name = "schema_name")
    private String schemaName;

    @Column(name = "table_name")
    private String tableName;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "action_tstamp")
    private Date actionTStamp;

    @Column(name = "action")
    private String action;

    @Column(name = "original_data")
    private String originalData;

    @Column(name = "new_data")
    private String newData;

    @Column(name = "query")
    private String query;
}

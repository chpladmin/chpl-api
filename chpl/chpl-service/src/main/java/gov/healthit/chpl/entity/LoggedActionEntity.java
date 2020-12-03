package gov.healthit.chpl.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "audit.logged_actions")
@Data
public class LoggedActionEntity implements Serializable {
    private static final long serialVersionUID = -7980143499756233253L;

    @Id
    @Column(name = "schema_name")
    private String schemaName;

    @Id
    @Column(name = "table_name")
    private String tableName;

    @Id
    @Column(name = "user_name")
    private String userName;

    @Id
    @Column(name = "action_tstamp")
    private Date actionTStamp;

    @Id
    @Column(name = "action")
    private String action;

    @Id
    @Column(name = "original_data")
    private String originalData;

    @Id
    @Column(name = "new_data")
    private String newData;

    @Id
    @Column(name = "query")
    private String query;
}

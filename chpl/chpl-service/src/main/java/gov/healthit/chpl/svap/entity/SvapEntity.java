package gov.healthit.chpl.svap.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "svap")
@Data
public class SvapEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long svaplId;

    @Column(name = "regulatory_text_citation", nullable = false)
    private String regulatoryTextCitation;

    @Column(name = "approved_standard_version", nullable = false)
    private String approvedStandardVersion;

    @Column(name = "deleted", nullable = false)
    protected Boolean deleted;

    @Column(name = "last_modified_user", nullable = false)
    protected Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

}

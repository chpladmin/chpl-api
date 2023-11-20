    package gov.healthit.chpl.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import gov.healthit.chpl.entity.lastmodifieduserstrategy.CurrentUserStrategy;
import gov.healthit.chpl.entity.lastmodifieduserstrategy.LastModifiedUserStrategy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@MappedSuperclass
public abstract class EntityAudit implements Serializable {
    private static final long serialVersionUID = 4636811643343798730L;

    public LastModifiedUserStrategy getLastModifiedUserStrategy() {
        return new CurrentUserStrategy();
    }

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(nullable = false)
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = true)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = true)
    @Column(name = "last_modified_sso_user", nullable = false)
    private UUID lastModifiedSsoUser;

    @PrePersist
    public void populateAuditInformationBeforeInsert() {
        this.creationDate = new Date();
        this.lastModifiedDate = new Date();
        getLastModifiedUserStrategy().populationLastModifiedUser(this);
    }

    @PreUpdate
    public void populateAuditInformationBeforeUpdate() {
        this.lastModifiedDate = new Date();
        getLastModifiedUserStrategy().populationLastModifiedUser(this);
    }
}

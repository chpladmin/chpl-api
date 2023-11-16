    package gov.healthit.chpl.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

import org.ff4j.FF4j;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.SpringContext;
import gov.healthit.chpl.auth.user.CognitoSystemUsers;
import gov.healthit.chpl.auth.user.SystemUsers;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.util.AuthUtil;
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

    @Transient
    private Boolean useSystemUserIdForAuditUser = false;

    @Transient
    private Boolean useAdminUserIdForAuditUser = false;

    @Transient
    private Boolean useDefaulUserIdForAuditUser = false;

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
        setLastModifiedUser();
    }

    @PreUpdate
    public void populateAuditInformationBeforeUpdate() {
        this.lastModifiedDate = new Date();
        setLastModifiedUser();
    }

    private void setLastModifiedUser() {
        boolean isSsoFeatueOn = isSsoFeatureOn();

        //TODO - OCD-4333 Does this work with impersonated users???
        User user = AuthUtil.getCurrentUser();

        if (isSsoFeatueOn) {
            lastModifiedUser = null;
            if (useSystemUserIdForAuditUser) {
                lastModifiedSsoUser = CognitoSystemUsers.SYSTEM_USER_ID;
            } else if (useAdminUserIdForAuditUser) {
                lastModifiedSsoUser = CognitoSystemUsers.ADMIN_USER_ID;
            } else if (useDefaulUserIdForAuditUser) {
                lastModifiedSsoUser = CognitoSystemUsers.DEFAULT_USER_ID;
            } else if (user.getSsoId() != null) {
                lastModifiedSsoUser = user.getSsoId();
            } else {
                throw new RuntimeException("Could not determine the Audit User.");
            }
        } else {
            lastModifiedSsoUser = null;
            if (useSystemUserIdForAuditUser) {
                lastModifiedUser = SystemUsers.SYSTEM_USER_ID;
            } else if (useAdminUserIdForAuditUser) {
                lastModifiedUser = SystemUsers.ADMIN_USER_ID;
            } else if (useDefaulUserIdForAuditUser) {
                lastModifiedUser = SystemUsers.DEFAULT_USER_ID;
            } else if (user.getId() != null) {
                lastModifiedUser = user.getId();
            } else {
                throw new RuntimeException("Could not determine the Audit User.");
            }
        }
    }

    private boolean isSsoFeatureOn() {
        FF4j ff4j = SpringContext.getBean(FF4j.class);
        return ff4j.check(FeatureList.SSO);
    }
}

package gov.healthit.chpl.entity.auth;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import gov.healthit.chpl.domain.auth.UserPermission;
import lombok.Data;

@Data
@Entity
@Table(name = "user_permission")
@SQLDelete(sql = "UPDATE user_permission SET deleted = true WHERE user_permission_id = ?")
@Where(clause = "NOT deleted")
public class UserPermissionEntity {
    @Id
    @Column(name = "user_permission_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "authority", unique = true)
    private String authority;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    public UserPermission toDomain() {
        return UserPermission.builder()
                .id(this.getId())
                .name(this.getName())
                .description(this.getDescription())
                .authority(this.getAuthority())
                .build();
    }
}

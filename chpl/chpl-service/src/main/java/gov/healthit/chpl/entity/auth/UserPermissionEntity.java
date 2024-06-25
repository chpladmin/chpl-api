package gov.healthit.chpl.entity.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import gov.healthit.chpl.domain.auth.UserPermission;
import gov.healthit.chpl.entity.EntityAudit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_permission")
public class UserPermissionEntity extends EntityAudit {
    private static final long serialVersionUID = -621965774938575157L;

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

    public UserPermission toDomain() {
        return UserPermission.builder()
                .id(this.getId())
                .name(this.getName())
                .description(this.getDescription())
                .authority(this.getAuthority())
                .build();
    }
}

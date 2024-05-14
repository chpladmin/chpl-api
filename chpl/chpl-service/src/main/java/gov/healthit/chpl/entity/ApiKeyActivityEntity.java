package gov.healthit.chpl.entity;

import gov.healthit.chpl.entity.lastmodifieduserstrategy.CurrentUserThenSystemUserStrategy;
import gov.healthit.chpl.entity.lastmodifieduserstrategy.LastModifiedUserStrategy;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "api_key_activity")
public class ApiKeyActivityEntity extends EntityAudit {
    private static final long serialVersionUID = 1590293931196081305L;

    @Override
    public LastModifiedUserStrategy getLastModifiedUserStrategy() {
        return new CurrentUserThenSystemUserStrategy();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "api_key_activity_id", nullable = false)
    private Long id;

    @Basic(optional = false)

    @Column(name = "api_key_id")
    private Long apiKeyId;

    @Basic(optional = false)
    @Column(name = "api_call_path")
    private String apiCallPath;

    @Basic(optional = false)
    @Column(name = "api_call_method")
    private String apiCallMethod;

}

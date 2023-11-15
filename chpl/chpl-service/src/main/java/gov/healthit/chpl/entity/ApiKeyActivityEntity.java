package gov.healthit.chpl.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
@Table(name = "openchpl.api_key_activity")
public class ApiKeyActivityEntity extends EntityAudit {
    private static final long serialVersionUID = 1590293931196081305L;

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

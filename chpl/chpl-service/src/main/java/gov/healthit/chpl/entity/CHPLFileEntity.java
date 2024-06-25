package gov.healthit.chpl.entity;

import java.util.Date;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import gov.healthit.chpl.entity.lastmodifieduserstrategy.CurrentUserThenSystemUserStrategy;
import gov.healthit.chpl.entity.lastmodifieduserstrategy.LastModifiedUserStrategy;
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
@Table(name = "chpl_file")
public class CHPLFileEntity extends EntityAudit {
    private static final long serialVersionUID = -6492436561440937066L;

    @Override
    public LastModifiedUserStrategy getLastModifiedUserStrategy() {
        return new CurrentUserThenSystemUserStrategy();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "chpl_file_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "file_type_id", nullable = false, insertable = true,
            updatable = false)
    private FileTypeEntity fileType;

    @Basic(optional = false)
    @Column(name = "file_data")
    private byte[] fileData;

    @Basic(optional = true)
    @Column(name = "file_name", nullable = true)
    private String fileName;

    @Basic(optional = true)
    @Column(name = "content_type", nullable = true)
    private String contentType;

    @Basic(optional = true)
    @Column(name = "associated_date", nullable = true)
    private Date associatedDate;
}

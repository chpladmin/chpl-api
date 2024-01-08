package gov.healthit.chpl.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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

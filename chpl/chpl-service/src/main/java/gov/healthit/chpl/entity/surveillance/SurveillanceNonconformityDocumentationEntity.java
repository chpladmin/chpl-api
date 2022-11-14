package gov.healthit.chpl.entity.surveillance;

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

import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformityDocument;
import lombok.Data;

@Entity
@Table(name = "surveillance_nonconformity_document")
@Data
public class SurveillanceNonconformityDocumentationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "surveillance_nonconformity_id")
    private Long nonconformityId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "surveillance_nonconformity_id", insertable = false, updatable = false)
    private SurveillanceNonconformityEntity nonconformityEntity;

    @Column(name = "filename")
    private String fileName;

    @Column(name = "filetype")
    private String fileType;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "filedata")
    private byte[] fileData;

    @Column(name = "creation_date", nullable = false, insertable = false, updatable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false, insertable = false, updatable = false)
    private Date lastModifiedDate;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(nullable = false)
    private Boolean deleted;

    public SurveillanceNonconformityDocument toDomain() {
        SurveillanceNonconformityDocument doc = new SurveillanceNonconformityDocument();
        doc.setId(this.getId());
        doc.setFileType(this.getFileType());
        doc.setFileName(this.getFileName());
        doc.setFileContents(this.getFileData());
        return doc;
    }

}

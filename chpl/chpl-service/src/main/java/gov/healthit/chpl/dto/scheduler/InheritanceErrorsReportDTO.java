package gov.healthit.chpl.dto.scheduler;

import java.io.Serializable;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.entity.scheduler.InheritanceErrorsReportEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InheritanceErrorsReportDTO implements Serializable {

    private static final long serialVersionUID = -1536844909545189801L;

    private Long id;
    private String chplProductNumber;
    private String developer;
    private String product;
    private String version;
    private CertificationBodyDTO certificationBody;
    private String url;
    private String reason;
    private Boolean deleted;


    public InheritanceErrorsReportDTO(InheritanceErrorsReportEntity entity) {
        this.id = entity.getId();
        this.chplProductNumber = entity.getChplProductNumber();
        this.developer = entity.getDeveloper();
        this.product = entity.getProduct();
        this.version = entity.getVersion();
        this.certificationBody = new CertificationBodyDTO(entity.getCertificationBody());
        this.url = entity.getUrl();
        this.reason = entity.getReason();
        this.deleted = entity.getDeleted();
    }

}

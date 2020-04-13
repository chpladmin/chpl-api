package gov.healthit.chpl.dto.surveillance.report;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntity;
import gov.healthit.chpl.entity.listing.ListingWithPrivilegedSurveillanceEntity;
import gov.healthit.chpl.entity.surveillance.report.PrivilegedSurveillanceEntity;
import lombok.Data;
import lombok.Singular;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuarterlyReportRelevantListingDTO extends CertifiedProductDetailsDTO {
    private static final long serialVersionUID = -2198910382314894675L;
    private QuarterlyReportDTO quarterlyReport;
    private boolean isExcluded;
    private String exclusionReason;
    @Singular
    private List<PrivilegedSurveillanceDTO> surveillances;

    public QuarterlyReportRelevantListingDTO() {
        super();
        this.surveillances = new ArrayList<>();
    }

    public QuarterlyReportRelevantListingDTO(final CertifiedProductDetailsEntity entity) {
        super(entity);
        this.surveillances = new ArrayList<>();
    }

    public QuarterlyReportRelevantListingDTO(final ListingWithPrivilegedSurveillanceEntity entity) {
        this.setId(entity.getId());
        this.setChplProductNumber(entity.getChplProductNumber());
        DeveloperDTO developer = new DeveloperDTO();
        developer.setId(entity.getDeveloperId());
        developer.setName(entity.getDeveloperName());
        this.setDeveloper(developer);
        ProductDTO product = new ProductDTO();
        product.setId(entity.getProductId());
        product.setName(entity.getProductName());
        this.setProduct(product);
        ProductVersionDTO version = new ProductVersionDTO();
        version.setId(entity.getProductVersionId());
        version.setVersion(entity.getProductVersion());
        this.setVersion(version);
        this.setCertificationBodyId(entity.getCertificationBodyId());
        this.setCertificationBodyName(entity.getCertificationBodyName());
        this.setCertificationBodyCode(entity.getCertificationBodyCode());
        this.setCertificationEditionId(entity.getCertificationEditionId());
        this.setCuresUpdate(entity.getCuresUpdate());
        this.setYear(entity.getYear());
        this.setCertificationStatusId(entity.getCertificationStatusId());
        this.setCertificationStatusName(entity.getCertificationStatusName());
        this.setCertificationDate(entity.getCertificationDate());
        this.setLastModifiedDate(entity.getLastModifiedDate());
        this.surveillances = new ArrayList<PrivilegedSurveillanceDTO>();
        if (entity.getSurveillances() != null && entity.getSurveillances().size() > 0) {
            for (PrivilegedSurveillanceEntity entitySurv : entity.getSurveillances()) {
                this.surveillances.add(new PrivilegedSurveillanceDTO(entitySurv));
            }
        }
    }

    @Override
    public boolean equals(final Object anotherObject) {
        if (anotherObject == null || !(anotherObject instanceof QuarterlyReportRelevantListingDTO)) {
            return false;
        }
        QuarterlyReportRelevantListingDTO anotherRelevantListing = (QuarterlyReportRelevantListingDTO) anotherObject;
        if (this.getId() == null && anotherRelevantListing.getId() != null
                || this.getId() != null && anotherRelevantListing.getId() == null
                || this.getId() == null && anotherRelevantListing.getId() == null) {
            return false;
        }
        if (this.getId().longValue() == anotherRelevantListing.getId().longValue()) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (this.getId() == null) {
            return -1;
        }
        return this.getId().hashCode();
    }
}

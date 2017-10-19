package gov.healthit.chpl.dto.questionableActivity;

import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.entity.questionableActivity.QuestionableActivityListingEntity;

public class QuestionableActivityListingDTO extends QuestionableActivityDTO {
    private Long listingId;
    private CertifiedProductDetailsDTO listing;
    
    public QuestionableActivityListingDTO() {
        super();
    }
    
    public QuestionableActivityListingDTO(QuestionableActivityListingEntity entity) {
        super(entity);
        this.listingId = entity.getListingId();
        if(entity.getListing() != null) {
            this.listing = new CertifiedProductDetailsDTO();
            this.listing.setId(entity.getListingId());
            this.listing.setChplProductNumber(entity.getListing().getChplProductNumber());
            this.listing.setCertificationBodyName(entity.getListing().getAcbName());
            DeveloperDTO developer = new DeveloperDTO();
            developer.setName(entity.getListing().getDeveloper());
            this.listing.setDeveloper(developer);
            ProductDTO product = new ProductDTO();
            product.setName(entity.getListing().getProduct());
            this.listing.setProduct(product);
            ProductVersionDTO version = new ProductVersionDTO();
            version.setVersion(entity.getListing().getVersion());
            this.listing.setVersion(version);
            this.listing.setCertificationStatusName(entity.getListing().getCertificationStatus());
        }
    }
    
    public Class<?> getActivityObjectClass() {
        return CertifiedProductDetailsDTO.class;
    }

    public Long getListingId() {
        return listingId;
    }

    public void setListingId(Long listingId) {
        this.listingId = listingId;
    }

    public CertifiedProductDetailsDTO getListing() {
        return listing;
    }

    public void setListing(CertifiedProductDetailsDTO listing) {
        this.listing = listing;
    }

}

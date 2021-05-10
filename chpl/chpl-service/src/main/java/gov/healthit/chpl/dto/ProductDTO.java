package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.domain.Statuses;
import gov.healthit.chpl.entity.ProductActiveOwnerEntity;
import gov.healthit.chpl.entity.ProductEntity;
import gov.healthit.chpl.entity.ProductEntitySimple;
import gov.healthit.chpl.entity.ProductVersionEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDTO implements Serializable {
    private static final long serialVersionUID = -5440560685496661764L;
    private Long id;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private String name;
    private ContactDTO contact;
    @Builder.Default
    private Set<ProductVersionDTO> productVersions = new HashSet<ProductVersionDTO>();
    private String reportFileLocation;
    @Deprecated
    private Long developerId;
    @Deprecated
    private String developerName;
    @Deprecated
    private String developerCode;
    private DeveloperDTO owner;
    private Statuses statuses;
    private List<ProductOwnerDTO> ownerHistory;

    public ProductDTO() {
        this.owner = new DeveloperDTO();
        this.ownerHistory = new ArrayList<ProductOwnerDTO>();
        this.productVersions = new HashSet<ProductVersionDTO>();
    }

    public ProductDTO(ProductEntitySimple entity) {
        this();

        this.id = entity.getId();
        this.creationDate = entity.getCreationDate();
        this.deleted = entity.isDeleted();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
        this.name = entity.getName();
        this.reportFileLocation = entity.getReportFileLocation();
        this.owner.setId(entity.getDeveloperId());
        this.contact = new ContactDTO();
        this.contact.setId(entity.getContactId());
    }

    public ProductDTO(ProductEntity entity) {
        this();

        this.id = entity.getId();
        this.creationDate = entity.getCreationDate();
        this.deleted = entity.isDeleted();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
        this.name = entity.getName();
        this.reportFileLocation = entity.getReportFileLocation();
        if (entity.getContact() != null) {
            this.contact = new ContactDTO(entity.getContact());
        }
        this.developerId = entity.getDeveloperId();
        this.owner.setId(entity.getDeveloperId());
        if (entity.getDeveloper() != null) {
            this.developerName = entity.getDeveloper().getName();
            this.developerCode = entity.getDeveloper().getDeveloperCode();
            this.owner.setName(entity.getDeveloper().getName());
            this.owner.setDeveloperCode(entity.getDeveloper().getDeveloperCode());
            this.owner.setSelfDeveloper(entity.getDeveloper().getSelfDeveloper());
        }
        if (entity.getOwnerHistory() != null) {
            for (ProductActiveOwnerEntity ownerEntity : entity.getOwnerHistory()) {
                ProductOwnerDTO ownerDto = new ProductOwnerDTO(ownerEntity);
                this.ownerHistory.add(ownerDto);
            }
        }
        if (entity.getProductVersions() != null) {
            for (ProductVersionEntity version : entity.getProductVersions()) {
                ProductVersionDTO versionDto = new ProductVersionDTO(version);
                this.productVersions.add(versionDto);
            }
        }

        if (entity.getProductCertificationStatusesEntity() != null) {
            this.statuses = new Statuses(entity.getProductCertificationStatusesEntity().getActive(),
                    entity.getProductCertificationStatusesEntity().getRetired(),
                    entity.getProductCertificationStatusesEntity().getWithdrawnByDeveloper(),
                    entity.getProductCertificationStatusesEntity().getWithdrawnByAcb(),
                    entity.getProductCertificationStatusesEntity().getSuspendedByAcb(),
                    entity.getProductCertificationStatusesEntity().getSuspendedByOnc(),
                    entity.getProductCertificationStatusesEntity().getTerminatedByOnc());
        }
    }

    public ProductOwnerDTO getOwnerOnDate(Date date) {
        if (this.getOwnerHistory() == null || this.getOwnerHistory().size() == 0) {
            return null;
        }

        List<ProductOwnerDTO> localOwnerHistory = this.getOwnerHistory().stream()
                .collect(Collectors.toList());
        localOwnerHistory.add(ProductOwnerDTO.builder()
                .developer(this.getOwner())
                .productId(this.getId())
                //TODO: what does transfer date mean? the date this developer became
                //the owner of the product? the date the product got different ownership?
                .transferDate(null)
                .build());
        // first we need to make sure the status events are in ascending order
        this.getOwnerHistory().sort(new Comparator<ProductOwnerDTO>() {

            @Override
            public int compare(ProductOwnerDTO o1, ProductOwnerDTO o2) {
                if (o1.getTransferDate() != null && o2.getTransferDate() != null) {
                    return o1.getTransferDate().compareTo(o2.getTransferDate());
                }
                return 0;
            }
        });

        ProductOwnerDTO result = null;
        for (int i = 0; i < this.getOwnerHistory().size() && result == null; i++) {
            ProductOwnerDTO currOwner = this.getOwnerHistory().get(i);
            if (i < this.getOwnerHistory().size() - 1) {
                ProductOwnerDTO nextOwner = this.getOwnerHistory().get(i + 1);
                if (currOwner.getTransferDate() != null && currOwner.getTransferDate().longValue() <= date.getTime()
                        && nextOwner.getTransferDate() != null && nextOwner.getTransferDate().longValue() > date.getTime()) {
                    result = currOwner;
                }
            } else {
                result = currOwner;
            }
        }
        return result;
    }
}

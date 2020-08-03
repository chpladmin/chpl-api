package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.domain.Statuses;
import gov.healthit.chpl.entity.ProductActiveOwnerEntity;
import gov.healthit.chpl.entity.ProductEntity;
import gov.healthit.chpl.entity.ProductEntitySimple;
import gov.healthit.chpl.entity.ProductVersionEntity;
import gov.healthit.chpl.util.Util;
import lombok.Data;

@Data
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
    }

    public ProductDTO(final ProductEntitySimple entity) {
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

    public ProductDTO(final ProductEntity entity) {
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

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }
}

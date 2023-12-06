package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.LinkedHashSet;

import org.apache.commons.lang3.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dto.CertificationResultUcdProcessDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class CertifiedProductUcdProcess implements Serializable {
    private static final long serialVersionUID = 7248865611086710891L;

    private Long id;

    @Schema(description = "The UCD Process name")
    private String name;

    @JsonIgnore
    private String userEnteredName;

    @Schema(description = "A description of the UCD process used. This variable is applicable for "
            + "2014 and 2015 Edition, and a string variable that does not take any "
            + "restrictions on formatting or values.")
    private String details;

    @Builder.Default
    private LinkedHashSet<CertificationCriterion> criteria = new LinkedHashSet<CertificationCriterion>();

    public CertifiedProductUcdProcess() {
        super();
        this.criteria = new LinkedHashSet<CertificationCriterion>();
    }

    public CertifiedProductUcdProcess(CertificationResultUcdProcessDTO dto) {
        this();
        this.id = dto.getUcdProcessId();
        this.name = dto.getUcdProcessName();
        this.details = dto.getUcdProcessDetails();
    }

    public boolean matches(CertifiedProductUcdProcess anotherUcd) {
        boolean result = false;
        if (ObjectUtils.allNotNull(this.getId(), anotherUcd.getId())
                && this.getId().equals(anotherUcd.getId())) {
            result = true;
        }
        return result;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String ucdProcessName) {
        this.name = ucdProcessName;
    }

    public String getUserEnteredName() {
        return userEnteredName;
    }

    public void setUserEnteredName(String userEnteredName) {
        this.userEnteredName = userEnteredName;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String ucdProcessDetails) {
        this.details = ucdProcessDetails;
    }

    public LinkedHashSet<CertificationCriterion> getCriteria() {
        return criteria;
    }

    public void setCriteria(LinkedHashSet<CertificationCriterion> criteria) {
        this.criteria = criteria;
    }
}

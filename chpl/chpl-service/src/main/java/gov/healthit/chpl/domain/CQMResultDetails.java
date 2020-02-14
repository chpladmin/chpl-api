package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.CQMResultCriteriaDTO;
import gov.healthit.chpl.dto.CQMResultDetailsDTO;

/**
 * The clinical quality measure to which a given listing has been certified.
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CQMResultDetails implements Serializable {
    private static final long serialVersionUID = -7077008682408284325L;

    /**
     * CQM internal ID
     */
    @XmlElement(required = false, nillable = true)
    private Long id;

    /**
     * The CMS ID clinical quality measures to which the Health IT Module has
     * been certified. It is applicable to 2014 and 2015 Edition. For a list of
     * the clinical quality measures, please reference the CMS eCQM library.
     */
    @XmlElement(required = false, nillable = true)
    private String number;

    /**
     * The CMS ID clinical quality measures to which the Health IT Module has
     * been certified.
     */
    @XmlElement(required = false, nillable = true)
    private String cmsId;

    /**
     * The title of the clinical quality measure.
     */
    @XmlElement(required = false, nillable = true)
    private String title;

    /**
     * The description of the clinical quality measure.
     */
    @XmlElement(required = false, nillable = true)
    private String description;

    /**
     * The NQF Number of the clinical quality measure
     */
    @XmlElement(required = false, nillable = true)
    private String nqfNumber;

    /**
     * Type of CQM. 1 for Ambulatory, 2 for Inpatient
     */
    @XmlElement(required = false, nillable = true)
    private Long typeId;

    /**
     * Category of the clinial quality measure. Examples include
     * "Population/Public Health" or "Patient and Family Engagement"
     */
    @XmlElement(required = false, nillable = true)
    private String domain;

    /**
     * This variable indicates whether or not the clinical quality measure has
     * been certified to the related listing. It is applicable to 2014 and 2015
     * Edition and a binary variable that takes either true or false value.
     */
    @XmlElement(required = false, nillable = true)
    private Boolean success;

    /**
     * The corresponding version of the clinical quality measures to which the
     * Health IT Module has been certified. It is applicable to 2014 and 2015
     * Edition. For a list of clinical quality measures and their viable
     * versions, please reference the CMS eCQM library.
     */
    @XmlElementWrapper(name = "successVersions", nillable = true, required = false)
    @XmlElement(name = "version",required = false, nillable = true)
    private Set<String> successVersions;

    /**
     * All possible versions of the clinical quality measure. For a list of
     * clinical quality measures and their viable versions, please reference the
     * CMS eCQM library.
     */
    @XmlElementWrapper(name = "allVersions", nillable = true, required = false)
    @XmlElement(name = "version",required = false, nillable = true)
    private Set<String> allVersions;

    /**
     * The certification criteria to which a given clinical quality measure
     * applies. It is only applicable to 2015 Edition. It takes values include:
     * c1, c2, c3, c4,c1;c2[DC1], c1;c3, c1;c4, c2;c3, c2;c4, c3;c4, c1;c2;c3,
     * c2;c3;c4, c1;c2;c3;c4
     */
    @XmlElementWrapper(name = "criteriaList", nillable = true, required = false)
    @XmlElement(name = "criteria")
    private List<CQMResultCertification> criteria;

    public CQMResultDetails() {
        this.successVersions = new HashSet<String>();
        this.allVersions = new HashSet<String>();
        this.criteria = new ArrayList<CQMResultCertification>();
    }

    public CQMResultDetails(CQMResultDetailsDTO dto) {
        this();
        this.id = dto.getId();
        this.number = dto.getNumber();
        this.cmsId = dto.getCmsId();
        this.title = dto.getTitle();
        this.description = dto.getDescription();
        this.nqfNumber = dto.getNqfNumber();
        this.typeId = dto.getCqmCriterionTypeId();
        this.domain = dto.getDomain();

        if (!StringUtils.isEmpty(dto.getCmsId())) {
            this.getSuccessVersions().add(dto.getVersion());
        } else if (!StringUtils.isEmpty(dto.getNqfNumber())) {
            this.setSuccess(dto.getSuccess());
        }

        if (dto.getCriteria() != null && dto.getCriteria().size() > 0) {
            for (CQMResultCriteriaDTO criteriaDTO : dto.getCriteria()) {
                CQMResultCertification criteria = new CQMResultCertification(criteriaDTO);
                this.criteria.add(criteria);
            }
        }
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(final String number) {
        this.number = number;
    }

    public String getCmsId() {
        return cmsId;
    }

    public void setCmsId(final String cmsId) {
        this.cmsId = cmsId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getNqfNumber() {
        return nqfNumber;
    }

    public void setNqfNumber(final String nqfNumber) {
        this.nqfNumber = nqfNumber;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(final Long typeId) {
        this.typeId = typeId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(final String domain) {
        this.domain = domain;
    }

    public Set<String> getSuccessVersions() {
        return successVersions;
    }

    public void setSuccessVersions(final Set<String> successVersions) {
        this.successVersions = successVersions;
    }

    public Set<String> getAllVersions() {
        return allVersions;
    }

    public void setAllVersions(final Set<String> allVersions) {
        this.allVersions = allVersions;
    }

    public Boolean isSuccess() {
        if (successVersions != null && successVersions.size() > 0) {
            return true;
        }
        return success;
    }

    public void setSuccess(final Boolean success) {
        this.success = success;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public List<CQMResultCertification> getCriteria() {
        return criteria;
    }

    public void setCriteria(final List<CQMResultCertification> criteria) {
        this.criteria = criteria;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

}

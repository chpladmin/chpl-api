package gov.healthit.chpl.entity.listing.pending;

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

import gov.healthit.chpl.entity.AgeRangeEntity;
import gov.healthit.chpl.entity.EducationTypeEntity;
import gov.healthit.chpl.util.Util;

@Entity
@Table(name = "pending_test_participant")
public class PendingTestParticipantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pending_test_participant_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "test_participant_unique_id", nullable = false)
    private String uniqueId;

    @Column(name = "gender", nullable = false)
    private String gender;

    @Column(name = "user_entered_education_type", nullable = false)
    private String userEnteredEducation;

    @Column(name = "education_type_id", nullable = false)
    private Long educationTypeId;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "education_type_id", unique = true, nullable = true, insertable = false, updatable = false)
    private EducationTypeEntity education;

    @Column(name = "user_entered_age", nullable = false)
    private String userEnteredAge;

    @Column(name = "test_participant_age_id", nullable = false)
    private Long ageRangeId;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "test_participant_age_id", unique = true, nullable = true, insertable = false, updatable = false)
    private AgeRangeEntity ageRange;

    @Column(name = "occupation", nullable = false)
    private String occupation;

    @Column(name = "professional_experience_months", nullable = false)
    private Integer professionalExperienceMonths;

    @Column(name = "computer_experience_months", nullable = false)
    private Integer computerExperienceMonths;

    @Column(name = "product_experience_months", nullable = false)
    private Integer productExperienceMonths;

    @Column(name = "assistive_technology_needs", nullable = false)
    private String assistiveTechnologyNeeds;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(final String gender) {
        this.gender = gender;
    }

    public Long getEducationTypeId() {
        return educationTypeId;
    }

    public void setEducationTypeId(final Long educationTypeId) {
        this.educationTypeId = educationTypeId;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(final String occupation) {
        this.occupation = occupation;
    }

    public Integer getProfessionalExperienceMonths() {
        return professionalExperienceMonths;
    }

    public void setProfessionalExperienceMonths(final Integer professionalExperienceMonths) {
        this.professionalExperienceMonths = professionalExperienceMonths;
    }

    public Integer getComputerExperienceMonths() {
        return computerExperienceMonths;
    }

    public void setComputerExperienceMonths(final Integer computerExperienceMonths) {
        this.computerExperienceMonths = computerExperienceMonths;
    }

    public Integer getProductExperienceMonths() {
        return productExperienceMonths;
    }

    public void setProductExperienceMonths(final Integer productExperienceMonths) {
        this.productExperienceMonths = productExperienceMonths;
    }

    public String getAssistiveTechnologyNeeds() {
        return assistiveTechnologyNeeds;
    }

    public void setAssistiveTechnologyNeeds(final String assistiveTechnologyNeeds) {
        this.assistiveTechnologyNeeds = assistiveTechnologyNeeds;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(final String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public EducationTypeEntity getEducation() {
        return education;
    }

    public void setEducation(final EducationTypeEntity education) {
        this.education = education;
    }

    public Long getAgeRangeId() {
        return ageRangeId;
    }

    public void setAgeRangeId(final Long ageRangeId) {
        this.ageRangeId = ageRangeId;
    }

    public AgeRangeEntity getAgeRange() {
        return ageRange;
    }

    public void setAgeRange(final AgeRangeEntity ageRange) {
        this.ageRange = ageRange;
    }

    public String getUserEnteredEducation() {
        return userEnteredEducation;
    }

    public void setUserEnteredEducation(final String userEnteredEducation) {
        this.userEnteredEducation = userEnteredEducation;
    }

    public String getUserEnteredAge() {
        return userEnteredAge;
    }

    public void setUserEnteredAge(final String userEnteredAge) {
        this.userEnteredAge = userEnteredAge;
    }
}

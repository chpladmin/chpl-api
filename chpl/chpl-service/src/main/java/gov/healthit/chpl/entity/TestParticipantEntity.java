package gov.healthit.chpl.entity;

import java.io.Serializable;
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

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "test_participant")
public class TestParticipantEntity implements Serializable {
    private static final long serialVersionUID = -5914300900465335522L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "test_participant_id", nullable = false)
    private Long id;

    @Column(name = "gender", nullable = false)
    private String gender;

    @Column(name = "education_type_id", nullable = false)
    private Long educationTypeId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "education_type_id", nullable = true, insertable = false, updatable = false)
    private EducationTypeEntity education;

    @Column(name = "test_participant_age_id", nullable = false)
    private Long ageRangeId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "test_participant_age_id", nullable = true, insertable = false, updatable = false)
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

    @Column(name = "deleted", insertable = false)
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;
}

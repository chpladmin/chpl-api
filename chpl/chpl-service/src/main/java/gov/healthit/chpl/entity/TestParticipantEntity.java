package gov.healthit.chpl.entity;

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

import gov.healthit.chpl.domain.TestParticipant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "test_participant")
public class TestParticipantEntity extends EntityAudit {
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

    public TestParticipant toDomain() {
        return TestParticipant.builder()
                .id(this.getId())
                .gender(this.getGender())
                .educationType(this.getEducation().toDomain())
                .educationTypeId(this.getEducationTypeId())
                .educationTypeName(this.getEducation().getName())
                .age(this.getAgeRange().toDomain())
                .ageRangeId(this.getAgeRangeId())
                .ageRange(this.getAgeRange().getAge())
                .occupation(this.getOccupation())
                .professionalExperienceMonths(this.getProfessionalExperienceMonths())
                .computerExperienceMonths(this.getComputerExperienceMonths())
                .productExperienceMonths(this.getProductExperienceMonths())
                .assistiveTechnologyNeeds(this.getAssistiveTechnologyNeeds())
                .build();
    }

}

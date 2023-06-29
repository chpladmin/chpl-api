package gov.healthit.chpl.criteriaattribute.testtool;

import java.io.Serializable;
import java.time.LocalDate;
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

import gov.healthit.chpl.criteriaattribute.RuleEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "test_tool")
public class TestToolEntity implements Serializable {
    private static final long serialVersionUID = -5376346428073267735L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_tool_id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "value")
    private String value;

    @Basic(optional = true)
    @Column(name = "regulation_text_citation")
    private String regulationTextCitation;

    @Basic(optional = true)
    @Column(name = "start_day")
    private LocalDate startDay;

    @Basic(optional = true)
    @Column(name = "end_day")
    private LocalDate endDay;

    @Basic(optional = true)
    @Column(name = "required_day")
    private LocalDate requiredDay;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    private RuleEntity rule;

    //@Basic(optional = true)
    //@OneToMany(targetEntity = CertificationResultTestToolEntity.class, fetch = FetchType.LAZY)
    //private List<CertificationResultTestTool> certificationResultTestTool;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(nullable = false)
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    public TestTool toDomain() {
        return TestTool.builder()
                .id(id)
                .value(value)
                .regulationTextCitation(regulationTextCitation)
                .startDay(startDay)
                .endDay(endDay)
                .requiredDay(requiredDay)
                .rule(rule != null ? rule.toDomain() : null)
                .build();
    }
}

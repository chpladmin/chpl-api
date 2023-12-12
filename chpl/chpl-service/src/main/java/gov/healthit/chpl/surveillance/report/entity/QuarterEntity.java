package gov.healthit.chpl.surveillance.report.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import gov.healthit.chpl.entity.EntityAudit;
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
@Immutable
@Table(name = "quarter")
public class QuarterEntity extends EntityAudit {
    private static final long serialVersionUID = 890033274436575913L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "quarter_begin_month")
    private Integer quarterBeginMonth;

    @Column(name = "quarter_begin_day")
    private Integer quarterBeginDay;

    @Column(name = "quarter_end_month")
    private Integer quarterEndMonth;

    @Column(name = "quarter_end_day")
    private Integer quarterEndDay;

}

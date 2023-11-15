package gov.healthit.chpl.entity.statistics;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.datatypes.StringJsonUserType;
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
@Table(name = "summary_statistics")
@TypeDefs({@TypeDef(name = "StringJsonObject", typeClass = StringJsonUserType.class)})
public class SummaryStatisticsEntity extends EntityAudit {
    private static final long serialVersionUID = 4752929481454934958L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_statistics_id", nullable = false)
    private Long summaryStatisticsId;

    @Column(name = "end_Date", nullable = false)
    private Date endDate;

    @Column(name = "summary_statistics")
    @Type(type = "StringJsonObject")
    private String summaryStatistics;

}

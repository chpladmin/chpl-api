package gov.healthit.chpl.entity.statistics;

import java.util.Date;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.lastmodifieduserstrategy.LastModifiedUserStrategy;
import gov.healthit.chpl.entity.lastmodifieduserstrategy.SystemUserStrategy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
//@Convert(attributeName = "summaryStatistics", converter = StringJsonUserType.class)
public class SummaryStatisticsEntity extends EntityAudit {
    private static final long serialVersionUID = 4752929481454934958L;

    @Override
    public LastModifiedUserStrategy getLastModifiedUserStrategy() {
        return new SystemUserStrategy();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_statistics_id", nullable = false)
    private Long summaryStatisticsId;

    @Column(name = "end_Date", nullable = false)
    private Date endDate;

    @Column(name = "summary_statistics")
    @JdbcTypeCode(SqlTypes.JSON)
    private String summaryStatistics;

}

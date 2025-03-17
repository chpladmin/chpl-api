package gov.healthit.chpl.report.common;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = false)
public class CertificationCriterionWithOrder extends CertificationCriterion {
    private static final long serialVersionUID = -859127653800126456L;

    private Long order;

    public CertificationCriterionWithOrder(CertificationCriterion cc) {
        this.setId(cc.getId());
        this.setNumber(cc.getNumber());
        this.setTitle(cc.getTitle());
        this.setDescription(cc.getDescription());
        this.setCertificationEdition(cc.getCertificationEdition());
        this.setCertificationEditionId(cc.getCertificationEditionId());
        this.setStartDay(cc.getStartDay());
        this.setEndDay(cc.getEndDay());
        this.setRule(cc.getRule());
        this.setCompanionGuideLink(cc.getCompanionGuideLink());
    }
}

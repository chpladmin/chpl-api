package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.CertificationCriterion;

public class CertificationCriterionXmlGenerator extends XmlGenerator {
    public static void add(List<CertificationCriterion> criterionList, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (criterionList != null) {
            sw.writeStartElement(rootNodeName);
            for (CertificationCriterion criterion : criterionList) {
                add(criterion, "criteria", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(CertificationCriterion criterion, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (criterion != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(criterion.getCertificationEdition(), "certificationEdition", sw);
            createSimpleElement(criterion.getCertificationEditionId(), "certificationEditionId", sw);
            createSimpleElement(criterion.getDescription(), "description", sw);
            createSimpleElement(criterion.getId(), "id", sw);
            createSimpleElement(criterion.getNumber(), "number", sw);
            createSimpleElement(criterion.getTitle(), "title", sw);
            sw.writeEndElement();
        }
    }
}

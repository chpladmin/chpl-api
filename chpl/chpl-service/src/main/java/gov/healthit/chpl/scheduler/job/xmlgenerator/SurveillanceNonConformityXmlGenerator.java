package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;

public class SurveillanceNonConformityXmlGenerator extends XmlGenerator {
    public static void add(List<SurveillanceNonconformity> sncs, String rootNodeName,
            XMLStreamWriter sw) throws XMLStreamException {
        if (sncs != null) {
            sw.writeStartElement(rootNodeName);
            for (SurveillanceNonconformity snc : sncs) {
                add(snc, "nonconformity", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(SurveillanceNonconformity snc, String rootNodeName, XMLStreamWriter sw)
            throws XMLStreamException {
        if (snc != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(snc.getCapApprovalDate(), "capApprovalDate", sw);
            createSimpleElement(snc.getCapEndDate(), "capEndDate", sw);
            createSimpleElement(snc.getCapMustCompleteDate(), "capMustCompleteDate", sw);
            createSimpleElement(snc.getCapStartDate(), "capStartDate", sw);
            CertificationCriterionXmlGenerator.add(snc.getCriterion(), "criterion", sw);
            createSimpleElement(snc.getDateOfDetermination(), "dateOfDetermination", sw);
            createSimpleElement(snc.getDeveloperExplanation(), "developerExplanation", sw);
            SurveillanceNonConformityDocumentXmlGenerator.add(snc.getDocuments(), "documents", sw);
            createSimpleElement(snc.getFindings(), "findings", sw);
            createSimpleElement(snc.getId(), "id", sw);
            createSimpleElement(snc.getLastModifiedDate(), "lastModifiedDate", sw);
            createSimpleElement(snc.getNonconformityCloseDate(), "nonconformityCloseDate", sw);
            createSimpleElement(snc.getNonconformityStatus(), "nonconformityStatus", sw);
            createSimpleElement(snc.getNonconformityType(), "nonconformityType", sw);
            createSimpleElement(snc.getResolution(), "resolution", sw);
            createSimpleElement(snc.getSitesPassed(), "sitesPassed", sw);
            createSimpleElement(snc.getSummary(), "summary", sw);
            createSimpleElement(snc.getTotalSites(), "totalSites", sw);
            sw.writeEndElement();
        }
    }
}

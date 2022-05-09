package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.compliance.DirectReviewNonConformity;

public class DirectReviewNonConformityXmlGenerator extends XmlGenerator {
    public static void add(List<DirectReviewNonConformity> nonconformities, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (nonconformities != null) {
            sw.writeStartElement(rootNodeName);
            for (DirectReviewNonConformity nonconformity : nonconformities) {
                add(nonconformity, "nonConformity", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(DirectReviewNonConformity nonconformity, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        sw.writeStartElement(rootNodeName);
        createSimpleElement(nonconformity.getCapApprovalDate() != null
                ? nonconformity.getCapApprovalDate().toString() : null, "capApprovalDate", sw);
        createSimpleElement(nonconformity.getCapEndDate() != null
                ? nonconformity.getCapEndDate().toString() : null, "capEndDate", sw);
        createSimpleElement(nonconformity.getCapMustCompleteDate() != null
                ? nonconformity.getCapMustCompleteDate().toString() : null, "capMustCompleteDate", sw);
        createSimpleElement(nonconformity.getCreated(), "created", sw);
        DeveloperAssociatedListingXmlGenerator.add(nonconformity.getDeveloperAssociatedListings(), "developerAssociatedListings", sw);
        createSimpleElement(nonconformity.getLastUpdated(), "lastUpdated", sw);
        createSimpleElement(nonconformity.getNonConformityStatus(), "nonConformityStatus", sw);
        createSimpleElement(nonconformity.getNonConformityType(), "nonConformityType", sw);
        sw.writeEndElement();
    }
}

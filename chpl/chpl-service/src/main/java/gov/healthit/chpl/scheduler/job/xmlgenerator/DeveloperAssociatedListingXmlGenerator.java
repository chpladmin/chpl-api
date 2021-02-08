package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.compliance.DeveloperAssociatedListing;

public class DeveloperAssociatedListingXmlGenerator extends XmlGenerator {
    public static void add(List<DeveloperAssociatedListing> dals, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (dals != null) {
            sw.writeStartElement(rootNodeName);
            for (DeveloperAssociatedListing dal : dals) {
                add(dal, "listing", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(DeveloperAssociatedListing dal, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        sw.writeStartElement(rootNodeName);
        createSimpleElement(dal.getChplProductNumber(), "chplProductNumber", sw);
        createSimpleElement(dal.getId(), "id", sw);
        sw.writeEndElement();
    }
}

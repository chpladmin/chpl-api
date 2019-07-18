package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformityDocument;

public class SurveillanceNonConformityDocumentXmlGenerator extends XmlGenerator {
    public static void add(List<SurveillanceNonconformityDocument> docs, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (docs != null) {
            sw.writeStartElement(rootNodeName);
            for (SurveillanceNonconformityDocument doc : docs) {
                add(doc, "document", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(SurveillanceNonconformityDocument doc, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (doc != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(doc.getId(), "id", sw);
            createSimpleElement(doc.getFileName(), "fileName", sw);
            createSimpleElement(doc.getFileType(), "fileType", sw);
            sw.writeEndElement();
        }
    }
}

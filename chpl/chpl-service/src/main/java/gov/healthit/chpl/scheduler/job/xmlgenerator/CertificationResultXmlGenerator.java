package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.CertificationResult;

public class CertificationResultXmlGenerator {
    public static void add(List<CertificationResult> results, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (results != null) {
            sw.writeStartElement(rootNodeName);
            for (CertificationResult result : results) {
                
            }
            sw.writeEndElement();
        }
    }
    
    public static void add(CertificationResult result, String rootNodeName, XMLStreamWriter sw) {
        
    }
}

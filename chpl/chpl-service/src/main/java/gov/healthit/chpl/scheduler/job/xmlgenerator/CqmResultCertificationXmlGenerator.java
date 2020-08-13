package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.CQMResultCertification;

public class CqmResultCertificationXmlGenerator extends XmlGenerator {
    public static void add(List<CQMResultCertification> certs, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (certs != null) {
            sw.writeStartElement(rootNodeName);
            for (CQMResultCertification cert : certs) {
                add(cert, "criteria", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(CQMResultCertification cert, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (cert != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(cert.getCertificationId(), "certificationId", sw);
            createSimpleElement(cert.getCertificationNumber(), "certificationNumber", sw);
            createSimpleElement(cert.getId(), "id", sw);
            sw.writeEndElement();
        }
    }
}

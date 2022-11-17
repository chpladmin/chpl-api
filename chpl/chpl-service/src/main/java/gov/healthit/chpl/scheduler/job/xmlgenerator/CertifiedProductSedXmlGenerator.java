package gov.healthit.chpl.scheduler.job.xmlgenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.CertifiedProductSed;

public class CertifiedProductSedXmlGenerator extends XmlGenerator {
    public static void add(CertifiedProductSed sed, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (sed != null) {
            sw.writeStartElement(rootNodeName);
            TestTaskXmlGenerator.add(sed.getTestTasks(), "testTasks", sw);
            CertifiedProductUcdProcessXmlGenerator.add(sed.getUcdProcesses(), "ucdProcesses", sw);
            sw.writeEndElement();
        }
    }

}

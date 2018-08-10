package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.CertificationResultTestProcedure;

public class CertificationResultTestProcedureXmlGenerator extends XmlGenerator {
    public static void add(List<CertificationResultTestProcedure> procedures, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (procedures != null) {
            sw.writeStartElement(rootNodeName);
            for (CertificationResultTestProcedure procedure : procedures) {
                add(procedure, "testProcedure", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(CertificationResultTestProcedure procedure, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (procedure != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(procedure.getId(), "id", sw);
            TestProcedureXmlGenerator.add(procedure.getTestProcedure(), "testProcedure", sw);
            createSimpleElement(procedure.getTestProcedureVersion(), "testProcedureVersion", sw);
            sw.writeEndElement();
        }
    }
}

package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.CertifiedProductTargetedUser;

public class CertifiedProductTargetedUserXmlGenerator extends XmlGenerator {
    public static void add(List<CertifiedProductTargetedUser> users, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (users != null) {
            sw.writeStartElement(rootNodeName);
            for (CertifiedProductTargetedUser user : users) {
                add(user, "targetedUser", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(CertifiedProductTargetedUser user, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (user != null) {
            sw.writeStartElement(rootNodeName);
            createSimpleElement(user.getId(), "id", sw);
            createSimpleElement(user.getTargetedUserId(), "targetedUserId", sw);
            createSimpleElement(user.getTargetedUserName(), "targetedUserName", sw);
            sw.writeEndElement();
        }
    }
}

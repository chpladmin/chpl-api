package gov.healthit.chpl.scheduler.job.xmlgenerator;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;

public class TestTaskXmlGenerator extends XmlGenerator {
    public static void add(List<TestTask> tasks, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (tasks != null) {
            sw.writeStartElement(rootNodeName);
            for (TestTask task : tasks) {
                add(task, "testTask", sw);
            }
            sw.writeEndElement();
        }
    }

    public static void add(TestTask task, String rootNodeName, XMLStreamWriter sw) throws XMLStreamException {
        if (task != null) {
            sw.writeStartElement(rootNodeName);
            CertificationCriterionXmlGenerator.add(task.getCriteria(), "criteriaList", sw);
            createSimpleElement(task.getDescription(), "description", sw);
            createSimpleElement(task.getId(), "id", sw);
            createSimpleElement(task.getTaskErrors(), "taskErrors", sw);
            createSimpleElement(task.getTaskErrorsStddev(), "taskErrorsStddev", sw);
            createSimpleElement(task.getTaskPathDeviationObserved(), "taskPathDeviationObserved", sw);
            createSimpleElement(task.getTaskPathDeviationOptimal(), "taskPathDeviationOptimal", sw);
            createSimpleElement(task.getTaskRating(), "taskRating", sw);
            createSimpleElement(task.getTaskRatingScale(), "taskRatingScale", sw);
            createSimpleElement(task.getTaskRatingStddev(), "taskRatingStddev", sw);
            createSimpleElement(task.getTaskSuccessAverage(), "taskSuccessAverage", sw);
            createSimpleElement(task.getTaskSuccessStddev(), "taskSuccessStddev", sw);
            createSimpleElement(task.getTaskTimeAvg(), "taskTimeAvg", sw);
            createSimpleElement(task.getTaskTimeDeviationObservedAvg(), "taskTimeDeviationObservedAvg", sw);
            createSimpleElement(task.getTaskTimeDeviationOptimalAvg(), "taskTimeDeviationOptimalAvg", sw);
            createSimpleElement(task.getTaskTimeStddev(), "taskTimeStddev", sw);
            //not in alphabetical order on purpose because the ordering appears to use the name
            //of the field not the name in the annotation (ordering it as if the name is 'testParticipants'
            //not 'participants')
            TestParticipantXmlGenerator.add(new ArrayList<TestParticipant>(task.getTestParticipants()), "participants", sw);
            sw.writeEndElement();
        }
    }
}

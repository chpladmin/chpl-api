package gov.healthit.chpl.scheduler.job.summarystatistics.email;

import java.util.List;
import java.util.Objects;

import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.EmailCertificationBodyStatistic;

public abstract class StatisticsSectionCreator {

    public String buildHeader(String text, Long count) {
        StringBuilder header = new StringBuilder();
        return header.append("<h4>")
                .append(text)
                .append(" - ")
                .append(count)
                .append("</h4>")
                .toString();
    }

    public String buildHeader(String text, String count) {
        StringBuilder header = new StringBuilder();
        return header.append("<h4>")
                .append(text)
                .append(" - ")
                .append(count)
                .append("</h4>")
                .toString();
    }

    public String buildSection(String header, Long headerCount, List<EmailCertificationBodyStatistic> stats) {
        StringBuilder section = new StringBuilder();

        section.append(buildSectionHeader(header, headerCount))
        .append("<ul>");

        stats.stream()
        .forEach(stat -> section.append(buildAcbCount(stat.getAcbName(), stat.getCount())));

        section.append("</ul>");
        return section.toString();
    }

    public String buildItem(String text, Long count) {
        StringBuilder item = new StringBuilder();
        return item.append("<li>")
                .append(text)
                .append(" - ")
                .append(count)
                .append("</li>")
                .toString();
    }

    public String buildItem(String text, String count) {
        StringBuilder item = new StringBuilder();
        return item.append("<li>")
                .append(text)
                .append(" - ")
                .append(count)
                .append("</li>")
                .toString();
    }

    private String buildSectionHeader(String header, Long count) {
        StringBuilder section = new StringBuilder();
        return section.append("<li>")
                .append(header)
                .append(" - ")
                .append(Objects.nonNull(count) ? count.toString() : "")
                .append("</li>")
                .toString();
    }

    private String buildAcbCount(String acbName, Long count) {
        StringBuilder section = new StringBuilder();
        return section.append("<li>Certified by ")
                .append(acbName)
                .append(" - ")
                .append(count)
                .append("</li>")
                .toString();
    }

    public Integer get2015EditionAsInteger() {
        return Integer.valueOf(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
    }

    public Integer get2014EditionAsInteger() {
        return Integer.valueOf(CertificationEditionConcept.CERTIFICATION_EDITION_2014.getYear());
    }
}

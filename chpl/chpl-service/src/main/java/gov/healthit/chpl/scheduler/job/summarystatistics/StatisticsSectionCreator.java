package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.util.List;
import java.util.stream.Collectors;

import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;

public abstract class StatisticsSectionCreator {

    public abstract Long getStatistic(CertifiedBodyStatistics stat);

    public String buildHeader(String text, Long count) {
        StringBuilder header = new StringBuilder();
        return header.append("<h4>")
                .append(text)
                .append(" - ")
                .append(count)
                .append("</h4>")
                .toString();
    }

    public String buildSection(String header, Long headerCount, List<CertifiedBodyStatistics> stats) {
        StringBuilder section = new StringBuilder();

        section.append(buildSectionHeader(header, headerCount))
                .append("<ul>");

        stats.stream()
                .forEach(stat -> section.append(buildAcbCount(stat.getName(), getStatistic(stat))));

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

    private String buildSectionHeader(String header, Long count) {
        StringBuilder section = new StringBuilder();
        return section.append("<li>")
                .append(header)
                .append(" - ")
                .append(count)
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

    public Long sumTotalListings(List<CertifiedBodyStatistics> stats) {
        return stats.stream()
                .collect(Collectors.summingLong(CertifiedBodyStatistics::getTotalListings));
    }

    public Long sumTotalDeveloperWithListings(List<CertifiedBodyStatistics> stats) {
        return stats.stream()
                .collect(Collectors.summingLong(CertifiedBodyStatistics::getTotalDevelopersWithListings));
    }

    public Integer get2015EditionAsInteger() {
        return Integer.valueOf(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
    }

    public Integer get2014EditionAsInteger() {
        return Integer.valueOf(CertificationEditionConcept.CERTIFICATION_EDITION_2014.getYear());
    }
}

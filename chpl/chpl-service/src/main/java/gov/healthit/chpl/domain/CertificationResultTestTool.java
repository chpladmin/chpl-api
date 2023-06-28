package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.time.LocalDate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.criteriaattribute.Rule;
import gov.healthit.chpl.criteriaattribute.testtool.TestTool;
import lombok.AllArgsConstructor;
import lombok.Builder;

//TODO: OCD-4242 Can I remove the unnecessary setters?


/**
 * The test tool used to certify the Health IT Module to the corresponding
 * certification criteria Allowable values are based on the NIST 2014 and 2015
 * Edition Test Tools.
 *
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@Builder
public class CertificationResultTestTool implements Serializable {
    private static final long serialVersionUID = 2785949879671019720L;

    /**
     * Test tool to certification result mapping internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * Test tool internal ID
     */
    @XmlElement(required = true)
    private Long testToolId;

    /**
     * The test tool used to certify the Health IT Module to the corresponding
     * certification criteria
     */
//    @XmlElement(required = true)
//    private String testToolName;

    /**
     * The version of the test tool being used. This variable is applicable for
     * 2014 and 2015 Edition, and a string variable that does not take any
     * restrictions on formatting or values.
     */
    @XmlElement(required = false, nillable = true)
    private String version;

    /**
     * TODO: Need this text
     */
    @XmlElement(required = true, nillable = true)
    private String value;

    /**
     * TODO: Need this text
     */
    @XmlElement(required = false, nillable = true)
    private String regulationTextCitation;

    /**
     * TODO: Need this text
     */
    @XmlElement(required = false, nillable = true)
    private LocalDate startDay;

    /**
     * TODO: Need this text
     */
    @XmlElement(required = false, nillable = true)
    private LocalDate endDay;

    /**
     * TODO: Need this text
     */
    @XmlElement(required = false, nillable = true)
    private LocalDate requiredDay;

    /**
     * TODO: Need this text
     */
    @XmlElement(required = false, nillable = true)
    private Rule rule;


    /**
     * Whether or not the test tool has been retired.
     */
    @XmlElement(required = false, nillable = true)
    private boolean retired;

    @XmlTransient
    @JsonIgnore
    private Long certificationResultId;

    @XmlTransient
    @JsonIgnore
    private TestTool testTool;

    /**
     * Default constructor.
     */
    public CertificationResultTestTool() {
        super();
    }

    /**
     * Indicate if this test tool matches another one.
     * @param anotherTool the other tool
     * @return true iff (id or name equal) and version equals
     */
    public boolean matches(final CertificationResultTestTool anotherTool) {
        boolean result = false;
        if (this.getTestToolId() != null && anotherTool.getTestToolId() != null
                && this.getTestToolId().longValue() == anotherTool.getTestToolId().longValue()
                && ((StringUtils.isEmpty(this.getVersion())
                        && StringUtils.isEmpty(anotherTool.getVersion()))
                     ||
                     (!StringUtils.isEmpty(this.getVersion())
                             && !StringUtils.isEmpty(anotherTool.getVersion())
                             && this.getVersion().equalsIgnoreCase(anotherTool.getVersion())))) {
            result = true;
        } else if (!StringUtils.isEmpty(this.getValue()) && !StringUtils.isEmpty(anotherTool.getValue())
                && this.getValue().equalsIgnoreCase(anotherTool.getValue())
                && ((StringUtils.isEmpty(this.getValue()) && StringUtils.isEmpty(anotherTool.getValue()))
                     ||
                     (!StringUtils.isEmpty(this.getVersion())
                             && !StringUtils.isEmpty(anotherTool.getVersion())
                             && this.getVersion().equalsIgnoreCase(anotherTool.getVersion())))) {
            result = true;
        }
        return result;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getTestToolId() {
        return testToolId;
    }

    private void setTestToolId(final Long testToolId) {
        this.testToolId = testToolId;
    }

    public String getValue() {
        return value;
    }

    private void setValue(final String value) {
        this.value = value;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String testToolVersion) {
        this.version = testToolVersion;
    }

    public boolean isRetired() {
        return retired;
    }

    private void setRetired(final boolean retired) {
        this.retired = retired;
    }

    public Long getCertificationResultId() {
        return this.certificationResultId;
    }

    public void setCertificationResultId(Long certificationResultId) {
        this.certificationResultId = certificationResultId;
    }

    public TestTool getTestTool() {
        return this.testTool;
    }

    public void setTestTool(TestTool testTool) {
        setTestToolId(testTool.getId());
        setValue(testTool.getValue());
        setRegulationTextCitation(testTool.getRegulationTextCitation());
        setStartDay(testTool.getStartDay());
        setEndDay(testTool.getEndDay());
        setRequiredDay(testTool.getRequiredDay());
        setRetired(testTool.isRetired());
    }

    public String getRegulationTextCitation() {
        return regulationTextCitation;
    }

    private void setRegulationTextCitation(String regulationTextCitation) {
        this.regulationTextCitation = regulationTextCitation;
    }

    public LocalDate getStartDay() {
        return startDay;
    }

    private void setStartDay(LocalDate startDay) {
        this.startDay = startDay;
    }

    public LocalDate getEndDay() {
        return endDay;
    }

    private void setEndDay(LocalDate endDay) {
        this.endDay = endDay;
    }

    public LocalDate getRequiredDay() {
        return requiredDay;
    }

    private void setRequiredDay(LocalDate requiredDay) {
        this.requiredDay = requiredDay;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

}

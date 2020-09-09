package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;

/**
 * Safety Enhanced Design data that is relied upon by this Health IT Module to
 * demonstrate its compliance with a certification criterion or criteria. The
 * SED data includes both user-centered design and test tasks.
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class CertifiedProductSed implements Serializable {
    private static final long serialVersionUID = -4131156681875211447L;

    /**
     * The user-centered design (UCD) process applied for the corresponding
     * certification criteria. This variable is applicable for 2014 and 2015
     * Edition, and a string variable that does not take any restrictions on
     * formatting or values.
     */
    @XmlElementWrapper(name = "ucdProcesses", nillable = true, required = false)
    @XmlElement(name = "ucdProcess")
    @Singular
    private List<UcdProcess> ucdProcesses;

    /**
     * Tasks used for SED testing
     */
    @XmlElementWrapper(name = "testTasks", nillable = true, required = false)
    @XmlElement(name = "testTask")
    @Singular
    private List<TestTask> testTasks;

    public CertifiedProductSed() {
        super();
        this.ucdProcesses = new ArrayList<UcdProcess>();
        this.testTasks = new ArrayList<TestTask>();
    }

    public List<UcdProcess> getUcdProcesses() {
        return ucdProcesses;
    }

    public void setUcdProcesses(final List<UcdProcess> ucdProcesses) {
        this.ucdProcesses = ucdProcesses;
    }

    public List<TestTask> getTestTasks() {
        return testTasks;
    }

    public void setTestTasks(final List<TestTask> testTasks) {
        this.testTasks = testTasks;
    }
}

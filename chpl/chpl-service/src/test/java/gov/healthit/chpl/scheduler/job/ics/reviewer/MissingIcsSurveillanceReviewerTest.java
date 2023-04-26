package gov.healthit.chpl.scheduler.job.ics.reviewer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.compliance.surveillance.SurveillanceManager;
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.domain.surveillance.RequirementType;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.dto.CertifiedProductDTO;

public class MissingIcsSurveillanceReviewerTest {
    private static final String ICS_REQUIREMENT_TITLE = "Inherited Certified Status";
    private static final String ERROR_MESSAGE = "ICS surveillance required for listings that use ICS more than 3 consecutive times";

    private SurveillanceManager survManager;
    private ListingGraphDAO listingGraphDao;
    private MissingIcsSurveillanceReviewer reviewer;

    @Before
    @SuppressWarnings("checkstyle:magicnumber")
    public void setup() {
        survManager = Mockito.mock(SurveillanceManager.class);
        listingGraphDao = Mockito.mock(ListingGraphDAO.class);
        reviewer = new MissingIcsSurveillanceReviewer(survManager, listingGraphDao, ERROR_MESSAGE);
    }

    @Test
    public void review_listingWithoutIcs_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .chplProductNumber("15.02.05.1439.A111.01.00.1.200219")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(false)
                        .build())
                .build();
        String errorMessage = reviewer.getIcsError(listing);
        assertNull(errorMessage);
    }

    @Test
    public void review_listingWithSelfReference_noSurveillance_noErrorAndNoStackOverflow() {
        CertifiedProductDTO childDto = CertifiedProductDTO.builder()
                .id(2L)
                .chplProductNumber("15.02.05.1439.A111.01.01.1.200219")
                .build();
        CertifiedProductSearchDetails child = CertifiedProductSearchDetails.builder()
                .id(2L)
                .chplProductNumber("15.02.05.1439.A111.01.01.1.200219")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .build())
                .build();

        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(child.getId())))
            .thenReturn(Stream.of(childDto).collect(Collectors.toList()));

        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(child.getId())))
            .thenReturn(null);

        String errorMessage = reviewer.getIcsError(child);
        assertNull(errorMessage);
    }

    @Test
    public void review_listingWithCircularInheritance_noSurveillance_noErrorAndNoStackOverflow() {
        CertifiedProductSearchDetails listingA = CertifiedProductSearchDetails.builder()
                .id(1L)
                .chplProductNumber("15.02.05.1439.A111.01.01.1.200219")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .build())
                .build();
        CertifiedProductSearchDetails listingB = CertifiedProductSearchDetails.builder()
                .id(2L)
                .chplProductNumber("15.02.05.1439.A111.01.02.1.200219")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .build())
                .build();

        CertifiedProductDTO listingADto = CertifiedProductDTO.builder()
                .id(1L)
                .chplProductNumber("15.02.05.1439.A111.01.01.1.200219")
                .build();
        CertifiedProductDTO listingBDto = CertifiedProductDTO.builder()
                .id(2L)
                .chplProductNumber("15.02.05.1439.A111.01.02.1.200219")
                .build();

        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(listingA.getId())))
            .thenReturn(Stream.of(listingBDto).collect(Collectors.toList()));
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(listingB.getId())))
            .thenReturn(Stream.of(listingADto).collect(Collectors.toList()));

        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(listingA.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(listingB.getId())))
            .thenReturn(null);
        String errorMessage = reviewer.getIcsError(listingA);
        assertNull(errorMessage);
    }

    @Test
    public void review_listingWithOneGeneration_noSurveillance_noError() {
        CertifiedProductDTO parent = CertifiedProductDTO.builder()
                .id(1L)
                .chplProductNumber("15.02.05.1439.A111.01.00.1.200219")
                .build();
        CertifiedProductSearchDetails child = CertifiedProductSearchDetails.builder()
                .id(2L)
                .chplProductNumber("15.02.05.1439.A111.01.01.1.200219")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .build())
                .build();

        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(child.getId())))
            .thenReturn(Stream.of(parent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(parent.getId())))
            .thenReturn(null);

        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(child.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(parent.getId())))
            .thenReturn(null);

        String errorMessage = reviewer.getIcsError(child);
        assertNull(errorMessage);
    }

    @Test
    public void review_listingWithTwoGenerations_noSurveillance_noError() {
        CertifiedProductDTO grandparent = CertifiedProductDTO.builder()
                .id(1L)
                .chplProductNumber("15.02.05.1439.A111.01.00.1.200219")
                .build();
        CertifiedProductDTO parent = CertifiedProductDTO.builder()
                .id(2L)
                .chplProductNumber("15.02.05.1439.A111.01.01.1.200219")
                .build();
        CertifiedProductSearchDetails child = CertifiedProductSearchDetails.builder()
                .id(3L)
                .chplProductNumber("15.02.05.1439.A111.01.02.1.200219")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .build())
                .build();

        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(child.getId())))
            .thenReturn(Stream.of(parent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(parent.getId())))
            .thenReturn(Stream.of(grandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(grandparent.getId())))
            .thenReturn(null);

        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(child.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(parent.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(grandparent.getId())))
            .thenReturn(null);

        String errorMessage = reviewer.getIcsError(child);
        assertNull(errorMessage);
    }

    @Test
    public void review_listingWithThreeGenerations_noSurveillance_noError() {
        CertifiedProductDTO greatGrandparent = CertifiedProductDTO.builder()
                .id(1L)
                .chplProductNumber("15.02.05.1439.A111.01.00.1.200219")
                .build();
        CertifiedProductDTO grandparent = CertifiedProductDTO.builder()
                .id(2L)
                .chplProductNumber("15.02.05.1439.A111.01.01.1.200219")
                .build();
        CertifiedProductDTO parent = CertifiedProductDTO.builder()
                .id(3L)
                .chplProductNumber("15.02.05.1439.A111.01.02.1.200219")
                .build();
        CertifiedProductSearchDetails child = CertifiedProductSearchDetails.builder()
                .id(4L)
                .chplProductNumber("15.02.05.1439.A111.01.03.1.200219")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .build())
                .build();

        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(child.getId())))
            .thenReturn(Stream.of(parent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(parent.getId())))
            .thenReturn(Stream.of(grandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(grandparent.getId())))
            .thenReturn(Stream.of(greatGrandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(greatGrandparent.getId())))
            .thenReturn(null);

        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(child.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(parent.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(grandparent.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(greatGrandparent.getId())))
            .thenReturn(null);

        String errorMessage = reviewer.getIcsError(child);
        assertNull(errorMessage);
    }

    @Test
    public void review_listingWithFourGenerations_noSurveillance_hasError() {
        CertifiedProductDTO greatGreatGrandparent = CertifiedProductDTO.builder()
                .id(1L)
                .chplProductNumber("15.02.05.1439.A111.01.00.1.200219")
                .build();
        CertifiedProductDTO greatGrandparent = CertifiedProductDTO.builder()
                .id(2L)
                .chplProductNumber("15.02.05.1439.A111.01.01.1.200219")
                .build();
        CertifiedProductDTO grandparent = CertifiedProductDTO.builder()
                .id(3L)
                .chplProductNumber("15.02.05.1439.A111.01.02.1.200219")
                .build();
        CertifiedProductDTO parent = CertifiedProductDTO.builder()
                .id(4L)
                .chplProductNumber("15.02.05.1439.A111.01.03.1.200219")
                .build();
        CertifiedProductSearchDetails child = CertifiedProductSearchDetails.builder()
                .id(5L)
                .chplProductNumber("15.02.05.1439.A111.01.04.1.200219")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .build())
                .build();

        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(child.getId())))
            .thenReturn(Stream.of(parent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(parent.getId())))
            .thenReturn(Stream.of(grandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(grandparent.getId())))
            .thenReturn(Stream.of(greatGrandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(greatGrandparent.getId())))
            .thenReturn(Stream.of(greatGreatGrandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(greatGreatGrandparent.getId())))
            .thenReturn(null);

        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(child.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(parent.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(grandparent.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(greatGrandparent.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(greatGreatGrandparent.getId())))
            .thenReturn(null);

        String errorMessage = reviewer.getIcsError(child);
        assertNotNull(errorMessage);
        assertEquals(ERROR_MESSAGE, errorMessage);
    }

    @Test
    public void review_listingWithFiveGenerations_noSurveillance_twoGenerationsHaveErrors() {
        CertifiedProductDTO greatGreatGreatGrandparent = CertifiedProductDTO.builder()
                .id(1L)
                .chplProductNumber("15.02.05.1439.A111.01.00.1.200219")
                .build();
        CertifiedProductDTO greatGreatGrandparent = CertifiedProductDTO.builder()
                .id(2L)
                .chplProductNumber("15.02.05.1439.A111.01.01.1.200219")
                .build();
        CertifiedProductDTO greatGrandparent = CertifiedProductDTO.builder()
                .id(3L)
                .chplProductNumber("15.02.05.1439.A111.01.02.1.200219")
                .build();
        CertifiedProductDTO grandparent = CertifiedProductDTO.builder()
                .id(4L)
                .chplProductNumber("15.02.05.1439.A111.01.03.1.200219")
                .build();
        CertifiedProductDTO parent = CertifiedProductDTO.builder()
                .id(5L)
                .chplProductNumber("15.02.05.1439.A111.01.04.1.200219")
                .build();
        CertifiedProductSearchDetails parentDetails = CertifiedProductSearchDetails.builder()
                .id(5L)
                .chplProductNumber("15.02.05.1439.A111.01.04.1.200219")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .build())
                .build();
        CertifiedProductSearchDetails child = CertifiedProductSearchDetails.builder()
                .id(6L)
                .chplProductNumber("15.02.05.1439.A111.01.05.1.200219")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .build())
                .build();

        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(child.getId())))
            .thenReturn(Stream.of(parent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(parent.getId())))
            .thenReturn(Stream.of(grandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(grandparent.getId())))
            .thenReturn(Stream.of(greatGrandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(greatGrandparent.getId())))
            .thenReturn(Stream.of(greatGreatGrandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(greatGreatGrandparent.getId())))
            .thenReturn(Stream.of(greatGreatGreatGrandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(greatGreatGreatGrandparent.getId())))
            .thenReturn(null);

        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(child.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(parent.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(grandparent.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(greatGrandparent.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(greatGreatGrandparent.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(greatGreatGreatGrandparent.getId())))
            .thenReturn(null);

        String errorMessage = reviewer.getIcsError(child);
        assertNotNull(errorMessage);
        assertEquals(ERROR_MESSAGE, errorMessage);

        errorMessage = reviewer.getIcsError(parentDetails);
        assertNotNull(errorMessage);
        assertEquals(ERROR_MESSAGE, errorMessage);
    }

    @Test
    public void review_listingWithFourGenerations_hasRwtSurveillance_hasError() {
        CertifiedProductDTO greatGreatGrandparent = CertifiedProductDTO.builder()
                .id(1L)
                .chplProductNumber("15.02.05.1439.A111.01.00.1.200219")
                .build();
        CertifiedProductDTO greatGrandparent = CertifiedProductDTO.builder()
                .id(2L)
                .chplProductNumber("15.02.05.1439.A111.01.01.1.200219")
                .build();
        CertifiedProductDTO grandparent = CertifiedProductDTO.builder()
                .id(3L)
                .chplProductNumber("15.02.05.1439.A111.01.02.1.200219")
                .build();
        CertifiedProductDTO parent = CertifiedProductDTO.builder()
                .id(4L)
                .chplProductNumber("15.02.05.1439.A111.01.03.1.200219")
                .build();
        CertifiedProductSearchDetails child = CertifiedProductSearchDetails.builder()
                .id(5L)
                .chplProductNumber("15.02.05.1439.A111.01.04.1.200219")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .build())
                .build();

        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(child.getId())))
            .thenReturn(Stream.of(parent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(parent.getId())))
            .thenReturn(Stream.of(grandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(grandparent.getId())))
            .thenReturn(Stream.of(greatGrandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(greatGrandparent.getId())))
            .thenReturn(Stream.of(greatGreatGrandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(greatGreatGrandparent.getId())))
            .thenReturn(null);

        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(child.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(parent.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(grandparent.getId())))
            .thenReturn(Stream.of(Surveillance.builder()
                    .id(1L)
                    .requirements(Stream.of(SurveillanceRequirement.builder()
                            .id(1L)
                            .requirementTypeOther("Annual Real World Testing Plan")
                            .build())
                            .collect(Collectors.toCollection(LinkedHashSet::new)))
                    .build())
                    .toList());
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(greatGrandparent.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(greatGreatGrandparent.getId())))
            .thenReturn(null);

        String errorMessage = reviewer.getIcsError(child);
        assertNotNull(errorMessage);
        assertEquals(ERROR_MESSAGE, errorMessage);
    }

    @Test
    public void review_listingWithFourGenerations_hasIcsSurveillanceOnSelf_noError() {
        CertifiedProductDTO greatGreatGrandparent = CertifiedProductDTO.builder()
                .id(1L)
                .chplProductNumber("15.02.05.1439.A111.01.00.1.200219")
                .build();
        CertifiedProductDTO greatGrandparent = CertifiedProductDTO.builder()
                .id(2L)
                .chplProductNumber("15.02.05.1439.A111.01.01.1.200219")
                .build();
        CertifiedProductDTO grandparent = CertifiedProductDTO.builder()
                .id(3L)
                .chplProductNumber("15.02.05.1439.A111.01.02.1.200219")
                .build();
        CertifiedProductDTO parent = CertifiedProductDTO.builder()
                .id(4L)
                .chplProductNumber("15.02.05.1439.A111.01.03.1.200219")
                .build();
        CertifiedProductSearchDetails child = CertifiedProductSearchDetails.builder()
                .id(5L)
                .chplProductNumber("15.02.05.1439.A111.01.04.1.200219")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .build())
                .build();

        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(child.getId())))
            .thenReturn(Stream.of(parent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(parent.getId())))
            .thenReturn(Stream.of(grandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(grandparent.getId())))
            .thenReturn(Stream.of(greatGrandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(greatGrandparent.getId())))
            .thenReturn(Stream.of(greatGreatGrandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(greatGreatGrandparent.getId())))
            .thenReturn(null);

        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(child.getId())))
            .thenReturn(Stream.of(Surveillance.builder()
                .id(1L)
                .requirements(Stream.of(SurveillanceRequirement.builder()
                        .id(1L)
                        .requirementType(RequirementType.builder()
                                .title(ICS_REQUIREMENT_TITLE)
                                .build())
                        .build())
                        .collect(Collectors.toCollection(LinkedHashSet::new)))
                .build())
                .toList());
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(parent.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(grandparent.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(greatGrandparent.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(greatGreatGrandparent.getId())))
            .thenReturn(null);

        String errorMessage = reviewer.getIcsError(child);
        assertNull(errorMessage);
    }

    @Test
    public void review_listingWithFourGenerations_hasIcsSurveillanceOnParent_noError() {
        CertifiedProductDTO greatGreatGrandparent = CertifiedProductDTO.builder()
                .id(1L)
                .chplProductNumber("15.02.05.1439.A111.01.00.1.200219")
                .build();
        CertifiedProductDTO greatGrandparent = CertifiedProductDTO.builder()
                .id(2L)
                .chplProductNumber("15.02.05.1439.A111.01.01.1.200219")
                .build();
        CertifiedProductDTO grandparent = CertifiedProductDTO.builder()
                .id(3L)
                .chplProductNumber("15.02.05.1439.A111.01.02.1.200219")
                .build();
        CertifiedProductDTO parent = CertifiedProductDTO.builder()
                .id(4L)
                .chplProductNumber("15.02.05.1439.A111.01.03.1.200219")
                .build();
        CertifiedProductSearchDetails child = CertifiedProductSearchDetails.builder()
                .id(5L)
                .chplProductNumber("15.02.05.1439.A111.01.04.1.200219")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .build())
                .build();

        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(child.getId())))
            .thenReturn(Stream.of(parent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(parent.getId())))
            .thenReturn(Stream.of(grandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(grandparent.getId())))
            .thenReturn(Stream.of(greatGrandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(greatGrandparent.getId())))
            .thenReturn(Stream.of(greatGreatGrandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(greatGreatGrandparent.getId())))
            .thenReturn(null);

        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(child.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(parent.getId())))
            .thenReturn(Stream.of(Surveillance.builder()
                .id(1L)
                .requirements(Stream.of(SurveillanceRequirement.builder()
                        .id(1L)
                        .requirementType(RequirementType.builder()
                                .title(ICS_REQUIREMENT_TITLE)
                                .build())
                        .build())
                        .collect(Collectors.toCollection(LinkedHashSet::new)))
                .build())
                .toList());
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(grandparent.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(greatGrandparent.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(greatGreatGrandparent.getId())))
            .thenReturn(null);

        String errorMessage = reviewer.getIcsError(child);
        assertNull(errorMessage);
    }

    @Test
    public void review_listingWithFourGenerations_hasIcsSurveillanceOnGrandparent_noError() {
        CertifiedProductDTO greatGreatGrandparent = CertifiedProductDTO.builder()
                .id(1L)
                .chplProductNumber("15.02.05.1439.A111.01.00.1.200219")
                .build();
        CertifiedProductDTO greatGrandparent = CertifiedProductDTO.builder()
                .id(2L)
                .chplProductNumber("15.02.05.1439.A111.01.01.1.200219")
                .build();
        CertifiedProductDTO grandparent = CertifiedProductDTO.builder()
                .id(3L)
                .chplProductNumber("15.02.05.1439.A111.01.02.1.200219")
                .build();
        CertifiedProductDTO parent = CertifiedProductDTO.builder()
                .id(4L)
                .chplProductNumber("15.02.05.1439.A111.01.03.1.200219")
                .build();
        CertifiedProductSearchDetails child = CertifiedProductSearchDetails.builder()
                .id(5L)
                .chplProductNumber("15.02.05.1439.A111.01.04.1.200219")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .build())
                .build();

        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(child.getId())))
            .thenReturn(Stream.of(parent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(parent.getId())))
            .thenReturn(Stream.of(grandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(grandparent.getId())))
            .thenReturn(Stream.of(greatGrandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(greatGrandparent.getId())))
            .thenReturn(Stream.of(greatGreatGrandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(greatGreatGrandparent.getId())))
            .thenReturn(null);

        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(child.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(parent.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(grandparent.getId())))
            .thenReturn(Stream.of(Surveillance.builder()
                    .id(1L)
                    .requirements(Stream.of(SurveillanceRequirement.builder()
                            .id(1L)
                            .requirementType(RequirementType.builder()
                                    .title(ICS_REQUIREMENT_TITLE)
                                    .build())
                            .build())
                            .collect(Collectors.toCollection(LinkedHashSet::new)))
                    .build())
                    .toList());
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(greatGrandparent.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(greatGreatGrandparent.getId())))
            .thenReturn(null);

        String errorMessage = reviewer.getIcsError(child);
        assertNull(errorMessage);
    }

    @Test
    public void review_listingWithFourGenerations_hasIcsSurveillanceOnGreatGrandParent_noError() {
        CertifiedProductDTO greatGreatGrandparent = CertifiedProductDTO.builder()
                .id(1L)
                .chplProductNumber("15.02.05.1439.A111.01.00.1.200219")
                .build();
        CertifiedProductDTO greatGrandparent = CertifiedProductDTO.builder()
                .id(2L)
                .chplProductNumber("15.02.05.1439.A111.01.01.1.200219")
                .build();
        CertifiedProductDTO grandparent = CertifiedProductDTO.builder()
                .id(3L)
                .chplProductNumber("15.02.05.1439.A111.01.02.1.200219")
                .build();
        CertifiedProductDTO parent = CertifiedProductDTO.builder()
                .id(4L)
                .chplProductNumber("15.02.05.1439.A111.01.03.1.200219")
                .build();
        CertifiedProductSearchDetails child = CertifiedProductSearchDetails.builder()
                .id(5L)
                .chplProductNumber("15.02.05.1439.A111.01.04.1.200219")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .build())
                .build();

        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(child.getId())))
            .thenReturn(Stream.of(parent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(parent.getId())))
            .thenReturn(Stream.of(grandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(grandparent.getId())))
            .thenReturn(Stream.of(greatGrandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(greatGrandparent.getId())))
            .thenReturn(Stream.of(greatGreatGrandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(greatGreatGrandparent.getId())))
            .thenReturn(null);

        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(child.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(parent.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(grandparent.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(greatGrandparent.getId())))
        .thenReturn(Stream.of(Surveillance.builder()
            .id(1L)
            .requirements(Stream.of(SurveillanceRequirement.builder()
                    .id(1L)
                    .requirementType(RequirementType.builder()
                            .title(ICS_REQUIREMENT_TITLE)
                            .build())
                    .build())
                    .collect(Collectors.toCollection(LinkedHashSet::new)))
            .build())
            .toList());
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(greatGreatGrandparent.getId())))
            .thenReturn(null);

        String errorMessage = reviewer.getIcsError(child);
        assertNull(errorMessage);
    }

    @Test
    public void review_listingWithFourGenerations_hasIcsSurveillanceOnGreatGreatGrandParent_hasError() {
        CertifiedProductDTO greatGreatGrandparent = CertifiedProductDTO.builder()
                .id(1L)
                .chplProductNumber("15.02.05.1439.A111.01.00.1.200219")
                .build();
        CertifiedProductDTO greatGrandparent = CertifiedProductDTO.builder()
                .id(2L)
                .chplProductNumber("15.02.05.1439.A111.01.01.1.200219")
                .build();
        CertifiedProductDTO grandparent = CertifiedProductDTO.builder()
                .id(3L)
                .chplProductNumber("15.02.05.1439.A111.01.02.1.200219")
                .build();
        CertifiedProductDTO parent = CertifiedProductDTO.builder()
                .id(4L)
                .chplProductNumber("15.02.05.1439.A111.01.03.1.200219")
                .build();
        CertifiedProductSearchDetails child = CertifiedProductSearchDetails.builder()
                .id(5L)
                .chplProductNumber("15.02.05.1439.A111.01.04.1.200219")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .build())
                .build();

        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(child.getId())))
            .thenReturn(Stream.of(parent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(parent.getId())))
            .thenReturn(Stream.of(grandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(grandparent.getId())))
            .thenReturn(Stream.of(greatGrandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(greatGrandparent.getId())))
            .thenReturn(Stream.of(greatGreatGrandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(greatGreatGrandparent.getId())))
            .thenReturn(null);

        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(child.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(parent.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(grandparent.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(greatGrandparent.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(greatGreatGrandparent.getId())))
        .thenReturn(Stream.of(Surveillance.builder()
            .id(1L)
            .requirements(Stream.of(SurveillanceRequirement.builder()
                    .id(1L)
                    .requirementType(RequirementType.builder()
                            .title(ICS_REQUIREMENT_TITLE)
                            .build())
                    .build())
                    .collect(Collectors.toCollection(LinkedHashSet::new)))
            .build())
            .toList());

        String errorMessage = reviewer.getIcsError(child);
        assertNotNull(errorMessage);
        assertEquals(ERROR_MESSAGE, errorMessage);
    }

    @Test
    public void review_listingWithFourGenerationsAndBranchingGrandparents_hasIcsSurveillance_noError() {
        CertifiedProductDTO greatGreatGrandparent = CertifiedProductDTO.builder()
                .id(1L)
                .chplProductNumber("15.02.05.1439.A111.01.00.1.200219")
                .build();
        CertifiedProductDTO greatGrandparent = CertifiedProductDTO.builder()
                .id(2L)
                .chplProductNumber("15.02.05.1439.A111.01.01.1.200219")
                .build();
        CertifiedProductDTO grandparent1 = CertifiedProductDTO.builder()
                .id(3L)
                .chplProductNumber("15.02.05.1439.A111.01.02.1.200219")
                .build();
        CertifiedProductDTO grandparent2 = CertifiedProductDTO.builder()
                .id(100L)
                .chplProductNumber("15.02.05.1439.A112.01.02.1.200219")
                .build();
        CertifiedProductDTO parent = CertifiedProductDTO.builder()
                .id(4L)
                .chplProductNumber("15.02.05.1439.A111.01.03.1.200219")
                .build();
        CertifiedProductSearchDetails child = CertifiedProductSearchDetails.builder()
                .id(5L)
                .chplProductNumber("15.02.05.1439.A111.01.04.1.200219")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .build())
                .build();

        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(child.getId())))
            .thenReturn(Stream.of(parent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(parent.getId())))
            .thenReturn(Stream.of(grandparent1, grandparent2).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(grandparent2.getId())))
            .thenReturn(Stream.of(greatGrandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(grandparent1.getId())))
            .thenReturn(null);
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(greatGrandparent.getId())))
            .thenReturn(Stream.of(greatGreatGrandparent).toList());
        Mockito.when(listingGraphDao.getParents(ArgumentMatchers.eq(greatGreatGrandparent.getId())))
            .thenReturn(null);

        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(child.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(parent.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(grandparent2.getId())))
            .thenReturn(Stream.of(Surveillance.builder()
                    .id(1L)
                    .requirements(Stream.of(SurveillanceRequirement.builder()
                            .id(1L)
                            .requirementType(RequirementType.builder()
                                    .title(ICS_REQUIREMENT_TITLE)
                                    .build())
                            .build())
                            .collect(Collectors.toCollection(LinkedHashSet::new)))
                    .build())
                    .toList());
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(greatGrandparent.getId())))
            .thenReturn(null);
        Mockito.when(survManager.getByCertifiedProduct(ArgumentMatchers.eq(greatGreatGrandparent.getId())))
            .thenReturn(null);

        String errorMessage = reviewer.getIcsError(child);
        assertNull(errorMessage);
    }
}

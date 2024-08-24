package gov.healthit.chpl.certifiedproduct.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.certificationCriteria.CertificationCriteriaManager;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.standard.StandardManager;
import gov.healthit.chpl.util.DateUtil;

public class ListingCsvDataWriterTest {

    private ListingCsvDataWriter writer;

    @Before
    public void setup() {
        CertificationCriteriaManager criteriaManager = Mockito.mock(CertificationCriteriaManager.class);
        StandardManager standardManager = Mockito.mock(StandardManager.class);
        Mockito.when(standardManager.getStandardsByCriteria(ArgumentMatchers.anyLong())).thenReturn(List.of());

        writer = new ListingCsvDataWriter(criteriaManager, standardManager);
    }

    @Test
    public void writeListingBasicData() {
        HashMap<String, Object> acb = new HashMap<String, Object>();
        acb.put(CertifiedProductSearchDetails.ACB_NAME_KEY, "Katy");

        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .chplProductNumber("15.05.05.3121.ONEL.01.00.1.220823")
                .developer(Developer.builder()
                        .name("Dev")
                        .build())
                .product(Product.builder()
                        .name("Prod")
                        .build())
                .version(ProductVersion.builder()
                        .version("1")
                        .build())
                .certifyingBody(acb)
                .certificationEvents(Stream.of(CertificationStatusEvent.builder()
                        .eventDate(DateUtil.toDate(LocalDate.of(2024, 8, 1)).getTime())
                        .status(CertificationStatus.builder()
                                .id(1L)
                                .name(CertificationStatusType.Active.getName())
                                .build())
                        .build())
                        .collect(Collectors.toList()))
                .build();
        List<List<String>> rows = writer.getCsvData(listing, 100);
        assertEquals(1, rows.size());
        assertEquals("15.05.05.3121.ONEL.01.00.1.220823", rows.get(0).get(0));
        assertEquals("Dev", rows.get(0).get(1));
        assertEquals("Prod", rows.get(0).get(2));
        assertEquals("1", rows.get(0).get(3));
        assertEquals("", rows.get(0).get(4));
        assertEquals("", rows.get(0).get(5));
        assertEquals("Katy", rows.get(0).get(9));
        assertEquals("", rows.get(0).get(10));
    }
}

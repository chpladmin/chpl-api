package gov.healthit.chpl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.ChplProductNumberDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.TestingLabDAO;

public class ChplProductNumberUtilTest {

    private TestingLabDAO testingLabDao;
    private CertificationBodyDAO certBodyDao;
    private DeveloperDAO developerDao;
    private CertifiedProductSearchResultDAO certifiedProductSearchResultDao;
    private ChplProductNumberDAO chplProductNumberDao;
    private CertifiedProductDAO cpDao;
    private ChplProductNumberUtil chplProductNumberUtil;

    @Before
    public void before() {
        testingLabDao = Mockito.mock(TestingLabDAO.class);
        certBodyDao = Mockito.mock(CertificationBodyDAO.class);
        developerDao = Mockito.mock(DeveloperDAO.class);
        certifiedProductSearchResultDao = Mockito.mock(CertifiedProductSearchResultDAO.class);
        chplProductNumberDao = Mockito.mock(ChplProductNumberDAO.class);
        cpDao = Mockito.mock(CertifiedProductDAO.class);
        chplProductNumberUtil = new ChplProductNumberUtil(testingLabDao, certBodyDao, developerDao,
                certifiedProductSearchResultDao, chplProductNumberDao, cpDao);
    }

    @Test
    public void parseAcbCode_ValidChplProductNumber_parsesCorrectly() {
        String acbCode = chplProductNumberUtil.getAcbCode("15.04.04.2669.MDTB.03.01.1.200707");
        assertNotNull(acbCode);
        assertEquals("04", acbCode);
    }

    @Test
    public void parseAcbCode_TooLongCode_parsesCorrectly() {
        String acbCode = chplProductNumberUtil.getAcbCode("15.04.0477.2669.MDTB.03.01.1.200707");
        assertNotNull(acbCode);
        assertEquals("0477", acbCode);
    }

    @Test
    public void parseAcbCode_TooShortCode_parsesCorrectly() {
        String acbCode = chplProductNumberUtil.getAcbCode("15.04.4.2669.MDTB.03.01.1.200707");
        assertNotNull(acbCode);
        assertEquals("4", acbCode);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void parseAcbCode_MissingPartBeforeAcb_throwsException() {
        chplProductNumberUtil.getAcbCode("03.04.2669.MDTB.03.01.1.200707");
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void parseAcbCode_MissingPartAfterAcb_throwsException() {
        chplProductNumberUtil.getAcbCode("15.03.04.MDTB.03.01.1.200707");
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void parseAcbCode_ExtraPartBeforeAcb_throwsException() {
        chplProductNumberUtil.getAcbCode("15.15.03.04.2669.MDTB.03.01.1.200707");
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void parseAcbCode_ExtraPartAfterAcb_throwsException() {
        chplProductNumberUtil.getAcbCode("15.03.04.2669.MDTB.03.01.1.200707.200707");
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void parseAcbCode_JunkChplProductNumber_throwsException() {
        chplProductNumberUtil.getAcbCode("JUNK");
    }
}

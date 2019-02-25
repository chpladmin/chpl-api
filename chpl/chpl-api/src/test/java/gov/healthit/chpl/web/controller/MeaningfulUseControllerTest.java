package gov.healthit.chpl.web.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.UnitTestUtil;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.MeaningfulUseUserRecord;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class MeaningfulUseControllerTest extends TestCase {
    private static final Logger logger = LogManager.getLogger(MeaningfulUseControllerTest.class);

    private static final String CSV_SEPARATOR = ",";

    @Autowired
    MeaningfulUseController meaningfulUseController;
    @Autowired
    CertifiedProductDAO cpDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    private static JWTAuthenticatedUser adminUser;

    @BeforeClass
    public static void setUpClass() throws Exception {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(UnitTestUtil.ADMIN_ID);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
    }

    /**
     * Given that a user with ROLE_ONC or ROLE_ADMIN has uploaded a CSV
     * with meaningfulUseUser counts (passed in as MultipartFile file) When the
     * UI calls the API at /uploadMeaningfulUse When the CSV contains
     * incorrectly named headers When the CSV contains some incorrect
     * CHPLProductNumbers When the CSV contains 2014 & 2015 edition CHPL Product
     * Numbers When the CSV contains a row with leading and trailing spaces for
     * CHPL Product Number When the CSV contains a duplicate CHPL Product Number
     * Then the API parses the contents of the csv Then the API updates the
     * meaningfulUseUser count for each CHPL Product Number/Certified Product
     * Then the API returns MeaningfulUseUserResults to the UI as a JSON
     * response Then the MeaningfulUseUserResults contains an array with
     * unsuccessful updates Then the API trims leading and trailing spaces and
     * successfully updates meaningfulUseUsers for the CHPLProductNumber Then
     * the duplicate CHPL Product Number results in an error added to the
     * results errors array Then the certifiedProductId is updated for non-error
     * results.
     *
     * @throws IOException
     * @throws JSONException
     */
    @Transactional
    @Rollback(true)
    @Test
    public void test_uploadMeaningfulUseUsers_returnsMeaningfulUseUserResults()
            throws EntityRetrievalException, EntityCreationException, IOException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        logger.info("Running test_uploadMeaningfulUseUsers_returnsMeaningfulUseUserResults");

        // Create CSV input for API
        MeaningfulUseUserRecord meaningfulUseUser1 = new MeaningfulUseUserRecord("CHP-024050", 10L); // MeaningfulUseUser
                                                                                                     // 0
        MeaningfulUseUserRecord meaningfulUseUser2 = new MeaningfulUseUserRecord("CHP-024051", 20L); // MeaningfulUseUser
                                                                                                     // 1
        MeaningfulUseUserRecord meaningfulUseUser3 = new MeaningfulUseUserRecord(" CHP-024052 ", 30L); // MeaningfulUseUser
                                                                                                       // 2
        MeaningfulUseUserRecord meaningfulUseUser4 = new MeaningfulUseUserRecord(" 15.01.01.1009.IC13.36.02.1.160402 ",
                40L); // MeaningfulUseUser 3
        MeaningfulUseUserRecord meaningfulUseUser5 = new MeaningfulUseUserRecord("wrongChplProductNumber", 50L); // Errors
                                                                                                                 // 0
        MeaningfulUseUserRecord meaningfulUseUser6 = new MeaningfulUseUserRecord(" CHPL-024053 ", 60L); // Errors
                                                                                                        // 1
        MeaningfulUseUserRecord meaningfulUseUser7 = new MeaningfulUseUserRecord("15.02.03.9876.AB01.01.00.1.123456",
                70L); // Errors 2
        MeaningfulUseUserRecord meaningfulUseUser8 = new MeaningfulUseUserRecord("15.01.01.1009.IC13.36.02.1.160402",
                70L); // Errors 3 (because duplicate of MeaningfulUseUser 3
        logger.info("Created 8 of MeaningfulUseUser to be updated in the database");

        List<MeaningfulUseUserRecord> meaningfulUseUserList = new ArrayList<MeaningfulUseUserRecord>();
        meaningfulUseUserList.add(meaningfulUseUser1);
        meaningfulUseUserList.add(meaningfulUseUser2);
        meaningfulUseUserList.add(meaningfulUseUser3);
        meaningfulUseUserList.add(meaningfulUseUser4);
        meaningfulUseUserList.add(meaningfulUseUser5);
        meaningfulUseUserList.add(meaningfulUseUser6);
        meaningfulUseUserList.add(meaningfulUseUser7);
        meaningfulUseUserList.add(meaningfulUseUser8);
        logger.info("Populated List of MeaningfulUseUser from 8 MeaningfulUseUser");

        File file = new File("testMeaningfulUseUsers.csv");

        try {
            BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file.getName()), "UTF-8"));
            StringBuffer headerLine = new StringBuffer();
            headerLine.append("cipl_product_number");
            headerLine.append(CSV_SEPARATOR);
            headerLine.append("n_meaningful_use");
            bw.write(headerLine.toString());
            bw.newLine();
            for (MeaningfulUseUserRecord muUser : meaningfulUseUserList) {
                StringBuffer oneLine = new StringBuffer();
                oneLine.append(muUser.getProductNumber() != null ? muUser.getProductNumber() : "");
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(muUser.getNumberOfUsers() != null ? muUser.getNumberOfUsers() : "");
                bw.write(oneLine.toString());
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (UnsupportedEncodingException e) {
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        logger.info("Wrote meaningfulUseUserList to testMeaningfulUseUsers.csv");

        FileInputStream input = new FileInputStream(file);
        logger.info("Create FileInputStream from csv file to populate MultipartFile");
        MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/csv",
                IOUtils.toByteArray(input));
        logger.info("Create & populate MultipartFile from csv file");

        try {
            meaningfulUseController.uploadMeaningfulUseUsers(multipartFile, System.currentTimeMillis());
        } catch (MaxUploadSizeExceededException e) {
            e.printStackTrace();
            fail();
        } catch (ValidationException e) {
            e.printStackTrace();
            fail();
        }

        input.close();
        file.delete();
    }

    /**
     * Given that a user with ROLE_ONC or ROLE_ADMIN has uploaded a CSV
     * with meaningfulUseUser counts (passed in as MultipartFile file) When the
     * UI calls the API at /uploadMeaningfulUse When the CSV contains no header
     * When the CSV contains a 2014 CHPL Product Number with a non-legacy format
     * Then the API continues with updating the CHPL Product Numbers with their
     * respective num_meaningful_users
     *
     * @throws IOException
     * @throws JSONException
     */
    @Transactional
    @Rollback(true)
    @Test
    public void test_uploadMeaningfulUseUsers_noHeader_runsWithoutError()
            throws EntityRetrievalException, EntityCreationException, IOException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        logger.info("Running test_uploadMeaningfulUseUsers_returnsMeaningfulUseUserResults");

        // Create CSV input for API
        MeaningfulUseUserRecord meaningfulUseUser1 = new MeaningfulUseUserRecord("CHP-024050", 10L);
        MeaningfulUseUserRecord meaningfulUseUser2 = new MeaningfulUseUserRecord("CHP-024051", 20L);
        MeaningfulUseUserRecord meaningfulUseUser3 = new MeaningfulUseUserRecord("CHP-024052", 30L);
        // get some 2015 CHPL IDs we can use
        CertifiedProduct cp1 = new CertifiedProduct(cpDao.getDetailsById(17L));
        CertifiedProduct cp2 = new CertifiedProduct(cpDao.getDetailsById(15L));
        MeaningfulUseUserRecord meaningfulUseUser4 = new MeaningfulUseUserRecord(cp1.getChplProductNumber(), 40L);
        MeaningfulUseUserRecord meaningfulUseUser5 = new MeaningfulUseUserRecord(cp2.getChplProductNumber(), 50L);
        logger.info("Created 5 of MeaningfulUseUser to be updated in the database");

        List<MeaningfulUseUserRecord> meaningfulUseUserList = new ArrayList<MeaningfulUseUserRecord>();
        meaningfulUseUserList.add(meaningfulUseUser1);
        meaningfulUseUserList.add(meaningfulUseUser2);
        meaningfulUseUserList.add(meaningfulUseUser3);
        meaningfulUseUserList.add(meaningfulUseUser4);
        meaningfulUseUserList.add(meaningfulUseUser5);
        logger.info("Populated List of MeaningfulUseUser from 5 MeaningfulUseUser");

        File file = new File("testMeaningfulUseUsers.csv");

        try {
            BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file.getName()), "UTF-8"));
            for (MeaningfulUseUserRecord muUser : meaningfulUseUserList) {
                StringBuffer oneLine = new StringBuffer();
                oneLine.append(muUser.getProductNumber() != null ? muUser.getProductNumber() : "");
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(muUser.getNumberOfUsers() != null ? muUser.getNumberOfUsers() : "");
                bw.write(oneLine.toString());
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (UnsupportedEncodingException e) {
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        logger.info("Wrote meaningfulUseUserList to testMeaningfulUseUsers.csv");

        FileInputStream input = new FileInputStream(file);
        logger.info("Create FileInputStream from csv file to populate MultipartFile");
        MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/csv",
                IOUtils.toByteArray(input));
        logger.info("Create & populate MultipartFile from csv file");

        try {
            meaningfulUseController.uploadMeaningfulUseUsers(multipartFile, System.currentTimeMillis());
        } catch (MaxUploadSizeExceededException e) {
            e.printStackTrace();
            fail();
        } catch (ValidationException e) {
            e.printStackTrace();
            fail();
        }

        input.close();
        file.delete();
    }
}

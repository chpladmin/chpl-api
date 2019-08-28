package gov.healthit.chpl.dao.scheduler;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.scheduler.CheckableUrlDTO;
import gov.healthit.chpl.dto.scheduler.InheritanceErrorsReportDTO;
import gov.healthit.chpl.dto.scheduler.UrlType;
import gov.healthit.chpl.entity.scheduler.InheritanceErrorsReportEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * Methods supporting the gathering of all URLs and recording results.
 * @author kekey
 *
 */
@Repository("urlCheckerDao")
public class UrlCheckerDao extends BaseDAOImpl {

    /**
     * Get all the URLs in the system.
     * Also gets the last time each of those URLs was checked.
     * @return
     */
    public List<CheckableUrlDTO> getAllSystemUrls() {
        List<CheckableUrlDTO> result = new ArrayList<CheckableUrlDTO>();
        for (UrlType urlType : UrlType.values()) {
            switch (urlType) {
                case ACB:
                    //query from acb table
                    List<String> acbWebsites =
                        entityManager.createQuery("SELECT website FROM CertificationBodyEntity WHERE deleted = false")
                        .getResultList();
                    for (String website : acbWebsites) {
                        CheckableUrlDTO checkableUrl = new CheckableUrlDTO();
                        checkableUrl.setUrl(website);
                        checkableUrl.setUrlType(urlType);
                        result.add(checkableUrl);
                    }
                    break;
                case ATL:
                    //query from atl table
                    List<String> atlWebsites =
                        entityManager.createQuery("SELECT website FROM TestingLabEntity WHERE deleted = false")
                        .getResultList();
                    for (String website : atlWebsites) {
                        CheckableUrlDTO checkableUrl = new CheckableUrlDTO();
                        checkableUrl.setUrl(website);
                        checkableUrl.setUrlType(urlType);
                        result.add(checkableUrl);
                    }
                    break;
                case DEVELOPER:
                    //query from developer table
                    List<String> developerWebsites =
                        entityManager.createQuery("SELECT website FROM DeveloperEntity WHERE deleted = false")
                        .getResultList();
                    for (String website : developerWebsites) {
                        CheckableUrlDTO checkableUrl = new CheckableUrlDTO();
                        checkableUrl.setUrl(website);
                        checkableUrl.setUrlType(urlType);
                        result.add(checkableUrl);
                    }
                    break;
                case FULL_USABILITY_REPORT:
                case MANDATORY_DISCLOSURE_URL:
                case TEST_RESULTS_SUMMARY:
                    //query from cp table
                    List<Object[]> listingWebsites =
                        entityManager.createQuery(
                                "SELECT transparencyAttestationUrl, reportFileLocation, sedReportFileLocation "
                                + "FROM CertifiedProductEntity WHERE deleted = false")
                        .getResultList();
                    for (Object[] websites : listingWebsites) {
                        if (websites.length > 0) {
                        CheckableUrlDTO checkableUrl = new CheckableUrlDTO();
                        checkableUrl.setUrl(website);
                        checkableUrl.setUrlType(urlType);
                        result.add(checkableUrl);
                        }
                    }
                    break;
            }
        }
        return result;
    }
}

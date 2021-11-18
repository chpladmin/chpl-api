package gov.healthit.chpl.scheduler.job.developer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Query;
import javax.transaction.Transactional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ff4j.FF4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.UserDeveloperMapDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntity;
import gov.healthit.chpl.manager.InvitationManager;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "inviteDevelopersJobLogger")
public class InviteDevelopersJob implements Job {

    @Autowired
    private InvitationManager invitationManager;

    @Autowired
    private DeveloperDAO developerDao;

    @Autowired
    private DeveloperListingMapDao developerListingMapDao;

    @Autowired
    private UserDeveloperMapDAO userDeveloperMapDao;

    @Autowired
    private FF4j ff4j;

    private int emailCount = 0;

    @SuppressWarnings("checkstyle:linelength")
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        if (!ff4j.check(FeatureList.ROLE_DEVELOPER)) {
            LOGGER.fatal("The " + FeatureList.ROLE_DEVELOPER + " flag is OFF. Exiting job.");
            return;
        }

        LOGGER.info("********* Starting the Invite Developers job *********");
        try {
            List<DeveloperDTO> allDevelopers = developerDao.findAll();
            LOGGER.info("There are " + allDevelopers.size() + " in the system.");
            allDevelopers.stream()
                .peek(developer -> LOGGER.info("Processing Developer '" + developer.getName() + "' (id: " + developer.getId() + ")."))
                .filter(developer -> doesDeveloperHaveAnyActiveListings(developer))
                .filter(developer -> !doesDeveloperHaveUserAccounts(developer))
                .forEach(developerWithoutAccount -> inviteDeveloperPoc(developerWithoutAccount));
        } catch (Exception e) {
            LOGGER.catching(e);
        } finally {
            LOGGER.info("********* Completed the Invite Developers job *********");
        }
    }

    private boolean doesDeveloperHaveAnyActiveListings(DeveloperDTO developer) {
        List<CertificationStatusType> certificationStatuses = Stream.of(CertificationStatusType.Active,
                CertificationStatusType.SuspendedByAcb,
                CertificationStatusType.SuspendedByOnc).collect(Collectors.toList());
        List<CertifiedProductDetailsDTO> activeListings
            = developerListingMapDao.getListingsForDeveloperWithStatus(developer.getId(), certificationStatuses);
        if (CollectionUtils.isEmpty(activeListings)) {
            LOGGER.info("\tDeveloper '" + developer.getName() + "' (id: " + developer.getId() + ") has NO active listings.");
        } else {
            LOGGER.info("\tDeveloper '" + developer.getName() + "' (id: " + developer.getId() + ") has " + activeListings.size() + " active listings.");
        }
        return !CollectionUtils.isEmpty(activeListings);
    }

    private boolean doesDeveloperHaveUserAccounts(DeveloperDTO developer) {
        List<UserDeveloperMapDTO> userDeveloperMaps = userDeveloperMapDao.getByDeveloperId(developer.getId());
        LOGGER.info(String.format("\tDeveloper '" + developer.getName() + "' (id: "
                + developer.getId() + ") has " + userDeveloperMaps.size() + " user%s",
                userDeveloperMaps.size() == 1 ? "" : "s"));
        return !CollectionUtils.isEmpty(userDeveloperMaps);
    }

    private void inviteDeveloperPoc(DeveloperDTO developer) {
        if (developer.getContact() == null || StringUtils.isEmpty(developer.getContact().getEmail())) {
            LOGGER.warn("\tDeveloper '" + developer.getName() + "' (id: " + developer.getId() + ") has no POC. No invitation can be sent.");
        } else {
            if (emailCount > 100 || developer.getContact().getEmail().contains("ainq.com")) {
                LOGGER.info("\tNot inviting user " + developer.getContact().getEmail() + " for developer '"
                        + developer.getName() + "' (id: " + developer.getId() + ").");
            } else {
                try {
                    setSecurityContext();
                    invitationManager.inviteWithDeveloperAccess(developer.getContact().getEmail(), developer.getId());
                    LOGGER.error("\tInvited user " + developer.getContact().getEmail() + " for developer '"
                            + developer.getName() + "' (id: " + developer.getId() + ").");
                    emailCount++;
                } catch (Exception ex) {
                    LOGGER.error("\tError inviting user " + developer.getContact().getEmail() + " for developer '"
                            + developer.getName() + "' (id: " + developer.getId() + ").", ex);
                }
            }
        }
    }

    private void setSecurityContext() {
        JWTAuthenticatedUser adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(User.ADMIN_USER_ID);
        adminUser.setFriendlyName("Admin");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));

        SecurityContextHolder.getContext().setAuthentication(adminUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @Component("developerListingMapDao")
    @NoArgsConstructor
    private static class DeveloperListingMapDao extends BaseDAOImpl {

        @Transactional
        public List<CertifiedProductDetailsDTO> getListingsForDeveloperWithStatus(Long developerId, List<CertificationStatusType> certificationStatuses) {
                String hql = "SELECT cpd "
                        + "FROM DeveloperEntity dev, CertifiedProductDetailsEntity cpd "
                        + "WHERE cpd.developerId = dev.id "
                        + "AND cpd.developerId = :developerId "
                        + "AND cpd.certificationStatusName IN (:certificationStatusNames) "
                        + "AND cpd.deleted = false ";
                Query query = entityManager.createQuery(hql, CertifiedProductDetailsEntity.class);
                List<String> certificationStatusNames = certificationStatuses.stream()
                        .map(CertificationStatusType::getName)
                        .collect(Collectors.toList());
                query.setParameter("developerId", developerId);
                query.setParameter("certificationStatusNames", certificationStatusNames);

                List<CertifiedProductDetailsEntity> queryResults = query.getResultList();
                if (queryResults == null || queryResults.size() == 0) {
                    return new ArrayList<CertifiedProductDetailsDTO>();
                }
                return queryResults.stream()
                        .map(entity -> new CertifiedProductDetailsDTO(entity))
                        .collect(Collectors.toList());
        }
    }
}

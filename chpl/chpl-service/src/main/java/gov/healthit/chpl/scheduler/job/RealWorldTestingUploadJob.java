package gov.healthit.chpl.scheduler.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.google.common.base.Objects;

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingType;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingUpload;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class RealWorldTestingUploadJob implements Job {
    private static final Logger LOGGER = LogManager.getLogger("realWorldTestingUploadJobLogger");
    public static final String JOB_NAME = "realWorldTestingUploadJob";
    public static final String RWT_UPLOAD_ITEMS = "realWorldTestingUploadItems";
    public static final String USER_KEY = "user";

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private CertifiedProductManager certifiedProductManager;

    @Autowired
    private ErrorMessageUtil errorMessageUtil;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Real World Testing Upload job. *********");
        UserDTO user = (UserDTO) context.getMergedJobDataMap().get(USER_KEY);
        setSecurityContext(user);


        List<RealWorldTestingUpload> rwts = (List<RealWorldTestingUpload>) context.getMergedJobDataMap().get(RWT_UPLOAD_ITEMS);
        LOGGER.info(rwts.size());

        for (RealWorldTestingUpload rwt : rwts) {
            rwt = processRwtUploadItem(rwt);
            LOGGER.info(rwt.toString());
            if (rwt.getErrors().size() == 0) {
                LOGGER.info("Successfully Updated");
            } else {
                for (String e : rwt.getErrors()) {
                    LOGGER.info(e);
                }
            }
        }
        LOGGER.info("********* Completed the Real World Testing Upload job. *********");
    }

    private RealWorldTestingUpload processRwtUploadItem(RealWorldTestingUpload rwtUpload) {
        rwtUpload.getErrors().addAll(validateRwtUpload(rwtUpload));
        if (rwtUpload.getErrors().size() == 0) {
            Optional<CertifiedProductSearchDetails> listing = getListing(rwtUpload.getChplProductNumber());
            if (listing.isPresent()) {
                if (hasDataChanged(listing.get(), rwtUpload)) {
                    rwtUpload = updateListing(listing.get(), rwtUpload);
                } else {
                    rwtUpload.getErrors().add(errorMessageUtil.getMessage("realWorldTesting.upload.dataNotChanged"));
                }
            } else {
                rwtUpload.getErrors().add(errorMessageUtil.getMessage("realWorldTesting.upload.listingNotFound"));
            }
        }
        return rwtUpload;
    }

    private RealWorldTestingUpload updateListing(CertifiedProductSearchDetails listing, RealWorldTestingUpload rwtUpload) {
        if (rwtUpload.getType().equals(RealWorldTestingType.PLANS)) {
            listing.setRwtPlansCheckDate(rwtUpload.getLastChecked());
            listing.setRwtPlansUrl(rwtUpload.getUrl());
        } else if (rwtUpload.getType().equals(RealWorldTestingType.RESULTS)) {
            listing.setRwtResultsCheckDate(rwtUpload.getLastChecked());
            listing.setRwtResultsUrl(rwtUpload.getUrl());
        }
        ListingUpdateRequest request = new ListingUpdateRequest();
        request.setAcknowledgeWarnings(true);
        request.setListing(listing);
        try {
            certifiedProductManager.update(request);
        } catch (ValidationException e) {
            rwtUpload.getErrors().addAll(e.getErrorMessages());
        } catch (Exception e) {
            rwtUpload.getErrors().add(e.getMessage());
        }
        return rwtUpload;
    }

    private boolean hasDataChanged(CertifiedProductSearchDetails listing, RealWorldTestingUpload rwtUpload) {
        if (rwtUpload.getType().equals(RealWorldTestingType.PLANS)) {
            return !(Objects.equal(listing.getRwtPlansUrl(), rwtUpload.getUrl())
                    && Objects.equal(listing.getRwtPlansCheckDate(), rwtUpload.getLastChecked()));
        } else if (rwtUpload.getType().equals(RealWorldTestingType.RESULTS)) {
            return !(Objects.equal(listing.getRwtResultsUrl(), rwtUpload.getUrl())
                    && Objects.equal(listing.getRwtResultsCheckDate(), rwtUpload.getLastChecked()));
        } else {
            return false;
        }
    }

    @SuppressWarnings("checkstyle:linelength")
    private Optional<CertifiedProductSearchDetails> getListing(String chplProductNumber) {
        try {
            return Optional.ofNullable(certifiedProductDetailsManager.getCertifiedProductDetailsBasicByChplProductNumber(chplProductNumber));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private List<String> validateRwtUpload(RealWorldTestingUpload rwtUpload) {
        List<String> errors = new ArrayList<String>();

        //Makes sure all 4 pieces of data are present
        if (StringUtils.isEmpty(rwtUpload.getChplProductNumber())) {
            errors.add(errorMessageUtil.getMessage("realWorldTesting.upload.chplProductNumberInvalid"));
        }
        if (rwtUpload.getType() == null) {
            errors.add(errorMessageUtil.getMessage("realWorldTesting.upload.realWorldTestingTypeInvalid"));
        }
        if (rwtUpload.getLastChecked() == null) {
            errors.add(errorMessageUtil.getMessage("realWorldTesting.upload.lastCheckedDateInvalid"));
        }
        if (StringUtils.isEmpty(rwtUpload.getUrl())) {
            errors.add(errorMessageUtil.getMessage("realWorldTesting.upload.url"));
        }
        return errors;
    }

    private void setSecurityContext(UserDTO user) {
        JWTAuthenticatedUser splitUser = new JWTAuthenticatedUser();
        splitUser.setFullName(user.getFullName());
        splitUser.setId(user.getId());
        splitUser.setFriendlyName(user.getFriendlyName());
        splitUser.setSubjectName(user.getUsername());
        splitUser.getPermissions().add(user.getPermission().getGrantedPermission());

        SecurityContextHolder.getContext().setAuthentication(splitUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

}

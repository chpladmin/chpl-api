package gov.healthit.chpl.scheduler.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.HtmlEmailTemplate;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.ValidationException;
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

    @Autowired
    private Environment env;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @SuppressWarnings("checkstyle:linelength")
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Real World Testing Upload job. *********");

        try {
            UserDTO user = (UserDTO) context.getMergedJobDataMap().get(USER_KEY);
            setSecurityContext(user);

            List<RealWorldTestingUpload> rwts = (List<RealWorldTestingUpload>) context.getMergedJobDataMap().get(RWT_UPLOAD_ITEMS);

            // Run some basic validation on upload records that do not have any errors
            // yet...
            rwts.stream().filter(rwt -> rwt.getValidationErrors().size() == 0)
                    .forEach(rwt -> rwt.getValidationErrors().addAll(validateRwtUpload(rwt)));

            // Determine if there multiple Plans or Results for the same listing. These will
            // not get
            // get processed, and will have an error added to the upload record.
            rwts = markMultipleChangesForSamePlanTypeAndListing(rwts);

            // Process the plans
            List<RealWorldTestingUpload> rwtPlans = rwts.stream().filter(
                    rwt -> rwt.getValidationErrors().size() == 0 && rwt.getType().equals(RealWorldTestingType.PLANS))
                    .collect(Collectors.toList());
            saveRealWorldTestingUploads(rwtPlans);

            // Process the results
            List<RealWorldTestingUpload> rwtResults = rwts.stream().filter(
                    rwt -> rwt.getValidationErrors().size() == 0 && rwt.getType().equals(RealWorldTestingType.RESULTS))
                    .collect(Collectors.toList());
            saveRealWorldTestingUploads(rwtResults);

            rwts.sort((rwt1, rwt2) -> rwt1.getOrder().compareTo(rwt2.getOrder()));

            sendResults(rwts, user.getEmail());

        } catch (Exception e) {
            LOGGER.catching(e);
        }

        LOGGER.info("********* Completed the Real World Testing Upload job. *********");
    }

    private List<RealWorldTestingUpload> saveRealWorldTestingUploads(List<RealWorldTestingUpload> rwts) {
        ExecutorService executorService = null;
        try {
            Integer threadPoolSize = getThreadCountForJob();
            executorService = Executors.newFixedThreadPool(threadPoolSize);

            List<CompletableFuture<RealWorldTestingUpload>> futures = new ArrayList<CompletableFuture<RealWorldTestingUpload>>();
            for (RealWorldTestingUpload rwt : rwts) {
                futures.add(CompletableFuture.supplyAsync(() -> processRwtUploadItem(rwt), executorService));
            }

            return futures.stream().map(f -> getRwtUploadFromFuture(f)).collect(Collectors.toList());
        } finally {
            executorService.shutdown();
        }
    }

    private RealWorldTestingUpload getRwtUploadFromFuture(CompletableFuture<RealWorldTestingUpload> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error(e);
            return null;
        }
    }

    @SuppressWarnings("checkstyle:linelength")
    private RealWorldTestingUpload processRwtUploadItem(RealWorldTestingUpload rwt) {
        LOGGER.info("Processing record: " + rwt.getChplProductNumber());
        Optional<CertifiedProductSearchDetails> listing = getListing(rwt.getChplProductNumber());

        if (listing.isPresent()) {
            if (rwt.getType().equals(RealWorldTestingType.PLANS) && !hasDataRwtPlansChanged(listing.get(), rwt)) {
                rwt.getValidationErrors().add(errorMessageUtil.getMessage("realWorldTesting.upload.dataNotChanged"));
            } else if (rwt.getType().equals(RealWorldTestingType.RESULTS)
                    && !hasDataRwtResultsChanged(listing.get(), rwt)) {
                rwt.getValidationErrors().add(errorMessageUtil.getMessage("realWorldTesting.upload.dataNotChanged"));
            }
            if (rwt.getValidationErrors().size() == 0) {
                rwt = updateListing(listing.get(), rwt);
            }
        } else {
            rwt.getValidationErrors().add(errorMessageUtil.getMessage("realWorldTesting.upload.listingNotFound"));
        }

        LOGGER.info("Completed processing record: " + rwt.getChplProductNumber());
        return rwt;
    }

    private RealWorldTestingUpload updateListing(CertifiedProductSearchDetails listing, RealWorldTestingUpload rwt) {
        try {
            rwt = addRwtDataToListing(listing, rwt);
            ListingUpdateRequest request = new ListingUpdateRequest();
            request.setAcknowledgeWarnings(true);
            request.setListing(listing);
            certifiedProductManager.update(request);
        } catch (ValidationException e) {
            rwt.getValidationErrors().addAll(e.getErrorMessages().castToCollection());
        } catch (Exception e) {
            rwt.getValidationErrors().add(e.getMessage());
        }
        return rwt;
    }

    private RealWorldTestingUpload addRwtDataToListing(CertifiedProductSearchDetails listing,
            RealWorldTestingUpload rwt) {
        if (rwt.getType().equals(RealWorldTestingType.PLANS)) {
            listing.setRwtPlansCheckDate(rwt.getLastChecked());
            listing.setRwtPlansUrl(rwt.getUrl());
        } else if (rwt.getType().equals(RealWorldTestingType.RESULTS)) {
            listing.setRwtResultsCheckDate(rwt.getLastChecked());
            listing.setRwtResultsUrl(rwt.getUrl());
        }
        return rwt;
    }

    private boolean hasDataRwtPlansChanged(CertifiedProductSearchDetails listing, RealWorldTestingUpload rwt) {
        return !(Objects.equals(listing.getRwtPlansUrl(), rwt.getUrl())
                && Objects.equals(listing.getRwtPlansCheckDate(), rwt.getLastChecked()));
    }

    private boolean hasDataRwtResultsChanged(CertifiedProductSearchDetails listing, RealWorldTestingUpload rwt) {
        return !(Objects.equals(listing.getRwtResultsUrl(), rwt.getUrl())
                && Objects.equals(listing.getRwtResultsCheckDate(), rwt.getLastChecked()));
    }

    @SuppressWarnings("checkstyle:linelength")
    private Optional<CertifiedProductSearchDetails> getListing(String chplProductNumber) {
        try {
            return Optional.ofNullable(certifiedProductDetailsManager.getCertifiedProductDetailsByChplProductNumber(chplProductNumber));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private List<String> validateRwtUpload(RealWorldTestingUpload rwtUpload) {
        List<String> errors = new ArrayList<String>();

        if (StringUtils.isEmpty(rwtUpload.getChplProductNumber())) {
            errors.add(errorMessageUtil.getMessage("realWorldTesting.upload.chplProductNumberInvalid"));
        }
        if (rwtUpload.getType() == null) {
            errors.add(errorMessageUtil.getMessage("realWorldTesting.upload.realWorldTestingTypeInvalid"));
        }
        return errors;
    }

    private void setSecurityContext(UserDTO user) {
        JWTAuthenticatedUser rwtUploadUser = new JWTAuthenticatedUser();
        rwtUploadUser.setFullName(user.getFullName());
        rwtUploadUser.setId(user.getId());
        rwtUploadUser.setFriendlyName(user.getFriendlyName());
        rwtUploadUser.setSubjectName(user.getUsername());
        rwtUploadUser.getPermissions().add(user.getPermission().getGrantedPermission());

        SecurityContextHolder.getContext().setAuthentication(rwtUploadUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    private void sendResults(List<RealWorldTestingUpload> rwts, String address) throws EmailNotSentException {
        RwtEmail rwtEmail = new RwtEmail(env);
        List<String> addresses = new ArrayList<String>(Arrays.asList(address));

        chplEmailFactory.emailBuilder().recipients(addresses).subject("Real World Testing Upload Results")
                .htmlMessage(rwtEmail.getEmail(rwts)).sendEmail();
    }

    private Integer getThreadCountForJob() throws NumberFormatException {
        return Integer.parseInt(env.getProperty("executorThreadCountForQuartzJobs"));
    }

    private List<RealWorldTestingUpload> markMultipleChangesForSamePlanTypeAndListing(
            List<RealWorldTestingUpload> rwts) {
        // Find any listings that have duplicate updates - multiple Plans
        rwts.stream()
                .filter(rwt -> rwt.getValidationErrors().size() == 0
                        && rwt.getType().equals(RealWorldTestingType.PLANS))
                .collect(Collectors.groupingBy(RealWorldTestingUpload::getChplProductNumber)).entrySet().stream()
                .filter(lst -> lst.getValue().size() > 1).forEach(lst -> lst.getValue().stream().forEach(
                        rwt -> rwt.getValidationErrors().add("Multiple Plans found for this CHPL Product Number")));

        rwts.stream()
                .filter(rwt -> rwt.getValidationErrors().size() == 0
                        && rwt.getType().equals(RealWorldTestingType.RESULTS))
                .collect(Collectors.groupingBy(RealWorldTestingUpload::getChplProductNumber)).entrySet().stream()
                .filter(lst -> lst.getValue().size() > 1).forEach(lst -> lst.getValue().stream().forEach(
                        rwt -> rwt.getValidationErrors().add("Multiple Results found for this CHPL Product Number")));
        return rwts;
    }

    private class RwtEmail {
        private Environment env;

        RwtEmail(Environment env) {
            this.env = env;
        }

        public String getEmail(List<RealWorldTestingUpload> rwts) {
            HtmlEmailTemplate email = new HtmlEmailTemplate();
            email.setStyles(getStyles());
            email.setBody(getBody(rwts));
            return email.build();
        }

        private String getBody(List<RealWorldTestingUpload> rwts) {
            StringBuilder table = new StringBuilder();

            table.append("<table class='blueTable'>\n");
            table.append("    <thead>\n");
            table.append("        <tr>\n");
            table.append("            <th>\n");
            table.append("                CHPL Product Number\n");
            table.append("            </th>\n");
            table.append("            <th>\n");
            table.append("                Type\n");
            table.append("            </th>\n");
            table.append("            <th>\n");
            table.append("                Last Checked Date\n");
            table.append("            </th>\n");
            table.append("            <th>\n");
            table.append("                URL\n");
            table.append("            </th>\n");
            table.append("            <th>\n");
            table.append("                Result or Errors\n");
            table.append("            </th>\n");
            table.append("        </tr>\n");
            table.append("    </thead>\n");
            table.append("    <tbody>\n");
            int i = 1;
            for (RealWorldTestingUpload rwt : rwts) {
                String trClass = i % 2 == 0 ? "even" : "odd";

                table.append("        <tr class=\"" + trClass + "\">\n");
                table.append("            <td>");
                table.append(rwt.getOriginalData().getChplProductNumber());
                table.append("            </td>\n");
                table.append("            <td>");
                table.append(rwt.getOriginalData().getType());
                table.append("            </td>\n");
                table.append("            <td>");
                table.append(rwt.getOriginalData().getLastChecked());
                table.append("            </td>\n");
                table.append("            <td>");
                table.append(rwt.getOriginalData().getUrl());
                table.append("            </td>\n");
                table.append("            <td>");
                table.append(getErrorsAsString(rwt));
                table.append("            </td>\n");
                table.append("        </tr>\n");
                ++i;
            }
            table.append("    </tbody>\n");
            table.append("</table>\n");

            return table.toString();
        }

        private String getErrorsAsString(RealWorldTestingUpload rwt) {
            if (rwt.getValidationErrors().size() > 0) {
                return rwt.getValidationErrors().stream().map(err -> !err.startsWith("WARNING") ? "ERROR: " + err : err)
                        .collect(Collectors.joining("<br/>"));
            } else {
                return "SUCCESS";
            }
        }

        private String getStyles() {
            return env.getProperty("email_styles");
        }
    }
}

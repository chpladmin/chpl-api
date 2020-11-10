package gov.healthit.chpl.scheduler.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

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
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingUpload;
import gov.healthit.chpl.util.EmailBuilder;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.HtmlEmailTemplate;
import lombok.Data;

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

    @SuppressWarnings("checkstyle:linelength")
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Real World Testing Upload job. *********");
        ExecutorService executorService = null;

        try {
            UserDTO user = (UserDTO) context.getMergedJobDataMap().get(USER_KEY);
            setSecurityContext(user);

            Integer threadPoolSize = getThreadCountForJob();
            executorService = Executors.newFixedThreadPool(threadPoolSize);

            List<RealWorldTestingUpload> rwts = (List<RealWorldTestingUpload>) context.getMergedJobDataMap().get(RWT_UPLOAD_ITEMS);

            //Run some basic validation on upload records that do not have any errors yet...
            rwts.stream()
                    .filter(rwt -> rwt.getValidationErrors().size() == 0)
                    .forEach(rwt -> rwt.getValidationErrors().addAll(validateRwtUpload(rwt)));

            //Determine if there multiple Plans or Results for the same listing.  These will not get
            //get processed, and will have an error added to the upload record.
            rwts = markMultipleChangesForSamePlanTypeAndListing(rwts);

            //Create an Combined Upload Transaction object.  This pulls the Plans and Results for a listing into
            //a single object, allowing us to perform a single update for each listing, rather than an update
            //for each row in the upload file
            List<CombinedUploadTransaction> txns = getMergedRwtUploads(rwts.stream()
                    .collect(Collectors.toList()));

            //Process the list of combined transactions
            List<CompletableFuture<CombinedUploadTransaction>> futures = new ArrayList<CompletableFuture<CombinedUploadTransaction>>();
            for (CombinedUploadTransaction txn : txns) {
                futures.add(CompletableFuture.supplyAsync(() -> processRwtUploadItem(txn), executorService));
            }

            txns = futures.stream()
                    .map(f -> getRwtUploadFromFuture(f))
                    .collect(Collectors.toList());

            sendResults(txns, user.getEmail());

        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            executorService.shutdown();
        }

        LOGGER.info("********* Completed the Real World Testing Upload job. *********");
    }

    private CombinedUploadTransaction getRwtUploadFromFuture(CompletableFuture<CombinedUploadTransaction> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error(e);
            return null;
        }
    }

    @SuppressWarnings("checkstyle:linelength")
    private CombinedUploadTransaction processRwtUploadItem(CombinedUploadTransaction txn) {
        LOGGER.info("Processing record: " + txn.getChplProductNumber());
        Optional<CertifiedProductSearchDetails> listing = getListing(txn.getChplProductNumber());

        if (listing.isPresent()) {
            if (!hasDataRwtPlansChanged(listing.get(), txn)) {
                txn.getPlansUpload().getValidationErrors().add(errorMessageUtil.getMessage("realWorldTesting.upload.dataNotChanged") + " (PLANS)");
            }
            if (!hasDataRwtResultsChanged(listing.get(), txn)) {
                txn.getResultsUpload().getValidationErrors().add(errorMessageUtil.getMessage("realWorldTesting.upload.dataNotChanged") + " (RESULTS)");
            }
            txn = updateListing(listing.get(), txn);
        } else {
            txn.getErrors().add(errorMessageUtil.getMessage("realWorldTesting.upload.listingNotFound"));
        }

        LOGGER.info("Completed processing record: " + txn.getChplProductNumber());
        return txn;
    }

    private CombinedUploadTransaction updateListing(CertifiedProductSearchDetails listing, CombinedUploadTransaction txn) {
        if (txn.getPlansUpload() != null && txn.getPlansUpload().getValidationErrors().size() == 0) {
            listing.setRwtPlansCheckDate(txn.getPlansUpload().getLastChecked());
            listing.setRwtPlansUrl(txn.getPlansUpload().getUrl());
        }
        if (txn.getResultsUpload() != null && txn.getResultsUpload().getValidationErrors().size() == 0) {
            listing.setRwtResultsCheckDate(txn.getResultsUpload().getLastChecked());
            listing.setRwtResultsUrl(txn.getResultsUpload().getUrl());
        }

        ListingUpdateRequest request = new ListingUpdateRequest();
        request.setAcknowledgeWarnings(true);
        request.setListing(listing);
        try {
            certifiedProductManager.update(request);
        } catch (ValidationException e) {
            txn.getErrors().addAll(e.getErrorMessages());
        } catch (Exception e) {
            txn.getErrors().add(e.getMessage());
        }
        return txn;
    }

    private boolean hasDataRwtPlansChanged(CertifiedProductSearchDetails listing, CombinedUploadTransaction txn) {
        if (txn.getPlansUpload() != null) {
            return !(Objects.equals(listing.getRwtPlansUrl(), txn.getPlansUpload().getUrl())
                    && Objects.equals(listing.getRwtPlansCheckDate(), txn.getPlansUpload().getLastChecked()));
        } else {
            return false;
        }
    }

    private boolean hasDataRwtResultsChanged(CertifiedProductSearchDetails listing, CombinedUploadTransaction txn) {
        if (txn.getResultsUpload() != null) {
            return !(Objects.equals(listing.getRwtResultsUrl(), txn.getResultsUpload().getUrl())
                    && Objects.equals(listing.getRwtResultsCheckDate(), txn.getResultsUpload().getLastChecked()));
        } else {
            return false;
        }
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

    private void sendResults(List<CombinedUploadTransaction> txns, String address) throws MessagingException {
        RwtEmail rwtEmail = new RwtEmail(env);
        List<String> addresses = new ArrayList<String>(Arrays.asList(address));

        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipients(addresses)
        .subject("Real World Testing Upload Results")
        .htmlMessage(rwtEmail.getEmail(txns))
        .sendEmail();
    }

    private Integer getThreadCountForJob() throws NumberFormatException {
        return Integer.parseInt(env.getProperty("executorThreadCountForQuartzJobs"));
    }

    private List<RealWorldTestingUpload> markMultipleChangesForSamePlanTypeAndListing(List<RealWorldTestingUpload> rwts) {
        //Find any listings that have duplicate updates - multiple Plans
        /*
        rwts.stream()
                .filter(rwt -> rwt.getErrors().size() == 0
                    && rwt.getType().equals(RealWorldTestingType.PLANS))
                .collect(Collectors.groupingBy(RealWorldTestingUpload::getChplProductNumber))
                .entrySet().stream()
                .filter(lst -> lst.getValue().size() > 1)
                .forEach(lst -> lst.getValue().stream()
                        .forEach(rwt -> rwt.getErrors().add("Multiple Plans found for this CHPL Product Number")));


        rwts.stream()
                .filter(rwt -> rwt.getErrors().size() == 0
                    && rwt.getType().equals(RealWorldTestingType.RESULTS))
                .collect(Collectors.groupingBy(RealWorldTestingUpload::getChplProductNumber))
                .entrySet().stream()
                .filter(lst -> lst.getValue().size() > 1)
                .forEach(lst -> lst.getValue().stream()
                    .forEach(rwt -> rwt.getErrors().add("Multiple Results found for this CHPL Product Number")));
        */
        return rwts;
    }

    private List<CombinedUploadTransaction> getMergedRwtUploads(List<RealWorldTestingUpload> rwts) {
        Map<String, CombinedUploadTransaction> txns = new HashMap<String, CombinedUploadTransaction>();

        for (RealWorldTestingUpload rwt : rwts) {
            if (!txns.containsKey(rwt.getChplProductNumber())) {
                CombinedUploadTransaction txn = new CombinedUploadTransaction(rwt.getChplProductNumber());
                txns.put(rwt.getChplProductNumber(), txn);
            }
            CombinedUploadTransaction txn = txns.get(rwt.getChplProductNumber());
            switch (rwt.getType()) {
            case PLANS:
                txn.setPlansUpload(rwt);
                break;
            case RESULTS:
                txn.setResultsUpload(rwt);
                break;
            default:
                break;
            }
        }

        return txns.values().stream()
                .collect(Collectors.toList());
    }

    @Data
    class CombinedUploadTransaction {
        private String chplProductNumber;
        private RealWorldTestingUpload plansUpload;
        private RealWorldTestingUpload resultsUpload;
        private List<String> errors = new ArrayList<String>();

        CombinedUploadTransaction(String chplProductNumber) {
            this.chplProductNumber = chplProductNumber;
        }
    }


    private class RwtEmail {
        private Environment env;

        RwtEmail(Environment env) {
            this.env = env;
        }

        public String getEmail(List<CombinedUploadTransaction> txns) {
            HtmlEmailTemplate email = new HtmlEmailTemplate();
            email.setStyles(getStyles());
            email.setBody(getBody(txns));
            return email.build();
        }

        private String getBody(List<CombinedUploadTransaction> txns) {
            StringBuilder table = new StringBuilder();

            table.append("<table class='blueTable'>\n");
            table.append("    <thead>\n");
            table.append("        <tr>\n");
            table.append("            <th>\n");
            table.append("                CHPL Product Number\n");
            table.append("            </th>\n");
            table.append("            <th>\n");
            table.append("                Plans Last Checked Date\n");
            table.append("            </th>\n");
            table.append("            <th>\n");
            table.append("                Plans URL\n");
            table.append("            </th>\n");
            table.append("            <th>\n");
            table.append("                Results Last Checked Date\n");
            table.append("            </th>\n");
            table.append("            <th>\n");
            table.append("                Results URL\n");
            table.append("            </th>\n");
            table.append("            <th>\n");
            table.append("                Result or Errors\n");
            table.append("            </th>\n");
            table.append("        </tr>\n");
            table.append("    </thead>\n");
            table.append("    <tbody>\n");
            int i = 1;
            for (CombinedUploadTransaction txn : txns) {
                String trClass = i % 2 == 0 ? "even" : "odd";

                table.append("        <tr class=\"" + trClass + "\">\n");
                table.append("            <td>");
                table.append(txn.getChplProductNumber());
                table.append("            </td>\n");
                table.append("            <td>");
                table.append(txn.getPlansUpload().getLastChecked());
                table.append("            </td>\n");
                table.append("            <td>");
                table.append(txn.getPlansUpload().getUrl());
                table.append("            </td>\n");
                table.append("            <td>");
                table.append(txn.getResultsUpload().getLastChecked());
                table.append("            </td>\n");
                table.append("            <td>");
                table.append(txn.getResultsUpload().getUrl());
                table.append("            </td>\n");
                table.append("            <td>");
                table.append(getErrorsAsString(txn));
                table.append("            </td>\n");
                table.append("        </tr>\n");
                ++i;
            }
            table.append("    </tbody>\n");
            table.append("</table>\n");

            return table.toString();
        }



        private String getErrorsAsString(CombinedUploadTransaction txn) {
            Set<String> errors = new HashSet<String>();
            errors.addAll(txn.getPlansUpload().getValidationErrors());
            errors.addAll(txn.getResultsUpload().getValidationErrors());
            errors.addAll(txn.getErrors());
            if (errors.size() > 0) {
                return StringUtils.join(errors, "<br/>");
            } else {
                return "Successfully updated.";
            }
        }

        private String getStyles() {
            return env.getProperty("rwt_email_styles");
        }
     }
}

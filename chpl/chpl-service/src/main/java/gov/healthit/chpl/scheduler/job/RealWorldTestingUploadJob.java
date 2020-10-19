package gov.healthit.chpl.scheduler.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
import gov.healthit.chpl.util.EmailBuilder;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.HtmlEmailTemplate;

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

            List<CompletableFuture<RealWorldTestingUpload>> futures = new ArrayList<CompletableFuture<RealWorldTestingUpload>>();
            for (RealWorldTestingUpload rwt : rwts) {
                futures.add(CompletableFuture.supplyAsync(() -> processRwtUploadItem(rwt), executorService));
            }

            rwts = futures.stream()
                    .map(f -> getRwtUploadFromFuture(f))
                    .collect(Collectors.toList());

            sendResults(rwts, user.getEmail());

        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            executorService.shutdown();
        }

        LOGGER.info("********* Completed the Real World Testing Upload job. *********");
    }

    private RealWorldTestingUpload getRwtUploadFromFuture(CompletableFuture<RealWorldTestingUpload> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error(e);
            return null;
        }
    }

    private RealWorldTestingUpload processRwtUploadItem(RealWorldTestingUpload rwtUpload) {
        LOGGER.info("Processing record: " + rwtUpload.toString());
        if (rwtUpload.getErrors().size() == 0) {
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
        }
        LOGGER.info("Completed processing record: " + rwtUpload.toString());
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
            return Optional.ofNullable(certifiedProductDetailsManager.getCertifiedProductDetailsByChplProductNumber(chplProductNumber));
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

    private void sendResults(List<RealWorldTestingUpload> rwts, String address) throws MessagingException {
        RwtEmail rwtEmail = new RwtEmail();
        List<String> addresses = new ArrayList<String>(Arrays.asList(address));

        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipients(addresses)
        .subject("Real World Testing Upload Results")
        .htmlMessage(rwtEmail.getEmail(rwts))
        .sendEmail();
    }

    private Integer getThreadCountForJob() throws NumberFormatException {
        return Integer.parseInt(env.getProperty("executorThreadCountForQuartzJobs"));
    }

    class RwtEmail {
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
                table.append(rwt.getChplProductNumber());
                table.append("            </td>\n");
                table.append("            <td>");
                table.append(rwt.getType());
                table.append("            </td>\n");
                table.append("            <td>");
                table.append(rwt.getLastChecked());
                table.append("            </td>\n");
                table.append("            <td>");
                table.append(rwt.getUrl());
                table.append("            </td>\n");
                table.append("            <td>");
                table.append(getErrorsAsString(rwt.getErrors()));
                table.append("            </td>\n");
                table.append("        </tr>\n");
                ++i;
            }
            table.append("    </tbody>\n");
            table.append("</table>\n");

            return table.toString();
        }


        private String getErrorsAsString(List<String> errors) {
            if (errors.size() > 0) {
                return StringUtils.join(errors, "<br/>");
            } else {
                return "Successfully updated.";
            }
        }

        private String getStyles() {
            StringBuilder style = new StringBuilder();

            style.append("table.blueTable {\n");
            style.append("    background-color: #EEEEEE;\n");
            style.append("    width: 100%;\n");
            style.append("    text-align: left;\n");
            style.append("    border-collapse: collapse;\n");
            style.append("}\n");
            style.append("}\n");
            style.append("table.blueTable tbody td {\n");
            style.append("    font-size: 13px;\n");
            style.append("}\n");
            style.append("table.blueTable tr.even {\n");
            style.append("    background: #D0E4F5;\n");
            style.append("}\n");
            style.append("table.blueTable tr.odd {\n");
            style.append("    background: #EEEEEE;\n");
            style.append("}\n");
            style.append("table.blueTable thead {\n");
            style.append("    background: #1C6EA4;\n");
            style.append("    border-bottom: 2px solid #444444;\n");
            style.append("}\n");
            style.append("table.blueTable thead th {\n");
            style.append("    font-size: 15px;\n");
            style.append("    font-weight: bold;\n");
            style.append("    color: #FFFFFF;\n");
            style.append("    border-left: 0px solid #D0E4F5;\n");
            style.append("}\n");
            style.append("table.blueTable thead th:first-child {\n");
            style.append("    border-left: none;\n");
            style.append("}\n");
            style.append("table.blueTable tfoot {\n");
            style.append("    font-size: 14px;\n");
            style.append("    font-weight: bold;\n");
            style.append("    color: #FFFFFF;\n");
            style.append("    background: #D0E4F5;\n");
            style.append("    background: -moz-linear-gradient(top, #dcebf7 0%, #d4e6f6 66%, #D0E4F5 100%);\n");
            style.append("    background: -webkit-linear-gradient(top, #dcebf7 0%, #d4e6f6 66%, #D0E4F5 100%);\n");
            style.append("    background: linear-gradient(to bottom, #dcebf7 0%, #d4e6f6 66%, #D0E4F5 100%);\n");
            style.append("    border-top: 2px solid #444444;\n");
            style.append("}\n");
            style.append("table.blueTable tfoot td {\n");
            style.append("    font-size: 14px;\n");
            style.append("}\n");
            style.append("table.blueTable tfoot .links {\n");
            style.append("    text-align: right;\n");
            style.append("}\n");
            style.append("table.blueTable tfoot .links a{\n");
            style.append("    display: inline-block;\n");
            style.append("    background: #1C6EA4;\n");
            style.append("    color: #FFFFFF;\n");
            style.append("    padding: 2px 8px;\n");
            style.append("    border-radius: 5px;\n");
            style.append("}\n");

            return style.toString();
        }
     }
}

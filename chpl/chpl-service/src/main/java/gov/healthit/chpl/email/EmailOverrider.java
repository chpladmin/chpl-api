package gov.healthit.chpl.email;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.env.Environment;

public class EmailOverrider {
    private Environment env;

    /**
     * Default constructor.
     * @param env - Spring environment
     */
    public EmailOverrider(final Environment env) {
        this.env = env;
    }

    /**
     * Returns the list of recipients.  If there is an address that is not allowed in the list,
     * the list is replaced with the "forward-to" address.
     * @param toAddresses - List of strings representing email addresses
     * @return - List of Address objects
     */
    public List<String> getRecipients(final List<String> toAddresses) {
        List<String> addresses = new ArrayList<String>();
        if (shouldEmailBeRedirected(toAddresses)) {
            String forwardToAddress = getForwardToEmail();
            addresses.add(forwardToAddress);
        } else {
            for (String address : toAddresses) {
                addresses.add(address);
            }
        }
        return addresses;
    }

    /**
     * Returns the message body. If there is an email address with a domain not in the allowed
     * list, the original list of recipients is prepended the message body to indicate
     * who the intended recipients of the email was.
     * @param htmlBody - the original HTML formatted message
     * @param toAddresses - List of Strings representing email addresses
     * @return - String representing the updated (if necessary) HTML message
     */
    public String getBody(String htmlBody, List<String> toAddresses) {
        StringBuffer message = new StringBuffer();

        if (shouldEmailBeRedirected(toAddresses)) {
            message.append("<b>");
            message.append("The intended recipients: ");
            message.append(getToAddressesAsString(toAddresses));
            message.append("</b>");
            message.append("<br/><br/>");
        }
        message.append(htmlBody);
        return message.toString();
    }

    /**
     * Determines whether or not an email should remain in the Sent Items account.
     * Only production-environment emails should be saved.
     * @return
     */
    public Boolean getSaveToSentItems() {
        return isProductionEmailEnvironment();
    }

    private String getToAddressesAsString(final List<String> toAddresses) {
        String addresses = "";
        for (String address : toAddresses) {
            if (!addresses.equals("")) {
                addresses += ", ";
            }
            addresses += address;

        }
        return addresses;
    }

    private Boolean shouldEmailBeRedirected(final List<String> toAddresses) {
        //ASSUMPTION:
        //If any of the recipients are not in the allowed domains, we are going to redirect the email.

        if (!isProductionEmailEnvironment()) {
            List<String> allowedDomains = getAllowedDomains();
            for (String address : toAddresses) {
                if (!allowedDomains.contains(getEmailDomain(address))) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<String> getAllowedDomains() {
        List<String> domains = new ArrayList<String>();
        String domainList =  env.getProperty("emailBuilder.config.allowedDomains");
        if (domainList != null) {
            domains.addAll(Arrays.asList(domainList.split(",")));
        }
        return domains;
    }

    private Boolean isProductionEmailEnvironment() {
        String isProductionEmailEnvironment = env.getProperty("emailBuilder.config.productionEnvironment");
        //We are going to assume that if the property was not found that we are in a PROD environment
        if (isProductionEmailEnvironment == null) {
            return true;
        } else {
            return Boolean.parseBoolean(isProductionEmailEnvironment);
        }
    }

    private String getEmailDomain(final String someEmail) {
        return  someEmail.substring(someEmail.indexOf("@") + 1);
    }

    private String getForwardToEmail() {
        return env.getProperty("emailBuilder_config_forwardAddress");
    }
}

package gov.healthit.chpl.scheduler;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;

import gov.healthit.chpl.scheduler.App;
import gov.healthit.chpl.auth.SendMailUtil;

public abstract class NotificationEmailerReportApp extends App {
    private SendMailUtil mailUtils;

    protected static final Logger LOGGER = LogManager.getLogger(NotificationEmailerReportApp.class);

    public NotificationEmailerReportApp() {
    }

    @Override
    protected void initiateSpringBeans(final AbstractApplicationContext context) throws IOException {
        this.setMailUtils((SendMailUtil) context.getBean("SendMailUtil"));
    }

    public SendMailUtil getMailUtils() {
        return mailUtils;
    }

    public void setMailUtils(final SendMailUtil mailUtils) {
        this.mailUtils = mailUtils;
    }
}

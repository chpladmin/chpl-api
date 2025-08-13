package gov.healthit.chpl.scheduler.job.changerequest.presenter;

import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestType;

public class DownloadableRwtPlansPresenter extends DownloadableListingUrlPresenter {

    public DownloadableRwtPlansPresenter(Logger logger) {
       super(logger);
    }

    protected boolean isSupported(ChangeRequest data) {
        return data.getChangeRequestType().getName().equalsIgnoreCase(ChangeRequestType.RWT_PLANS_TYPE);
    }
}

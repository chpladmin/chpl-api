package gov.healthit.chpl.scheduler.job.changerequest.presenter;

import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestType;

public class DownloadableSbulPresenter extends DownloadableListingUrlPresenter {

    public DownloadableSbulPresenter(Logger logger) {
       super(logger);
    }

    protected boolean isSupported(ChangeRequest data) {
        return data.getChangeRequestType().getName().equalsIgnoreCase(ChangeRequestType.SBUL_TYPE);
    }
}

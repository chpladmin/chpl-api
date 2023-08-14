package gov.healthit.chpl.scheduler.presenter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.env.Environment;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;

public class SurveillanceNonconformityCsvPresenter extends SurveillanceAllCsvPresenter {

    public SurveillanceNonconformityCsvPresenter(Environment env) {
        super(env);
        // TODO Auto-generated constructor stub
    }


    @Override
    @Override
    public synchronized void add(CertifiedProductSearchDetails data) throws IOException {
        if (data.getSurveillance() != null && data.getSurveillance().size() > 0) {
            for (Surveillance currSurveillance : data.getSurveillance()) {
                // note if this surveillance has any nonconformities
                boolean hasNc = false;
                if (currSurveillance.getRequirements() != null
                        && currSurveillance.getRequirements().size() > 0) {
                    // marks requirements for removal if they have no non-conformities
                    List<SurveillanceRequirement> reqsToRemove = new ArrayList<SurveillanceRequirement>();
                    for (SurveillanceRequirement req : currSurveillance.getRequirements()) {
                        if (req.getNonconformities() != null && req.getNonconformities().size() > 0) {
                            hasNc = true;
                        } else {
                            reqsToRemove.add(req);
                        }
                    }

                    // remove requirements without nonconformities
                    for (SurveillanceRequirement reqToRemove : reqsToRemove) {
                        currSurveillance.getRequirements().remove(reqToRemove);
                    }
                }

                if (hasNc) {
                    // write out surveillance with non-conformities only
                    List<List<String>> rowValues = generateMultiRowValue(data, currSurveillance);
                    for (List<String> rowValue : rowValues) {
                        csvPrinter.printRecord(rowValue);
                    }
                }
            }
        }

    }
}

package gov.healthit.chpl.quesitonableActivity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.MacraMeasure;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityCertificationResultDTO;

@Component
public class CertificationResultQuestionableActivityProvider {
    
    public QuestionableActivityCertificationResultDTO checkG1SuccessUpdated(
            CertificationResult origCertResult, CertificationResult newCertResult) {
        
        QuestionableActivityCertificationResultDTO activity = null;
        if (origCertResult.isG1Success() != null || newCertResult.isG1Success() != null) {
            if ((origCertResult.isG1Success() == null && newCertResult.isG1Success() != null) || 
                 (origCertResult.isG1Success() == Boolean.FALSE && newCertResult.isG1Success() == Boolean.TRUE)) {
                //g1 success changed to true
                activity = new QuestionableActivityCertificationResultDTO();
                activity.setMessage("TRUE");
            } else if ((origCertResult.isG1Success() != null && newCertResult.isG1Success() == null) || 
                    (origCertResult.isG1Success() == Boolean.TRUE && newCertResult.isG1Success() == Boolean.FALSE)) {
                //g1 success changed to false
                activity = new QuestionableActivityCertificationResultDTO();
                activity.setMessage("FALSE");
            }
        }
        return activity;
    }
    
    public QuestionableActivityCertificationResultDTO checkG2SuccessUpdated(
            CertificationResult origCertResult, CertificationResult newCertResult) {
        
        QuestionableActivityCertificationResultDTO activity = null;
        if (origCertResult.isG2Success() != null || newCertResult.isG2Success() != null) {
            if ((origCertResult.isG2Success() == null && newCertResult.isG2Success() != null) || 
                 (origCertResult.isG2Success() == Boolean.FALSE && newCertResult.isG2Success() == Boolean.TRUE)) {
                //g2 success changed to true
                activity = new QuestionableActivityCertificationResultDTO();
                activity.setMessage("TRUE");
            } else if ((origCertResult.isG2Success() != null && newCertResult.isG2Success() == null) || 
                    (origCertResult.isG2Success() == Boolean.TRUE && newCertResult.isG2Success() == Boolean.FALSE)) {
                //g2 success changed to false
                activity = new QuestionableActivityCertificationResultDTO();
                activity.setMessage("FALSE");
            }
        }
        
        return activity;
    }
    
    public QuestionableActivityCertificationResultDTO checkGapUpdated(
            CertificationResult origCertResult, CertificationResult newCertResult) {
        
        QuestionableActivityCertificationResultDTO activity = null;
        if (origCertResult.isGap() != null || newCertResult.isGap() != null) {
            if ((origCertResult.isGap() == null && newCertResult.isGap() != null) || 
                 (origCertResult.isGap() == Boolean.FALSE && newCertResult.isGap() == Boolean.TRUE)) {
                //gap changed to true
                activity = new QuestionableActivityCertificationResultDTO();
                activity.setMessage("TRUE");
            } else if ((origCertResult.isGap() != null && newCertResult.isGap() == null) || 
                    (origCertResult.isGap() == Boolean.TRUE && newCertResult.isGap() == Boolean.FALSE)) {
                //gap changed to false
                activity = new QuestionableActivityCertificationResultDTO();
                activity.setMessage("FALSE");
            }
        }
        return activity;
    }
    
    public List<QuestionableActivityCertificationResultDTO> checkG1MacraMeasuresAdded(
            CertificationResult origCertResult, CertificationResult newCertResult) {
        
        List<QuestionableActivityCertificationResultDTO> addedMacras = new ArrayList<QuestionableActivityCertificationResultDTO>();
        if ((origCertResult.getG1MacraMeasures() == null || origCertResult.getG1MacraMeasures().size() == 0) && 
                newCertResult.getG1MacraMeasures() != null && newCertResult.getG1MacraMeasures().size() > 0) {
            //all the newcert g1 macras are "added"
            for(MacraMeasure newMacra : newCertResult.getG1MacraMeasures()) {
                QuestionableActivityCertificationResultDTO activity = new QuestionableActivityCertificationResultDTO();
                activity.setMessage(newMacra.getAbbreviation());
                addedMacras.add(activity);
            }
        } else if (origCertResult.getG1MacraMeasures() != null && origCertResult.getG1MacraMeasures().size() > 0 && 
                newCertResult.getG1MacraMeasures() != null && newCertResult.getG1MacraMeasures().size() > 0) {
            for (MacraMeasure newMacra : newCertResult.getG1MacraMeasures()) {
                boolean hasMatch = false;
                for (MacraMeasure origMacra : origCertResult.getG1MacraMeasures()) {
                    if(newMacra.getId().longValue() == origMacra.getId().longValue()) {
                        hasMatch = true;
                    }
                }
                if(!hasMatch) {
                    QuestionableActivityCertificationResultDTO activity = new QuestionableActivityCertificationResultDTO();
                    activity.setMessage(newMacra.getAbbreviation());
                    addedMacras.add(activity);
                }
            }
        }
        return addedMacras;
    }
    
    public List<QuestionableActivityCertificationResultDTO> checkG1MacraMeasuresRemoved(
            CertificationResult origCertResult, CertificationResult newCertResult) {
        
        List<QuestionableActivityCertificationResultDTO> removedMacras = new ArrayList<QuestionableActivityCertificationResultDTO>();
        if ((newCertResult.getG1MacraMeasures() == null || newCertResult.getG1MacraMeasures().size() == 0) && 
                origCertResult.getG1MacraMeasures() != null && origCertResult.getG1MacraMeasures().size() > 0) {
            //all the origCert g1 macras are "removed"
            for(MacraMeasure newMacra : origCertResult.getG1MacraMeasures()) {
                QuestionableActivityCertificationResultDTO activity = new QuestionableActivityCertificationResultDTO();
                activity.setMessage(newMacra.getAbbreviation());
                removedMacras.add(activity);
            }
        } else if (origCertResult.getG1MacraMeasures() != null && origCertResult.getG1MacraMeasures().size() > 0 && 
                newCertResult.getG1MacraMeasures() != null && newCertResult.getG1MacraMeasures().size() > 0) {
            for (MacraMeasure origMacra : origCertResult.getG1MacraMeasures()) {
                boolean hasMatch = false;
                for (MacraMeasure newMacra : newCertResult.getG1MacraMeasures()) {
                    if(newMacra.getId().longValue() == origMacra.getId().longValue()) {
                        hasMatch = true;
                    }
                }
                if(!hasMatch) {
                    QuestionableActivityCertificationResultDTO activity = new QuestionableActivityCertificationResultDTO();
                    activity.setMessage(origMacra.getAbbreviation());
                    removedMacras.add(activity);
                }
            }
        }
        return removedMacras;
    }
    
    public List<QuestionableActivityCertificationResultDTO> checkG2MacraMeasuresAdded(
            CertificationResult origCertResult, CertificationResult newCertResult) {
        
        List<QuestionableActivityCertificationResultDTO> addedMacras = new ArrayList<QuestionableActivityCertificationResultDTO>();
        if ((origCertResult.getG2MacraMeasures() == null || origCertResult.getG2MacraMeasures().size() == 0) && 
                newCertResult.getG2MacraMeasures() != null && newCertResult.getG2MacraMeasures().size() > 0) {
            //all the newCert G2 macras are "added"
            for(MacraMeasure newMacra : newCertResult.getG2MacraMeasures()) {
                QuestionableActivityCertificationResultDTO activity = new QuestionableActivityCertificationResultDTO();
                activity.setMessage(newMacra.getAbbreviation());
                addedMacras.add(activity);
            }
        } else if (origCertResult.getG2MacraMeasures() != null && origCertResult.getG2MacraMeasures().size() > 0 && 
                newCertResult.getG2MacraMeasures() != null && newCertResult.getG2MacraMeasures().size() > 0) {
            for (MacraMeasure newMacra : newCertResult.getG2MacraMeasures()) {
                boolean hasMatch = false;
                for (MacraMeasure origMacra : origCertResult.getG2MacraMeasures()) {
                    if(newMacra.getId().longValue() == origMacra.getId().longValue()) {
                        hasMatch = true;
                    }
                }
                if(!hasMatch) {
                    QuestionableActivityCertificationResultDTO activity = new QuestionableActivityCertificationResultDTO();
                    activity.setMessage(newMacra.getAbbreviation());
                    addedMacras.add(activity);
                }
            }
        }
        return addedMacras;
    }
    
    public List<QuestionableActivityCertificationResultDTO> checkG2MacraMeasuresRemoved(
            CertificationResult origCertResult, CertificationResult newCertResult) {
        
        List<QuestionableActivityCertificationResultDTO> removedMacras = new ArrayList<QuestionableActivityCertificationResultDTO>();
        if ((newCertResult.getG2MacraMeasures() == null || newCertResult.getG2MacraMeasures().size() == 0) && 
                origCertResult.getG2MacraMeasures() != null && origCertResult.getG2MacraMeasures().size() > 0) {
            //all the origCert G2 macras are "removed"
            for(MacraMeasure newMacra : origCertResult.getG2MacraMeasures()) {
                QuestionableActivityCertificationResultDTO activity = new QuestionableActivityCertificationResultDTO();
                activity.setMessage(newMacra.getAbbreviation());
                removedMacras.add(activity);
            }
        } else if (origCertResult.getG2MacraMeasures() != null && origCertResult.getG2MacraMeasures().size() > 0 && 
                newCertResult.getG2MacraMeasures() != null && newCertResult.getG2MacraMeasures().size() > 0) {
            for (MacraMeasure origMacra : origCertResult.getG2MacraMeasures()) {
                boolean hasMatch = false;
                for (MacraMeasure newMacra : newCertResult.getG2MacraMeasures()) {
                    if(newMacra.getId().longValue() == origMacra.getId().longValue()) {
                        hasMatch = true;
                    }
                }
                if(!hasMatch) {
                    QuestionableActivityCertificationResultDTO activity = new QuestionableActivityCertificationResultDTO();
                    activity.setMessage(origMacra.getAbbreviation());
                    removedMacras.add(activity);
                }
            }
        }
        return removedMacras;
    }
}

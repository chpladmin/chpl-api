package gov.healthit.chpl.upload.certifiedProduct;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductEntity;
import gov.healthit.chpl.upload.certifiedProduct.template.TemplateColumnIndexMap;
import gov.healthit.chpl.upload.certifiedProduct.template.TemplateColumnIndexMap2015Version5;
import gov.healthit.chpl.util.ErrorMessageUtil;

/**
 * Removed 'removed' criteria, includes all Cures criteria, adds Self Developer
 *
 */
@Component("certifiedProductHandler2015Version5")
public class CertifiedProductHandler2015Version5 extends CertifiedProductHandler2015Version4 {
    private TemplateColumnIndexMap templateColumnIndexMap;

    @Autowired
    public CertifiedProductHandler2015Version5(final ErrorMessageUtil msgUtil) {
        super(msgUtil);
        templateColumnIndexMap = new TemplateColumnIndexMap2015Version5();
    }

    @Override
    public TemplateColumnIndexMap getColumnIndexMap() {
        return templateColumnIndexMap;
    }

    @Override
    protected void parseDeveloperDetails(PendingCertifiedProductEntity pendingCertifiedProduct,
            CSVRecord record) {
        int devStartIndex = getColumnIndexMap().getDeveloperStartIndex();
        String developerStreetAddress = record.get(devStartIndex++).trim();
        String developerState = record.get(devStartIndex++).trim();
        String developerCity = record.get(devStartIndex++).trim();
        String developerZipcode = record.get(devStartIndex++).trim();
        String developerWebsite = record.get(devStartIndex++).trim();
        Boolean selfDeveloper = asBoolean(record.get(devStartIndex++).trim());
        String developerEmail = record.get(devStartIndex++).trim();
        String developerPhone = record.get(devStartIndex++).trim();
        String developerContactName = record.get(devStartIndex++).trim();
        pendingCertifiedProduct.setDeveloperStreetAddress(developerStreetAddress);
        pendingCertifiedProduct.setDeveloperCity(developerCity);
        pendingCertifiedProduct.setDeveloperState(developerState);
        pendingCertifiedProduct.setDeveloperZipCode(developerZipcode);
        pendingCertifiedProduct.setDeveloperWebsite(developerWebsite);
        pendingCertifiedProduct.setSelfDeveloper(selfDeveloper);
        pendingCertifiedProduct.setDeveloperEmail(developerEmail);
        pendingCertifiedProduct.setDeveloperPhoneNumber(developerPhone);
        pendingCertifiedProduct.setDeveloperContactName(developerContactName);

        // look for contact in db
        ContactDTO contactToFind = new ContactDTO();
        contactToFind.setFriendlyName(developerContactName);
        contactToFind.setEmail(developerEmail);
        contactToFind.setPhoneNumber(developerPhone);
        ContactDTO foundContact = contactDao.getByValues(contactToFind);
        if (foundContact != null) {
            pendingCertifiedProduct.setDeveloperContactId(foundContact.getId());
        }
    }
}

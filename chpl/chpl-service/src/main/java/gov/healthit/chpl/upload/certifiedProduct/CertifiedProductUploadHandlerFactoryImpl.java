package gov.healthit.chpl.upload.certifiedProduct;

import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.UploadTemplateVersionDAO;
import gov.healthit.chpl.domain.concept.UploadTemplateVersion;
import gov.healthit.chpl.dto.UploadTemplateVersionDTO;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;

@Service
public class CertifiedProductUploadHandlerFactoryImpl implements CertifiedProductUploadHandlerFactory {
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private UploadTemplateVersionDAO templateVersionDao;
    @Autowired
    private CertifiedProductHandler2014 handler2014;
    @Autowired
    private CertifiedProductHandler2015Version1 handler2015Version1;
    @Autowired
    private CertifiedProductHandler2015Version2 handler2015Version2;

    private CertifiedProductUploadHandlerFactoryImpl() {
    }

    @Override
    public CertifiedProductUploadHandler getHandler(CSVRecord heading, List<CSVRecord> cpRecords)
            throws InvalidArgumentsException {
        if (heading == null || StringUtils.isEmpty(heading.toString())) {
            String msg = String
                    .format(messageSource.getMessage(new DefaultMessageSourceResolvable("listing.upload.badHeader"),
                            LocaleContextHolder.getLocale()), "an empty string");
            throw new InvalidArgumentsException(msg);
        }

        CertifiedProductUploadHandler handler = null;
        UploadTemplateVersionDTO templateVersion = null;
        String csvHeading = heading.toString().trim();

        // look at the header row as a CSV string and match it to one of the
        // header values that we have in the db.
        List<UploadTemplateVersionDTO> templateVersions = templateVersionDao.findAll();
        for (UploadTemplateVersionDTO currVersion : templateVersions) {
            if (currVersion.getHeaderCsv().equalsIgnoreCase(csvHeading)) {
                templateVersion = currVersion;
            }
        }

        // figure out which handler to use based on the template found
        if (templateVersion == null) {
            String msg = String
                    .format(messageSource.getMessage(new DefaultMessageSourceResolvable("listing.upload.badHeader"),
                            LocaleContextHolder.getLocale()), csvHeading);
            throw new InvalidArgumentsException(msg);
        } else if (templateVersion.getName().equals(UploadTemplateVersion.EDITION_2014_VERSION_1.getName())) {
            handler = handler2014;
        } else if (templateVersion.getName().equals(UploadTemplateVersion.EDITION_2015_VERSION_1.getName())) {
            handler = handler2015Version1;
        } else if (templateVersion.getName().equals(UploadTemplateVersion.EDITION_2015_VERSION_2.getName())) {
            handler = handler2015Version2;
        }

        int lastDataIndex = -1;
        for (int i = 0; i < heading.size() && lastDataIndex < 0; i++) {
            String headingValue = heading.get(i);
            if (StringUtils.isEmpty(headingValue)) {
                lastDataIndex = i - 1;
            } else if (i == heading.size() - 1) {
                lastDataIndex = i;
            }
        }
        handler.setRecord(cpRecords);
        handler.setHeading(heading);
        handler.setLastDataIndex(lastDataIndex);
        return handler;
    }
}

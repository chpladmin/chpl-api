package gov.healthit.chpl.upload.certifiedProduct;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.UploadTemplateVersionDAO;
import gov.healthit.chpl.domain.concept.UploadTemplateVersion;
import gov.healthit.chpl.dto.UploadTemplateVersionDTO;
import gov.healthit.chpl.exception.InvalidArgumentsException;

@Service
public final class CertifiedProductUploadHandlerFactoryImpl implements CertifiedProductUploadHandlerFactory {
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private UploadTemplateVersionDAO templateVersionDao;
    @Qualifier("certifiedProductHandler2014Version1")
    @Autowired
    private CertifiedProductHandler2014Version1 handler2014Version1;
    @Qualifier("certifiedProductHandler2014Version2")
    @Autowired
    private CertifiedProductHandler2014Version2 handler2014Version2;
    @Qualifier("certifiedProductHandler2015Version1")
    @Autowired
    private CertifiedProductHandler2015Version1 handler2015Version1;
    @Qualifier("certifiedProductHandler2015Version2")
    @Autowired
    private CertifiedProductHandler2015Version2 handler2015Version2;
    @Qualifier("certifiedProductHandler2015Version3")
    @Autowired
    private CertifiedProductHandler2015Version3 handler2015Version3;

    private CertifiedProductUploadHandlerFactoryImpl() {
    }

    @Override
    public CertifiedProductUploadHandler getHandler(final CSVRecord heading, final List<CSVRecord> cpRecords)
            throws InvalidArgumentsException {
        if (heading == null) {
            String msg = String
                    .format(messageSource.getMessage(new DefaultMessageSourceResolvable("listing.upload.badHeader"),
                            LocaleContextHolder.getLocale()), "null");
            throw new InvalidArgumentsException(msg);
        }

        //write out heading record as csv string
        //trim each column value
        List<String> trimmedHeaderVals = new ArrayList<String>(heading.size());
        for (int i = 0; i < heading.size(); i++) {
            String headerVal = heading.get(i).trim();
            trimmedHeaderVals.add(headerVal);
        }

        //it is common for a bunch of extra blank columns to get put at the end
        //of an xls/csv file without the user knowing, so delete any that are at the end
        //use a list iterator to go from the end of the list to tbe beginning
        //deleting blank columns from the end until we come to the first one that's
        //not blank, they we won't delete anymore
        ListIterator<String> headerValIter = trimmedHeaderVals.listIterator(trimmedHeaderVals.size());
        boolean foundLastColumnWithText = false;
        while (headerValIter.hasPrevious()) {
            String lastItem = headerValIter.previous();
            if (!foundLastColumnWithText) {
                if (StringUtils.isEmpty(lastItem)) {
                    headerValIter.remove();
                } else {
                    foundLastColumnWithText = true;
                }
            }
        }

        StringBuffer buf = new StringBuffer();
        try (CSVPrinter writer = new CSVPrinter(buf, CSVFormat.EXCEL.withRecordSeparator(""))) {
            //adding a blank char as the record separator prevents an \r\n char
            //from getting appended to the end of the string (\r\n at the end
            //would not match any of the options in the db)
            writer.printRecord(trimmedHeaderVals);
        } catch (IOException ex) {
            String msg = String
                    .format(messageSource.getMessage(new DefaultMessageSourceResolvable("listing.upload.badHeader"),
                            LocaleContextHolder.getLocale()), "a bad header value");
            throw new InvalidArgumentsException(msg);
        }

        String trimmedHeadingStr = buf.toString();
        if (trimmedHeadingStr == null || StringUtils.isEmpty(heading.toString())) {
            String msg = String
                    .format(messageSource.getMessage(new DefaultMessageSourceResolvable("listing.upload.badHeader"),
                            LocaleContextHolder.getLocale()), "an empty string");
            throw new InvalidArgumentsException(msg);
        }

        CertifiedProductUploadHandler handler = null;
        UploadTemplateVersionDTO templateVersion = null;

        // look at the header row as a CSV string and match it to one of the
        // header values that we have in the db.
        List<UploadTemplateVersionDTO> templateVersions = templateVersionDao.findAll();
        for (UploadTemplateVersionDTO currVersion : templateVersions) {
            if (currVersion.getHeaderCsv().equalsIgnoreCase(trimmedHeadingStr)) {
                templateVersion = currVersion;
            }
        }

        // figure out which handler to use based on the template found
        if (templateVersion == null) {
            String msg = String
                    .format(messageSource.getMessage(new DefaultMessageSourceResolvable("listing.upload.badHeader"),
                            LocaleContextHolder.getLocale()), trimmedHeadingStr);
            throw new InvalidArgumentsException(msg);
        } else if (templateVersion.getName().equals(UploadTemplateVersion.EDITION_2014_VERSION_1.getName())) {
            handler = handler2014Version1;
        } else if (templateVersion.getName().equals(UploadTemplateVersion.EDITION_2014_VERSION_2.getName())) {
            handler = handler2014Version2;
        } else if (templateVersion.getName().equals(UploadTemplateVersion.EDITION_2015_VERSION_1.getName())) {
            handler = handler2015Version1;
        } else if (templateVersion.getName().equals(UploadTemplateVersion.EDITION_2015_VERSION_2.getName())) {
            handler = handler2015Version2;
        } else if (templateVersion.getName().equals(UploadTemplateVersion.EDITION_2015_VERSION_3.getName())) {
            handler = handler2015Version3;
        }

        if (handler != null)  {
            handler.setRecord(cpRecords);
            handler.setHeading(heading);
            handler.setUploadTemplateVersion(templateVersion);
        }
        return handler;
    }
}

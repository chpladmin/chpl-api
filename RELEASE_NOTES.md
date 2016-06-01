# Release Notes

## Version 
_TBD_

### Features Added
* Change participant age to a range from a selectable list of ranges
* Changed 2015 and 2014/2015 Certification ID validation to no longer check CQMs
* Added more product details to Certification ID results
* Changed Additional Software in Certification ID results to be URL encoded
* Added feature to Certification ID generation to prevent formation of words in IDs 

### Bugs Fixed
* 170.314 (f)(3) does not require test tools for ambulatory products but does for inpatient
* Editing a product was requiring g1 and g2 when it should not have. 
* Trim spaces from the ends of all fields in the upload file
* Properly save test functionality and test tools if an invalid one was in the upload file but was edited to be a valid one during confirm.
* Fix logging bug when invalid column header is in upload file.

## Version 1.4.0
_16 May 2016_

### Features Added
* Added EHR Certification ID rest service
* Rearranged data for the corrective action plan reports/activity so there is just one activity event per CAP change.
* Added EHR Certification ID rest service verify operation

### Bugs fixed
* Fix parsing error when spreadsheet has only a single row.
* Only return last N days of activity as requested.
* Fix 2015 upload file validation to catch missing UCD Process, test tasks, and test participants.
* Fix met calculation for Certification ID 2015 Ambulatory CQM validation
* Save sed testing end date and sed intended users on confirm

---

## Version 1.3.1
_2 May 2016_

### Bugs fixed
* Increased 2015 column count to handle (a)(10) G2

## Version 1.3.0
_27 April 2016_

### Features Added
* Added fields to the corrective action plans
* Changed search parameter values for searching by corrective action plan (open, closed, none)

## Version 1.2.0
_20 April 2016_

### Features Added
* Moved developer information around into the standard object when returning product or pending product details

### Bugs fixed
* Fix 'null' in developer report when transparency attestation is changed.
* Standards tested against are now saved.
* Functionality tested against is now saved.
* Fix error related to merging of products.
* Product ownership change shows up correctly in activity report.`
* Do not allow duplicate chpl ids in the same file unless they have XXXX (new developers)

## Version 1.1.0
_12 April 2016_

### Features Added
* Test tools are not required for 2014 g1 and g2
* Test data is optional in 2015 for everything except g1 and g2.
* Improved education level name matching and error messages for education levels that are not in the database.
* Allow portions of CHPL Product Number to be changed.
* Allow ACB_ADMIN role to change the name of their own ACB.
* Add service for version activity
* Trigger emails on version name change, product name change, developer name change, or certified product status change

### Bugs fixed
* Test tool version is now saved.
* Fix activity report link email
* Allow certification date to be changed.

## Version 1.0.0
_30 March 2016_

### Features added
* Check for questionable certified product updates and trigger email if found.
* Return list of applicable transparency attestations for each developer

### Bugs fixed
* Allow for empty transparency attestation and url values

## Version 0.5.0
_25 March 2016_

### Features added
* API call added to check 'system is up'
* 2015 Upload process
* 2014 validation improved
* Email sending updated to conform with deployment

### Bugs fixed
* Searching on criteria alone gives error

## Version 0.4.0
_14 March 2016_

### Features added
* Add validation to 2014 products
* Update Download file generation
* Made Transparency Attestation an ENUM, URL per product
* Added "targeted users" to Certified Product
* Restrict uploads to CSV only
* Added CQM mappings to Certified Product
* Generate ATL/ACB codes if ATL/ACB is created

### Bugs fixed
* Fix null value in announcement activity

## Version 0.3.2
_29 February 2016_

### Features added
* Added ability to do new 2014 upload files

## Version 0.3.1
_22 February 2016_

### Bugs fixed
* Added missing product number to CP manange select box

## Version 0.3.0
_18 February 2016_

### Features added or Updated
* Changed Corrective Action Plan/Surveillance API parameters
* Added descriptions to API methods.
* Changed API for certified products and certification results to match 2014 data requirements.
* Allow multiple values for the 'hasCap' field in search.

## Version 0.2.0
_3 February 2016_

### Features added or Updated
* Set up swagger API and endpoint annotations
* Added code to create the XML download file
* Added code to allow a currently logged in user to get additional permissions if invited (/users/authorize)
* Adjust what happens when an ACB gets deleted. The ACB is marked as deleted and any users who were ONLY associated with that ACB (no other ACBs, no ATLs, no ADMIN role) will have their account disabled.
* Add service to undelete ACBs and ATLs accessed by /acbs/3/undelete or /atls/3/undelete
* Added service for announcements

### Bugs Fixed
* Allow certified products with errors to be edited and remove those errors
* Fix email content typos

## Version 0.1.1
_12 January 2016_

### New features
* Integrated Swagger API generation

### Bugs Fixed
* Search on certifications / cqms
* Allow editing of products with errors
* Fixed various email content issues

## Version 0.1.0
_5 January 2016_

Features added or Updated
* Added terms of use and api documentation to the certified product apis.
* Added ability to update transparencyAttestation field per vendor and ACB combination. ADMINs can update the transparencyAttestation for all vendor/ACB combinations and anyone else can only update that field for the ACBs to which they have access.
* Added transparencyAttestation to the fields that come back with certified product details and search results. It is inferred from the vendor/ACB mapping.
* Added APIs for testing labs, found under /atls urls. 
* Complete CHPL Product Number for 2015 products with testing lab code.

Bugs Fixed
* Correct CHPL number is inserted in corrective action plan documentation activity.
* Authorizing existing users for new roles or ACBs/ATLs is fixed 

## Version 0.0.2
_7 December 2015_

Features added or Updated
* Updated the format activity streams for users are returned.
* Updated search options to remove references to 2011 edition data in UI
* Added Api key requirement and Api key activity logging.
* Updated search to allow search by "hasCAP" (search by Corrective Action Plan)

Bugs fixed
* ETL inserts to correct 170.314 (b)(5)(A) or (b)(5)(B) depending on Ambulatory/Inpatient
* ETL correctly inserts CMS9 and CMS26 CQMs

## Version 0.0.1
_13 November 2015_

First release

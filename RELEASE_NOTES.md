# Release Notes

## Version TBD
_Date TBD_

### Features Added
* Updated the algorithm for calculating weekly aggregate counts in order to take into account deleted developers/products

---

## Version 5.1.2
_7 October 2016_

### Bugs Fixed
* Allowed searching by CQM or Criteria again

---

## Version 5.1.1
_5 October 2016_

### Bugs Fixed
* Addressed issue with rejecting or overwriting pending products. Re-uploading and rejecting should be functional again. Pending products will only be marked as deleted and will no longer have their status changed from 'Pending'

---

## Version 5.1.0
_4 October 2016_

### Features Added
* Changed certification status names previously known as Suspended, Terminated, and Withdrawn.
* Created Java program to send weekly email with aggregate counts for developers, products, certified products, and 2014 + 2015 CPs
	* weekly email will be sent to weekly addresses defined in summary email
	* [TEMPLATE] for deployment, update the properties.environment to add summaryEmail property
* Updated /certification_ids/ call. Still returns the list of certification_ids and the date each was created. If user is ROLE_ADMIN, also returns a semi-colon separated list of CHPL product numbers with each certification_id.
* Remove terms of use and api documentation from certified products.
* Check product code, version code, and ICS code against specific sets of characters to make sure no special characters are used.
* Add retired flag for test tools. Do not allow products to change associations with retired test tools.
* Add script to generate weekly summary email that is run as scheduled cron job

---

## Version 5.0.0
_19 September 2016_

### Features added
* Add optional argument 'edition' to /download call.
  * Generate the chpl-all file as well as a chpl-{edition} file for each edition present in the database for download.
  * Downloads chpl-all by default
* Allowed c3/c4 to be connected to CQMs
* Added error messages for c1/2/3/4-CQM mismatch
* Re-factored Certification Id web services to improve performance
* Added statuses object that shows aggregate number of certified products associated with each developer and product. These objects will allow the website search page to filter on a developer/product's number of certified products that are active/retired/withdrawn/suspended/terminated.
* Updated /developers call to be faster
* Changed /developers/update to update transparency attestation based on acb name, not acb ID
* Re-factored Certification Id API controller to support new operations ("create", "search", "verify(GET)")
* Changed /certification_ids/all operation to just /certification_ids

### Bugs fixed
* Pending 2015 products can now have CQM versions modified
* API Documentation Link is now correctly validated on upload and edit 
* Added new style product number to CAP Activity Report descriptions

---

## Version 4.0.0
_30 August 2016_

### Features Added
* Added a new service certification_ids/all to generate JSON with two fields - the certification ID and the date created. This includes all certification IDS ever and could be large.
* Change all /activity calls that used to accept a lastNDays parameter to accept start and end parameters instead. Start and end are longs and treated as timestamps. (Not backwards compatible)
* Set up log4j2 and set hopefully appropriate log levels
* TO DO DURING THE RELEASE: change the questionable activity email recipients to just be the ONC_CHPL@hhs.gov email (i.e. remove onc.certification@hhs.gov)
* TO DO DURING THE RELEASE: create a file (can call it cleantomcat) in /etc/cron.daily and chmod a+x the file. Contents of the file are listed as a comment in OCD-811. The command deletes files that have not been written to since X days ago. The number near the end of the command is X.
* Updated /activity API endpoint to incorporate new parameters to filter by API-Key, sort dateAscending, and filter by start & end date.

* Do not allow 170.315 (d)(3) to mark GAP as true
* Added 'responsibleUser' field with all user data for /activity reports
* Removed CORSFilter in web.xml; this was preventing some ajax calls from other domains
* Add developer object to /activity/product calls using the developer present in "newData"

### Bugs fixed
* Upload field values for SED parsed as integers but entered as floats were not saved. Fixed.
* Task success avg was getting mixed up with Task errors avg
* Task time deviation optimal avg was getting mixed up with task path deviation optimal avg
* Test Task Participant Product & Professional experience was getting mixed up

---

## Version 3.0.0
_10 August 2016_

### Features Added
* By default do not return retired products. Still allow searching for retired products.
* Remove visibleOnChpl filter from queries (not backwards compatible)
* Adjust developer merge code to automatically select transparency attestations for each ACB/Developer, throwing an error if an ACB has inconsistent attestations already defined for the developers being merged.
* Add descriptions to CQMs in /details results.
* Add descriptions to test standards and test functionality in /data/test_functionality
* For criteria that are eligible for and meet GAP, test tools, test procedure, and test data are not required.
* ICS is no longer required when editing 2014 products.
* Added optional inclusion of criteria met for Certification ID details through the API

---

## Version 2.0.0
_2 August 2016_

### Features Added
* The searchTerm parameter of a certified product search may now be a CHP- id or a 9-part unique ID or an ACB certification ID number. Wildcards are not supported.
* Allow updating of accessibility standards list
* Added /data/test_standards
* Added /data/qms_standards
* Added /data/ucd_processes
* Added /data/accessibility_standards
* Modified /data/education_types (breaks backwards compatibility)
* Modified /data/age_ranges (breaks backwards compatibility)
* Modified /data/test_functionality (breaks backwards compatibility)
* Modified /data/test_tools (breaks backwards compatibility)

---

## Version 1.7.0
_25 july 2016_

### Features Added
* Add validation to CHPL product update so that it matches validation done on CHPL product confirm
* Pass errors in test tools and test functionality back to the UI
* Add task rating standard deviation to upload, confirm, and api get/update

### Bugs Fixed
* Check test functionality for invalid values on product confirm and edit
* Check test tools for invalid values on product confirm and edit

---

## Version 1.6.1
_1 July 2016_

### Features Added
* Added product id to Cert ID lookup results

---

## Version 1.6.0
_16 June 2016_

### Features Added
* Allow searching by certification status name
* Completed Lookup Certification ID API function

### Bugs Fixed
* Fixed chplProductNumber was null for 2015 products, now builds number from parts

---

## Version 1.5.1
_13 June 2016_

###  Bugs Fixed
* Make test functionality optional for all certifications
* Return vendor contact and address information with certified product details
* Remove URL validation via regex

---

## Version 1.5.0
_24 May 2016_

### Features Added
* Change participant age to a range from a selectable list of ranges
* Changed 2015 and 2014/2015 Certification ID validation to no longer check CQMs
* Added more product details to Certification ID results
* Changed Additional Software in Certification ID results to be URL encoded
* Added feature to Certification ID generation to prevent formation of words in IDs
* Changed encodeCollectionKey to implement key values of base 36 and padded to 8 digits

### Bugs Fixed
* 170.314 (f)(3) does not require test tools for ambulatory products but does for inpatient
* Editing a product was requiring g1 and g2 when it should not have.
* Trim spaces from the ends of all fields in the upload file
* Properly save test functionality and test tools if an invalid one was in the upload file but was edited to be a valid one during confirm.
* Fix logging bug when invalid column header is in upload file.

---

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

---

## Version 1.3.0
_27 April 2016_

### Features Added
* Added fields to the corrective action plans
* Changed search parameter values for searching by corrective action plan (open, closed, none)

---

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

---

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

---

## Version 1.0.0
_30 March 2016_

### Features added
* Check for questionable certified product updates and trigger email if found.
* Return list of applicable transparency attestations for each developer

### Bugs fixed
* Allow for empty transparency attestation and url values

---

## Version 0.5.0
_25 March 2016_

### Features added
* API call added to check 'system is up'
* 2015 Upload process
* 2014 validation improved
* Email sending updated to conform with deployment

### Bugs fixed
* Searching on criteria alone gives error

---

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

---

## Version 0.3.2
_29 February 2016_

### Features added
* Added ability to do new 2014 upload files

---

## Version 0.3.1
_22 February 2016_

### Bugs fixed
* Added missing product number to CP manange select box

---

## Version 0.3.0
_18 February 2016_

### Features added or Updated
* Changed Corrective Action Plan/Surveillance API parameters
* Added descriptions to API methods.
* Changed API for certified products and certification results to match 2014 data requirements.
* Allow multiple values for the 'hasCap' field in search.

---

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

---

## Version 0.1.1
_12 January 2016_

### New features
* Integrated Swagger API generation

### Bugs Fixed
* Search on certifications / cqms
* Allow editing of products with errors
* Fixed various email content issues

---

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

---

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

---

## Version 0.0.1
_13 November 2015_

First release

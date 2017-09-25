# Release Notes

## Version TBD
_Date TBD_

### Minor Features
* Create separate standalone applications for creating surveillance downloads vs each listing. Will need to make the following cron changes:
  * Remove `15 5 * * * cd /opt/chpl && ./generateXml.sh && ./cleanupXml.sh -dn 15 >> cleanupXml.log 2>&1`
  * Add `0 1 1 1,4,7,10 * cd /opt/chpl && ./generateListingResources.sh 2011`
  * Add `0 1 * * * cd /opt/chpl && ./generateListingResources.sh 2014`
  * Add `0 1 * * * cd /opt/chpl && ./generateListingResources.sh 2015`
  * Add `0 1 * * * cd /opt/chpl && ./generateSurveillanceResources.sh` 
  * Add lines as well for the cleanup script (may have been done by andlar already)

---

## Version 13.1.0
_25 September 2017_

### Minor features
* Speed up API call to retrieve a Listing's entire ICS family
* Add /data/job_types call to get list of possible background job types that can be run
* Add background job processing and apply it to meaningful use user upload.
* Update 'cleanupXml' script to allow for some parameters

### Bug Fixes
* Save ICS family data when going through pending listing confirm workflow.
* Do not show macra measures for 2014 listings.

---

## Version 13.0.0
_11 September 2017_

### Major Features
_Backwards compatibility breaking changes_
* Reorganize SED data within the listing details request and response. UCD and Test tasks/participants are now located under a "sed" field. Listing update code has changed significantly as a result

### Minor Features
* Add warnings for 'phantom' criteria data where criteria is not attested to but has associated data from the upload.
* Added SED Task/Participant required fields
* Added errors on edit/upload/confirm
* Allowed uploads to complete with missing age range/education
* Added API call to retrieve a Listing's entire ICS family

### Bug Fixes
* Show all errors when editing a listing and a criteria that requires SED does not have it (was previously stopping at the first criteria in error)

---

## Version 12.4.0
_14 August 2017_

## Minor Features
* Enforce ICS codes with 2 numbers (00-99) instead of just 1 number.
* Improve error messages returned for unknown user-entered values for SED age and education levels.
* Use subscriptions for weekly statistics email and questionable activity emails

## Bugs Fixed
* Summary statistics CSV should show same day of week that script was run
* Fixed summary statistics incorrect email counts

---

## Version 12.3.0
_31 July 2017_

## Minor Features
* Improve capturing of database errors on listing upload or update.
* Improve error handling for invalid length/format codes in the CHPL unique product id.
* Send a cache-cleared header when the search cache has been evicted
* Eliminate on-demand listing search cache eviction
* Add timed refresh of listings search

---

## Version 12.2.0
_17 July 2017_

## Minor Features
* Added /collections/developers API call to return a list of all developers, attestations, urls, and counts of their listings
* Completed bulk reject of pending surveillance.
* Pulling pending surveillance validation out of a db table instead of calculating on the fly to speed up large uploads.

## Bug Fixes
* Fixed incorrect contact name displayed if pending surveillance or listing had already been confirmed/rejected.
* Only evict basic search cache if listing create/update methods return successfully.

---

## Version 12.1.0
_3 July 2017_

## Minor Features
* Improve /update API call speed. Selectively update only items that have changed rather than doing a replace on the entire listing.
* Allow mass reject of pending surveillances

---

## Version 12.0.0
_3 July 2017_

### Major Features
_Backwards compatibility breaking changes_
* Changed ICS field in certified product details object (used on get and update calls) from a boolean to a more complex object to include whether something was inherited and a list of parents and children.

## Minor Features
* Changed ICS code from a string to an integer
* Added validation to warn on missing parents if ICS is indicated
* Added an additional app that runs to send out notifications of ICS inconsistencies (need to add cron for that)
* Added description of surveillance triggers to emails
* Added developer and listing statistics for ACBs to ONC summary email

---

## Version 11.0.0
_19 June 2017_

### Major Features
_Backwards compatibility breaking changes_
* Removed /decertifications/certified_products and /decertifications/inactive_certificates API calls. This same data can now be accessed through the /certified_products API method.

### Minor Features
* Add decertification date, numMeaningfulUse, transparency attestation url, and api documentation url fields to the flat search results
* Fill in blank cells for subelements in nonconformtiy and surveillance downloads.

---

## Version 10.2.0
_5 June 2017_

### Minor features
* Add schemagen xsd generation to the build. Updated JAXB XML annotations on relevant Java classes. Requires an extra step of copying the XSD to the downloads folder after the build. CHANGES XML FILE FORMAT
* Allow corrective action plan resolution to be blank even when the end date is filled in
* Added surveillance trigger for Open Nonconformity with closed CAP > 45 days prior

---

## Version 10.1.0
_22 May 2017_

### Minor features
* Return exception with last modified user's contact info when deleting or confirming a pending Certified Product that has already been deleted or confirmed
* Add surveillance rule when a Listing has an open Non-conformity and status of "Withdrawn by..."
* Add daily/weekly surveillance trigger emails that use notification subscriptions and are specific to ACB
* Update /data/test_standards to include certification edition
* Disallow duplicate test standards per certification criterion.
* Check for exisitng test standard in the listing's edition before adding a new one (eliminates repeated entries in the database and keeps the selection dropdowns manageable).
* Update all test standard entities, dtos, and domain objects to include certification edition.
* Use error message file for error messages and support internationalization.

---

## Version 10.0.0
_8 May 2017_

### Major features
_Backwards compatibility breaking changes_
* Allow users to specify a condition in the listing update request whether or not the developer status should be changed. Only applicable if listing status is changing to Withdrawn Under
Surveillance by ONC-ACB

### Features Added
* Limit activity searches to a configurable max date range (currently set to 60 days)
* Validate the privacy and security framework users enter for listing criteria
* Add notifications service to allow registration, getting, and deleting of email subscriptions
* Add /data/notification_types service to send back the types of notifications a logged-in user may work with
* Add certification edition to test functionality and check that the right edition of test functionality is used during listing edit and upload.

---

## Version 9.1.0
_24 April 2017_

### Features Added
* Update parameters for HTTP GET /search call. All the same options that were previously available only to POST exist for the GET. Updated related API documentation.
* Add /cache_status endpoint. Returns a status of OK or INITIALIZING based on whether the basic search cache has been loaded.
* Add statistics to ONC weekly email body and csv file
* Add endpoint /{productId}/split to allow splitting of products and moving versions to the old or new product.
* Add optional contact information for a product that can be changed by ONC or ACB Admins.
* Add optional showDeleted flag to the /data/search_options call. Defaults to false but setting to true will include deleted ACBs in the response.

### Bugs Fixed
* Fix error saving developer with updated contact information.

---

## Version 9.0.0
_10 April 2017_

### Features Added
* Show error to ONC-ACB during upload and edit when Surveillance Activity has a nonconformity with an entry for "Date Corrective Action Plan Was Approved" but no entry for "Date Corrective Action Plan Must Be Completed"
* Show error to ONC-ACB during upload and edit for a Surveillance Activity nonconformity that violates business rules for "Corrective Action Plan End Date"
* Add statusEvents field to developers to track past status changes (Active, Suspended by Onc, etc) and the date on which each status change occurred. Provide create, read, update, and delete API functionality for developer status changes.
* Add decertificationDate to the data returned in the banned developers API call.
* Updated basic search objects that get returned:
  * remove has open surveillance, has closed surveillance, has open nonconformities, has closed nonconformities
  * add surveillanceCount, openNonconformityCount, closedNonconformityCount fields
* Cache results of /certified_products/pending to improve view time by ~683%
* Add Surveillance Friendly ID to Surveillance (Basic) CSV file download
* Add optional "fields" parameter to /certified_products call to allow only sending back a custom list of fields
* Add nonconformity status to daily and weekly surveillance broken rules reports. Only include values in the reports with Open nonconformities.

### Bugs Fixed
* Fix misaligned cells in nonconformity download file

---

## Version 8.3.0
_27 March 2017_

### Features Added
* Add authority to surveillance to allow end user to tell whether ONC or ACB created a surveillance activity
* Disallow saving of Surveillance without close date but with no open Non-Conformities
* Improve performance of /surveillance/pending by ~643%

### Bugs Fixed
* Fix an Internal Server Error when obtaining user activities and there exists a deleted user

---

## Version 8.2.0
_13 March 2017_

### Features Added
* No longer use ACLs that were getting added to each pending product x ACB Admin.

---

## Version 8.1.0
_27 February 2017_

### Features Added
* Add basic search API endpoint to return all certified products
* Add surveillance statistics to weekly email
* Protect basic surveillance report and only allow download by ONC ADMIN and ONC STAFF
* Add functionality to allow a "rolling cache" that refreshes the cache asynchronously while allowing the user to view currently available cached data
* Make caches eternal so the user gets cached data more often
* Add columns to surveillance reports (ACB Name, certification status, hyperlink to CHPL listing) and reformat dates (yyyy/mm/dd)
* Add daily and weekly surveillance oversight reports to calculate which surveillance items have broken a given set of rules.
  * Environment properties have been added:
 ```
 #oversight email properties
 oversightEmailDailyTo=sample@email.com
 oversightEmailDailySubject=Daily Surveillance Broken Rules Alert
 oversightEmailDailyNoContent=<p>No surveillance oversight rules were newly broken in the last day.</p>
 oversightEmailWeeklyTo=sample@email.com
 oversightEmailWeeklySubject=Weekly Surveillance Broken Rules Alert
 oversightEmailWeeklyNoContent=<p>No surveillance oversight rules are broken.</p>
 suspendedDaysAllowed=30
 capApprovalDaysAllowed=75
 capStartDaysAllowed=10
 ```
  * To run the weekly report at 00:05 on Wednesdays, add a line like the below to crontab
 ```
 5 0 * * 3 cd /chpl/chpl-api/chpl/chpl-service && ./generateWeeklySurveillanceOversightReport.sh
 ```
  * To run the daily report at 00:05 every day, add a line like the below to crontab
 ```
 5 0 * * * cd /chpl/chpl-api/chpl/chpl-service && ./generateDailySurveillanceOversightReport.sh
 ```

---

## Verison 8.0.0
_7 February 2017_

### Features Added
* Add G1 and G2 macra measures to certified product upload, edit and detail display *BREAKS BACKWARDS COMPATIBILITY*
* Add functionality to get Meaningful Use User Accurate As Of Date and update it
* Check certification date and additional software code vs supplied data for CP edit and update code if necessary
* Add API call for decertified certified products with inactive certificates
* Update API call for decertified certified products to not include products with certification status 'Withdrawn by Developer'
* When a Certified Product is marked as "Withdrawn by Developer under Surveillance/Review" by an ONC_ADMIN or ACB_ADMIN, update Developer Status to "Under Certification Ban by ONC"
* Exclude Developers with status "Suspended by ONC" from the /decertifications/developers API call
* Selectively evict caches in order to improve website performance

### Bugs Fixed
* Update 2014 validator retired test tool logic to handle CHPL-XXXXXX products that will not have an icsCode

---

## Version 7.1.0
_23 January 2017_

### Features Added
* Improve website performance:
  * Initialize cache stores asynchronously at server startup
  * Increase cache timeout to 1 hour
  * Add caching for /certification_ids API call
  * Add caching for /pending API call
  * Add caching for /search API call
  * Add caching for /decertifications/developers
  * Add caching for /decertifications/certified_products
  * Update caching for /search_options API call
  * Cache underlying data that improves call speed for many other API calls
* Added properties to environment.properties related to cache timeout
  * `enableCacheInitialization=true`
  * `cacheClearTimeoutSecs=15`
  * `cacheInitializeTimeoutSecs=300`
* /authenticate now returns 403 Forbidden for bad credentials instead of 500 Internal Server Error
* Add new certification status for products
* Allow 0 for number of randomized sites and total sites for surveillance
* Remove products marked Suspended By ONC from Decertified Product search
* Allow retired test tools where certified product ICS=true

### Bugs Fixed
* Fixes exception when getting back activity performed by a user that has since been deleted
* Fixes innaccurate error message if a user tries to add surveillance to a product under an ACB they are not associated with

---

## Version 7.0.0
_6 January 2017_

### Features Added
* Add API methods for surveillance-related acivities. This includes upload, confirm, and reject for the bulk upload of surveillance data. It also includes create, update, and delete for singular changes to surveillance. Surveillance and associated non-conformities are returned with certified product details.
* Add API call to support updating certified_product meaningful_use_user counts with a CSV upload in CMS Management
* Change behavior if certified product is marked as suspended or terminated by ONC. These statuses also result in a developer status change and require ROLE_ADMIN
* Add API call to get decertified developers with developer name, the developer's associated ONC_ACBs, the developer status, and the sum of the developer's estimated number of meaningful use users for all certified products associated with the developer.
* Add API call to get decertified certified products with pageCount set to the total number of decertified products
* Add certificationDateStart and certificationDateEnd as advanced search parameters
* Change corrective action plan search parameters to new surveillance search parameters - *NOT BACKWARDS COMPATIBLE*
* Send questionable activity email when product owner changes (not during a merge)
* Add /surveillance/download[?type=all|basic|nonconformities] endpoint to allow download of CSV file with all surveillance and download of CSV file with surveillance that resulted in nonconformities
* Add lookup to new tables for certification status event history
* Add date of last certification status change to certified product details
* Add script cleanupXml.sh to remove download files older than 30 days except for the first of each month. Needs to be given executable permission and have a cron job set up.
* Add decertification date to certified product details *CHANGES XML DOWNLOAD FORMAT*

### Bugs Fixed
* Changed transition of care calculations for the EHR Certification ID to more closely match the rule.

---

## Version 6.0.1
_17 November 2016_

### Bugs Fixed
* Mark all DAO methods that return certified product details as transactional so they can retrieve the product owner history. Fixes issue with summary email being sent and with xml download not being generated.

---

## Version 6.0.0
_15 November 2016_

### Features Added
* Add ability to retrieve and edit product ownership history (which developers a product has previously been owned by). BREAKS BACKWARDS COMPATIBILITY when retrieving certified product details
* Add "showDeleted" parameter to /developers call. Defaults to false.

### Bugs Fixed
* Make it impossible to confirm duplicate pending certified products
* Ability to edit a certified product without security authorization

---

## Version 5.2.0
_21 October 2016_

### Features Added
* Added caching to searchOptions API call to speed up search page
  * Temporailly turned off due to transaction/caching issues
* Updated the algorithm for calculating weekly aggregate counts in order to take into account deleted developers/products
* Generating simple CSV files for quicker download and viewing than the large XML files
* Added "format" parameter to the download API endpoint which can be blank and will default to xml. Either xml or csv may be specified.
* Added Developer status view / edit
* Cleaned up persistence.xml resource files
* Added role 'ROLE_ONC_STAFF'
* Allowed ROLE_ONC_STAFF access to all report areas, including those previously restricted to ROLE_ADMIN only

### Bugs Fixed
* Trim spaces from splittable fields in CSV upload

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

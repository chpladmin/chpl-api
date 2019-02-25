# Release Notes

## Version 20.4.0
_25 February 2019_

### New features
* Allow upload / confirm of macra measures RT 13, 14, 15
* Update behavior of pre-loaded caches which will affect the /cache_status call.
  * All pre-loaded caches have an additional copy loaded in the background when their data changes. The background cache is then swapped with the live cache when necessary.
  * The /cache_status call was reporting OK after initial startup but would later report INITIALIZING if user actions changed any cached data. Now once it reports OK it should continue to do so.
* Save uploaded SED task/participant numbers as text; give error when cannot be parsed to numbers

---

## Version 20.3.0
_11 February 2019_

### New Features
* Remove ACB from scheduled jobs when ACB is retired
* Save retirement dates for ACBs/ATLs
* Allow ROLE_ADMIN and ROLE_ONC to manage pending surveillances
* Allow ROLE_ADMIN to run Quartz system jobs
* Update Swagger documentation regarding API security
* Change InfoGard's name to UL LLC

### Bug Fixes
* Allow newly added ACBs and ATLs to have users added to them.
* Remove duplicate G1/G2 macra measures and warn the user for criteria that are not attested to.

---

## Version 20.2.0
_28 January 2019_

### New Features
* Added ROLE_ONC user role. ROLE_ONC replaces the existing ROLE_ADMIN and all relevant accounts were converted to the new role.
* Removed references to ROLE_ONC_STAFF.
* Add upload errors for test task id and participant id being too long

---

## Version 20.1.0
_14 January 2019_

### New Features
* Change surveillance and sed report filenames to include timestamp when the file was last generated.
* Update edit and confirm of criteria to remove phantom data
* Update certain warnings to errors for uploading, editing, and confirming certified products
* When validating pending listings, check for duplicate values, remove the duplicate value, and provide a warning message that the duplicate was removed
* Show better error message when user uploads Test Tasks with IDs that are too long
* Add definition file for the Surveillance - Basic download file

### Bugs Fixed
* Save "Developer identified targeted users" on upload/confirm
* Update invalid error and warning messages for upload file

---

## Version 20.0.0
_17 December 2018_

### Backwards compatibility breaking changes
* Removed all previously deprecated API endpoints
* Removed the following endpoints:
  * /atls/{id}/delete
  * /atls/{id}/undelete
  * /acbs/{id}/delete
  * /acbs/{id}/undelete
* Remove showDeleted parameter from the following endpoints:
  * /activity/acbs
  * /activity/acbs/{id}
  * /activity/atls
  * /activity/atls/{id}
  * /data/search_options
  * /atls
  * /acbs
* Removed space following the colon in the /cache_status response. {"status": "OK"} becomes {"status":"OK"}
* Removed space following the colon in the /status response. {"status": "OK"} becomes {"status":"OK"}
* Removed /notifications endpoints.

### New Features
* Add retired flag to ACBs and ATLs to replace the functionality that previously used the deleted flag.
* Add Quartz job to require all users to change password on next login
* Update email notification about potential Developer ban to include:
  * Reason for status change
  * Reason for listing change
* Prevent users from using the following macra measures which are under review: RT13 EH/CAH Stage 3, RT14 EH/CAH Stage 3, RT15 EH/CAH Stage 3 

### Bugs Fixed
* Do not show error for 2014 listings on upload or edit if they attest to g3, have ICS = true, and do not have any criteria marked as SED.
* Fix issue with determining whether there is additional software associated with the certified product

---

## Version 19.2.0
_3 December 2018_

### New Features
* 2015 Functionality testing is restricted by criteria.
* Save 'reason' for developer's status change in questionable activivty.
* Output 'reason' for developer's status change on questionable activity report.
* When uploading a listing, remove duplicate G1 and G2 macra measures and provide a warning.
* Implement user ability to reset their password using a unique link
* Change /cache_status endpoint to report "OK" status only when all pre-loaded caches have completed.
* Add "user must reset password on next login" workflow
  * Gives error when user tries to log in and needs to change password
  * Adds end point to change_expired_password
  * Update User edit to allow admins to require password change
* Add API Documentation file upload and download endpoints

---

## Version 19.1.0
_19 November 2018_

### New Features
* Update ONC contact information for CHPL API and on user invitation emails.
* Check various site counts in surveillance to make sure they are reasonable
* Refresh the listing collection cache on demand when data has changed vs at timed intervals.

### Bug Fixes
* Fix dependency injection problem that prevented implementing security in the chpl-service classes.

---

## Version 19.0.0
_5 November 2018_

### Backwards compatibility breaking API changes
* Changed PUT /products call to not accept a productID
* Updated DELETE of api key to not use body

### New Features
* Add developer and product contact information to 2014/2015 download file
* Add Quartz job to allow interruption of other jobs
  * Enhanced "Certified Product Download File generation job" to be interruptable
* Add validation for submitted user information when creating a new user

### Bugs Fixed
* Properly handle invalid test tools entered into upload files by removing them and informing the user
* Make sure test tools are optional for 2014 ambulatory listings on g1, g2, and f3
* Remove required productID in /products PUT call that isn't used by the back end
* Insert listing update activity during meaningful use user uploads
* Update the description of the /certified_products endpoint to indicate that 'versionId' is a required parameter
* Fixed issue where adding ROLE via POST required body, even though no data was needed

---

## Version 18.0.0
_22 October 2018_

### Backwards compatibility breaking features
* Removed deprecated /certified_products/meaningful_use/upload. Use /meaningful_use/upload instead.

### New Features
* Added "last modified date" for surveillance and non-conformities to download files
* Move generate chart data app to quartz

### Bugs Fixed
* Fix creation of public announcement ignoring 'public' checkbox.
* Fix incorrect errors for changing sed to false for a criteria
* Modified the Summary Statistics Report email
  * Re-arranged the order of some headings
  * Added totals for Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2014 Listings
  * Added totals for Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2015 Listings
  * Added active ACBs (when broken out) where the total for the ACB is 0
* Allow editing of meaningful use user count and history of muu counts for individual listings.

### Bugs Fixed
* Fix creation of public announcement ignoring 'public' checkbox.
* Fix incorrect error messages when editing SED = false for a criteria

---

## Version 17.0.0
_8 October 2018_

### Major Change
* When retrieving Pending Surveillances, the listing details will no longer be populated

### New Features
* Move surveillance download file generation into quartz job to allow for better job management.
* Move SED G3 download file generation into quartz job for allow for better job management.
* Added scheduled job to send warning to users who have not used thier API key in the last X days
* Added scheduled job to delete api keys that have not been used in X days
* Added LastUsedDate and DeleteWarningSentDate to the ./key endpoint return value(s)
* Add password strength checker implementing [a java fork](https://github.com/nulab/zxcvbn4j) of [zxcvbn](https://github.com/dropbox/zxcvbn) when anyone:
  * creates a new User
  * updates a password
* Add Developer Status to search results view
* Catch any unexpected error that occurs during a listing upload. Allow the error to be emailed to CHPL team if desired.

### Bugs Fixed
* Invalid test functionality names are removed and an error is given to the user for 2014 and 2015 uploads.
* Fix questionable activity report end of the month date rollover.

---

## Version 16.4.0
_24 September 2018_

### New Features
* Validate URLs on upload / confirm / edit
  * Transparency Attestation
  * SED Report File
  * Other report file
  * API Documentation (at criteria level)
  * Developer website
* Add info messages for 2014 cms id widget
* Add info messages for 2014/2015 id widget
* Move questionable activity email code into quartz job to allow user scheduling.

---

## Version 16.3.0
_10 September 2018_

### New Features
* Add required reason business logic for developer bans.
* Add API throttling
* Moved the Quartz scheduler to run in the Tomcat context
* Each Quartz job writes to a separate log file
* Add Open Surveillance Activities by ACB in the Statistics Report
* Add Open Nonconformities by ACB in the Statistics Report
* Return info messages for CMS ID missing criteria

### Bugs Fixed
* Extra test data/test procedure/test functionality/etc. no longer appears after attesting to a criteria with previously entered phantom data.
* Fixed the "basic" certified product service to return the correct additional software code in the CHPL Product Number
* Fixed XML generation process to properly output `<tasks></tasks>` tag - was previously outputing as `<></>`.
* Use "full name" and "friendly name" for users/contacts
* Fix reason required error to show again for certain listing updates.
* Fix possibility of multiple developer status edits happening at the same time.

---

## Version 16.2.0
_27 August 2018_

### New Features
* Migrate "ICS errors report" to Quartz
  * Added new Quartz job to gather and store ICS error data
  * Added new user schedulable job that sends the Summary Statistics Email
  * Updated manager to support ACB specific Jobs
* Migrate "Broken Surveillance Rules" report to Quartz
  * Added new Quart job to gather and store surveillance error data
  * Added new user schedulable job to send the error email

### Bug Fixes
* Fix 2015 listing XML file creation process

---

## Version 16.1.0
_16 August 2018_

### New Features
* Add nonconformity charts statistics endpoint
* Identify questionable activity based on the confirmed date of a listing rather than the certification date.
* Add "Trigger Developer Ban" notification
* Migrate "Summary Statistics" to Quartz
  * Added new Quartz job to gather and store Summary Statistics
  * Added new user schedulable job that sends the Summary Statistics Email
  * Added new endpoint for retrieving all jobs that user has permission to schedule
* Download XML file changes
  * In the <ucdProces> node
    * AllVersions will now be structured: `<allVersions><version/><version/> ... </allVersions>`
    * SuccessVersions will now be structured: `<successVersions><version/><version/> ... </successVersions>`
  * In the <listing> node
    * CertificationStatusEvents will now be structured: `<certificationEvents><certificationEvent><eventDate/><id/><reason/></status></certificationEvent> ... </certificationEvents>`
    * TestingLabs will now be structured: `<testingLabs><testingLabs/><testingLab/> ... </testingLabs>`
  * In the <ucdProcess> node
    * Criteria will now be structured: `<criteriaList><criteria/><criteria/> ... </criteriaList>`

---

## Version 16.0.0
_7 August 2018_

### Backwards compatibility breaking changes
* Require start and end data parameters on all /activity API calls that are not for a single item.

### New Features
* Added JavaMelody to provide Tomcat performance monitoring page.
* Restrict certain /activity data to admin, acb, atl, or cms users as appropriate.
* Update text describing /download endpoint

---

## Version 15.16.0
_18 July 2018_

### New Features
* Legacy 2014 listings (those with product numbers like "CHP-") are allowed to have attested to 170.314 (g)(3) but not required to have a criteria with SED and vice versa.

---

## Version 15.15.2
_16 July 2018_

### Bugs Fixed
* Fixed bug where user erroneously receives a Duplicate CHPL Product Number error when editing a product

---

## Version 15.15.1
_5 July 2018_

### Bugs Fixed
* Fix display of 2015 products due to problems with getting test functionalities

---

## Version 15.15.0
_5 July 2018_

### New Features
* Converted "Downloadable Resource File" generation application to Quartz Job
  * Updated API endpoints to not return "System jobs" in regular GET call
* Logs for individually run apps will now show up in separate files under the logs directory.
* Validate 2014 test functionalities to ensure they are valid based on practice type and certification criterion
* Modified certified product details to return the allowable test functionalities for each criteria

### Bugs Fixed
* Save and display test tool name and test tool version for criteria 170.314 (c)(1)
* Save and display privacy and security framework for 170.315 (a)(7)

---

## Version 15.14.0
_18 June 2018_

### New Features
* Updated the XML and CSV file creation batch process to retrieve data asynchronously
* Added check for 2015 listings to ensure if they have attested to G1 or G2 that they also have listed G1 or G2 macra measures for at least one criteria.
* Added Quartz Scheduler component
  * Includes API endpoints for GET/PUT/POST/DELETE of schedule Triggers
  * Converted Cache Status Age app to Cache Status Age Quartz Job
* Updated validation to ensure (g)(3) certification is valid iff at least one criteria attests to SED

### Bug fixes
* Fix a bug that allows ROLE_ADMIN to see pending certified products and surveillances

---

## Version 15.13.1
_6 June 2018_

### Bug Fixes
* Handle blank or null test tool version in listing updates.

---

## Version 15.13.0
_4 June 2018_

### New Features
* Added new service /activity/corrective_action_plans to return just legacy corrective action plan activities.
* Add new chart: New vs. Incumbent Developer chart
  * Update chart data generation application
  * Add API endpoint to retrieve chart data
  * Refactored other Chart data generation to increase speed
* Updated URLs and verbs for several REST endpoints
  * Old endpoints have been deprecated
* Add new chart: Count of Developers & Products by Edition & Status
  * Update chart data generation application
  * Add API endpoint to retrieve chart data

### Bug Fixes
* Handle SED boolean parsing and UCD Process existence mistmatch for 2014 upload.

---

## Version 15.12.0
_21 May 2018_

### Minor Features
* Add endpoint /certified_product/{chpl_product_number} to return basic information about a certified product
* Add endpoint /certified_product/{chpl_product_number}/cqm_results to return CQM results information about a certified product based on a CHPL Product Number
* Add endpoint /certified_product/{chpl_product_number}certificationtion_results to return certification results information about a certified product based on a CHPL Product Number
* Add endpoint /certified_product/{chpl_product_number}/certificationtion_results to return certification results information about a certified product based on a CHPL Product Number
* Add endpoint /certified_product/{chpl_product_number}/ics_relationships to return relationship tree information about a certified product based on a CHPL Product Number
* Add endpoint /activity/certified_product/{chpl_product_number} to return activity information about a certified product based on a CHPL Product Number
* Add new chart: Criterion / Product statistics
  * Update chart data generation application
  * Add API endpoint to retrieve chart data

### Bugs Fixed
* Fix Summary Statistics Report missing ISCA count under the Total # of Unique Products with Active 2015 Listings section
* Respect configured questionable activity window to allow a user to make edits that do not get logged as questionable activity for a short amount of time after a listing's certification date.
* Fix bug restricting add/edit of Versions of Test Data

---

## Version 15.11.0
_7 May 2018_

### Minor Features
* Added error on upload for qms to be true but have empty standards and vice versa
* Restrict CMS IDs from using deleted Listings for /search or /create
* Added endpoint /certified_product/{certified_product} to return basic information about a certified product
* Added endpoint /certified_product/{certified_product}/cqm_results to return CQM results information about a certified product
* Added endpoint /certified_product/{certified_product}/certification_results to return certification results information about a certified product

### Bugs Fixed
* Fixed bug with searching for Listings with lower case CHPL ID values
* Fix bug where uploaded Listings file with bad B1 cell isn't treated correctly

---

## Version 15.10.0
_23 April 2018_

### Major Features
* Add support for Multiple ATLs
  * Upload / confirm workflow
  * Edit Listing validation & questionable activity

### Minor Features
* Add counts of 2015 Listings with Alternate Test methods to Summary email
* Wide variety of linting changes
* Enhanced performance when retrieving Certified Product Details
* Fixed counts for Total # of Active (Including Suspended by ONC/ONC-ACB 2014 Listings) in the Summary email

---

## Version 15.9.0
_9 April 2018_

### Minor Features
* Add ability to add G1/G2 measures for non-attested criteria
* Fixed bug that allowed duplicate Chpl Product Numbers during upload

---

## Version 15.8.0
_26 March 2018_

### Minor features
* Charting endpoints
  * Participant/Age counts
  * Participant/Education counts
  * Participant/Ender counts
  * Participant/Professional Experience
  * Participant/Product Experience
  * Participant/Computer Experience
* Charting application updates
  * Participant/Age counts
  * Participant/Education counts
  * Participant/Gender counts
  * Participant/Professional Experience
  * Participant/Product Experience
  * Participant/Computer Experience

---

## Version 15.7.0
_12 March 2018_

### Minor features
* Add endpoint for retrieving SED/Participant counts to be used for charting
* Add application to generate SED/Participant counts to support charting

### Bug fixed
* No longer show errant error messages for missing g1/g2 values if criteria not attested to

---

## Version 15.6.0
_26 February 2018_

### Minor features
* Enhanced certification status change questionable activity abilities to support
  * Change of current status date
  * Any modification of certification status history

### Bug fixed
* Enabled questionable activity window

---

## Version 15.5.0
_18 February 2018_

### Major features
* Add fuzzy match functionality for uploading of UCD Process, QMS Standard and Accessibility Standard names
* Add warning messages that name values have changed for certain Processes or Standard names
* Add /data/fuzzy_choices controller for getting and updating fuzzy match choices

### Minor features
* Look for required reason for certain questionable activities; error on those actions if a reason is not found.
* Add reason to questionable activity report.
* Add reference of pending listing to confirmed listings to enable tracking between pending and confirmed.
* Add certification status change reason to listing activity entities and to questionable activity report.
* Use most recently generated CMS ID if multiple exist with same product list set

---

## Version 15.4.3
_1 February 2018_

### Tiny features
* Do not require UCD Processes for 2014 criteria with SED true if the listing ICS is false.
* Do not require a reason for listing certification status change

---

## Version 15.4.2
_25 January 2018_

* Do not allow users to update listings certification status history if they remove the original Active status.

---

## Version 15.4.1
_19 January 2018_

### Bugs Fixed
* Save g1 and g2 success values for new 2014 listings with 170.314 (a)(4) criteria.

---

## Version 15.4.0
_17 January 2018_

### Major features
* Add new surveillance upload job type; process large upload files as background jobs.

### Minor features
* Allow reason for certification status change for any listing. Require reason for certification status change if new status is Withdrawn by ONC-ACB.
* Stop using 'certificationStatus' field; instead use "latest" of the certificationEvents array for current status

### Bugs Fixed
* Statistics correctly account for deleted listings/unique products.

---

## Version 15.3.0
_2 January 2018_

### Minor features
* Made ICS Source required for 2015 w/ICS on upload/confirm/edit
* Test tools are required for 170.315 (b)(8)
* GAP is required for 170.314 (b)(5)(B)
* Add warning messages to uploaded listings and surveillance regarding invalid characters found.
* Do not include 'deleted' field in listing update generated sql

### Bugs fixed
* Allow Listings to have CHPL IDs that match "deleted" Listings
* Allow ROLE_ADMIN to edit listings
* Allow ROLE_ADMIN to create and edit surveillance

---

## Version 15.2.0
_18 December 2017_

### Major feature
* Update ROLES
  * Change ROLE_ACB_ADMIN to ROLE_ACB
  * Change ROLE_ATL_ADMIN to ROLE_ATL
  * Remove ROLE_ACB_STAFF
  * Remove ROLE_ATL_STAFF

### Minor Features
* Do not allow duplicate QMS Standards or UCD Processes to be added.
* If GAP is not specified for 170.314 (b)(5)(B) a warning will be returned instead of an error.

---

## Version 15.1.0
_5 December 2017_

### Minor Features
* Added /data/test_data service to get all criteria with allowable test data
* Added /data/test_procedure service to get all criteria with allowable test procedure
* Use new test data values for upload and validation of listings.
* Use new test procedure values for upload and validation of listings.
* Support 2015 upload template v12.

### Bugs Fixed
* Surveillance upload fixed to match on criteria number without space between number and letters.

---

## Version 15.0.0
_20 November 2017_

### Backwards compatibility breaking features
* Re-wrote API search code. See documentation for the HTTP GET /search call for information.

### Bugs Fixed
* Fix 60 day date range check for activity when date range overlaps daylight savings.

---

## Version 14.2.1
_07 November 2017_

### Bugs Fixed
* 170.315 (b)(8) is not required to have test procedures or test tools. Will become required on the Nov 20 push.

---

## Version 14.2.0
_06 November 2017_

### Minor Features
* Add certified_products/sed_details endpoint that returns the latest SED all Details document
* Add new version of 2014 upload template that adds a GAP column for 170.314 (b)(5)(B)
* Change warning message for transparency attestation saving
* Re-work handling of questionable activities. Do not send an email each time questionable activity appears, but save it all and send as a weekly report to subscribers instead.

### Bugs fixed
* Fix bug where uploaded SED Test Participants with identical demographics are not recognized correctly
* Improve handling for upload files that have blank columns at the end

---

## Version 14.1.0
_23 October 2017_

### Minor Features
* Support multiple upload file templates for 2015 listings. The current template continues to be supported and also one that includes ICS family information, removes g1/g2 information for 170.315(g)(7), adds test tool and test data fields for 170.315 (b)(8), and removes test tool and test data fields for 170.315 (f)(5)

### Bug Fixes
* Fix issue preventing the creation of new users

---

## Version 14.0.2
_12 October 2017_

### Bug Fixes
* Do not allow g1 and g2 booleans for 2015 criteria

---

## Version 14.0.1
_11 October 2017_

### Bug Fixes
* Do not support /certified_products call without a ?versionId parameter

---

## Version 14.0.0
_10 October 2017_

### Backwards compatibility breaking features
* Remove /certified_product_details?productId= call

### Minor Features
* Create separate standalone applications for creating surveillance downloads vs each listing. Will need to make the following cron changes:
  * Remove `15 5 * * * cd /opt/chpl && ./generateXml.sh && ./cleanupXml.sh -dn 15 >> cleanupXml.log 2>&1`
  * Add `0 1 1 1,4,7,10 * cd /opt/chpl && ./generateListingResources.sh 2011`
  * Add `0 1 * * * cd /opt/chpl && ./generateListingResources.sh 2014`
  * Add `0 1 * * * cd /opt/chpl && ./generateListingResources.sh 2015`
  * Add `0 1 * * * cd /opt/chpl && ./generateSurveillanceResources.sh`
  * Add lines as well for the cleanup script (may have been done by andlar already)
* Return HTTP 404 for /certified_products/{id}/details if listing has never existed or has been deleted.
* Change any URL with an ID in the path to return 404 if that ID is not found.
* Allow all URLs to be accessed with or without a trailing slash ('/')

### Bug Fixes
* Do not show macra measures for 2014 listings

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

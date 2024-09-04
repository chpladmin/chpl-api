# Release Notes

## Version 46.2.0
_3 September 2024_

### Features
* Detect duplicate version names per product when editing versions
* Add validation for duplicate product names on product edit

### Bug Fixes
* Use correct HQL when removing criteria-SVAP association
* Use updated HQL syntax for LocalDateTime comparison for Announcements

---

## Version 46.1.0
_19 August 2024_

### Features
* Forgot password functionality for Cognito Users
* Create endpoint POST /subscriptions/notifications-report
* Create job to email report of subscription notifications to admin
* Add 2024 rwt criteria w/o b6 and 2025 rwt criteria with b11

### Flagged Features
* Require b11 when flag is on, otherwise a9 or b11 is ok

---

## Version 46.0.0
_5 August 2024_

### Breaking Changes
* Remove Cures Update Report and report-data/cures-update-report

### Features
* Add ability to edit Cognito user information
* Support Developer Name and User tokens in messaging
* Deprecate the activity/metadata/listings endpoint

### Bug Fixes
* Use correct SQL/HQL syntax for not null

---

## Version 45.1.0
_22 July 2024_

### Features
* Build "Unique products" report in PowerBI
* Allow disabling of sending emails throughout the app
* Allow newly created user to change their password when logging in
* Change 'Developer Status' col heading to 'Banned or Suspended'
* Add report for Inactive Products

---

## Version 45.0.0
_8 July 2024_

### Breaking Changes
* Remove 'showDeleted' request parameter from GET /developers

### Features
* Upgrade Spring Framework
* Create message-preview endpoint for developer messages
* Deprecate download/{listingType} for {listingType}/download
* Create Service Base URL List downloadable file after collecting data
* Deprecate developer statusEvents in favor of statuses
* Deprecate developer status history; add status start/end dates

### Bug Fixes
* Fix spelling of "entries" in messages
* Correctly change developer status on listing termination
* Developer ban/email occurs for past or present listing status change

---

## Version 44.5.0
_24 June 2024_

### Features
* Add endpoint to retrieve (b)(6) to (b)(10) over time report
* Create ability to download developer search results as CSV

---

## Version 44.4.0
_10 June 2024_

### Features
* Update Attestation Report to work with Cognito users
* Stop recording certification id created activity
* Create job for ADMIN/ONC to Message Developers

---

## Version 44.3.0
_28 May 2024_

### Features
* Add specific Cognito login endpoint
* Notify subscribers about more listing updates
* Incorporate fuzzy matching to optional standard upload
* Add 'displayValue' as Optional Standard field

### Bug Fixes
* Correctly handle editing of listing with duplicate QMS Standards

---

## Version 44.2.0
_14 May 2024_

### Features
* Congito users need to be associated with a CHPL env (DEV, QA,etc)
* Update Jobs to incorporate Cognito users
* Update Change Requests to incorporate Cognito users
* Make Risk Mgmt Summary Information url optional
* Create "v3" endpoint of developer search
* Deprecate /developers/search/v2 endpoint

### Bug Fixes
* Save activity when user is anonymous
* Declare unchecked exception about missing reason on listing update
* Remove 'update' feature of QMS Standards, only add and remove
* Stop recording duplicate product creation activity

---

## Version 44.1.0
_29 April 2024_

### Features
* View Cognito user information
* Create/invite new Cognito users (onc, cms, acb, dev)
* Update Activity and Questionable Activity to incorporate Cognito users
* Create endpoints to get SVAP, Standard, and Functionality Tested activity pages
* Save activity for CRUD ops of Functionality Tested, Standards, and SVAPS

---

## Version 44.0.0
_15 April 2024_

### Breaking Change
* Remove deprecated endpoints and fields eligible for removal Jan 1, 2024

### Features
* Get Listing's initial state if Certification Date before Real World Testing start date
* Support more criteria headings during upload
* Support RWT url and date fields in listing upload
* Support uploading and confirming Code Sets on a listing

---

## Version 43.7.0
_1 April 2024_

### Features
* Create/invite new ADMIN Cognito users that can login to CHPL
* Add Criteria Update report by Criteria Details (Attribute)
* Mark "removing code sets" as questionable activity
* Allow multiple surveillance process types for quarterly reports
* Remove code, endpoints related to quarterly report exclusions
* Add validations for required Standards during Listing Confirm and Edit workflows

### Bug Fixes
* Use left join for criteria editon when getting standard groups
* Handle attesting criteria during edit w/o selecting standards

---

## Version 43.6.0
_18 March 2024_

### Features
* Add Roles chpl-onc, chpl-developer, chpl-onc-acb to Cognito
* Service Base URL report - check that the response length is not 0 length
* Accept educationType and age objects for test participants
* Add associated ONC-ACBs and Developer Users to Service Base URL List email report

---

## Version 43.5.0
_5 March 2024_

### Features
* Add Code Set functionality to selected criteria
* 'Updated Listing Status Report' should show 'Criteria Require Update Count' only for Active criteria
* Create one-time job to remove ICS children
* Add searchTermRecordCount to search API response
* Create background job to handle future-dated cert status updates
* Group all activities as STATUS_CHANGE if any status event changes
* Detect questionable activity if cert status > 30 days in future
* Deprecate GET /data/developers, /data/products

### Bug Fixes
* Change current status calculation for listing to use today

---

## Version 43.4.0
_20 February 2024_

### Features
* Allow ONC/ADMIN to always update change request status
* Remove XML response format from search endpoints
* Add dependency between b11 and g3 criteria (related to SED)

---

## Version 43.3.0
_5 February 2024_

### Features
* Create new report for listings that need to be updated for HTI-1
* Require ONC-ACB to enter at least one Standard per Standards 'group'
* Remove erd-phase-2-grace-period-end, erd-phase-3 flags
* Remove all references to d14 criterion
* Clean up Summary Statistics report

### Bug Fixes
* Make all fields of CertifiedProductSearchDetails Serializable

---

## Version 43.2.0
_23 January 2024_

### Features
* Detect Questionable Activity when criterion with expired attribute is attested

### Bug Fixes
* Update API Key Last Used Date
* Correctly add CQMs for 2011 listings with NQF type of CQMs
* Correctly save listing CQM updates

---

## Version 43.1.1
_9 January 2024_

### System Maintenance
* Serialize calls to the listing search

---

## Version 43.1.0
_8 January 2024_

### Features
* Deprecate endpoints used by removed charts
* Update saving of Certification Results for 2014 Listings to handle unattested Criteria
* Check criteria URLs in the URL Status Report
* Include companion guide link in criterion API response
* Detect Questionable Activity when retired Standards are added to a Certification Result
* Add ability to store SSO user id when auditing changes
* Remove ability to download daily listing files as XML
* Stop generating an XML download file of listings
* Add developer rwt-plan-urls and rwt-result-urls methods
* Create /subscriptions/search endpoint for ADMIN use

### Bug Fixes
* Handle null edition in surveillance permissions check for ACBs
* Include responsible user in questionable activity report

---

## Version 43.0.0
_11 December 2023_

### Breaking Changes
* Remove lastModifiedDate from listing object
* Remove CertificationBody retirementDate
* Remove eligible deprecated endpoints

### Features
* Add Standards attribute to Criteria
* Check for removed criteria attributes added for questionable activity
* Stop detecting Cures Update removed questionable activity
* Stop recording questionable activity for 2011/2014 listing update

### Bug Fixes
* Correctly return null if edition is not found during upload
* Get uploaded listing records to include in error email

---

## Version 42.5.0
_27 November 2023_

### Features
* Update Surveillance to work with Editionless listings
* Implement a more flexible rate limiter
* Allow a9 or b11 to meet base EHR criteria after b11 start day
* Handle surveillance requirements/non-conformity based on surveillance dates
* Add Risk Management Summary Information to Questionable URL Report
* Add Risk Management Summary Information as criteria attribute

### Bug fixes
* Handle mixed case Conformance Methods
* Handle blank values on removed criteria that are not validated

### Flagged Features
* Implement basic Cognito authentication/authorization

---

## Version 42.4.0
_13 November 2023_

### Features
* Update structure of CertificationStatusEvent and CertifiedProductTestingLab objects
* Remove "unattested criteria" from Listings in data
* Add new download endpoints to download by edition or status
* Download file creation broken down by Active, Inactive, 2014, 2011
* Include all active listings in attestation warnings calculation
* Add "Editable" attribute to Criteria

### Flagged Feature
* When editing a listing, stop calculating Cures status events

### Bug Fixes
* Cures Update Report not generating data past August

---

## Version 42.3.0
_16 October 2023_

### Features
* Correctly handle cached Test Tools and Functionalities Tested when modified
* Remove edition from quartz reports where possible
* Parse criteria from upload file based on active date
* Allow duplicate test data with different versions
* Allow test tools with same name and different versions
* Add 'status' field to criterion for retired, active, removed
* Add business error if func tested is unavailable by date

---

## Version 42.2.0
_2 October 2023_

### Features
* Add GET optional-standards endpoint
* Add GET conformance-methods endpoint
* Deprecate allowed* fields of a certification result

---

## Version 42.1.0
_18 September 2023_

### Features
* Create admin tool to manage Functionality Tested for new HTI-1 rule
* Remove required date from Test Tools
* Deprecate decertificationDate, replace with decertificationDay
* Use '15' as default edition code always
* Add start/end date params for criteria active during range
* RWT Report Service looks at Active listings vs 2015 edition
* Consider active listings in ICS Errors Report, not edition
* Consider active listings vs 2015 edition in broken surv report
* Only consider listing status (not edition) in Bad URL Report
* Only consider active status in attestation report calculator
* Listing validation report includes all active listings

### Bug Fixes
* Fix how Service based URL List urls with colon is sent to Datadog

---

## Version 42.0.0
_5 September 2023_

### Breaking Changes
* Remove /data/test_functionality and /data/qms_standards
* Remove allowedTestFunctionalities, testFunctionality fields
* Remove ics_relationships endpoints
* Remove /certified_products?versionId=<id> call

### Features
* Remove unused code related to the Cures Statistics Report
* Add endpoint to GET /certification-criteria
* Add startDate, endDate, and rule to criterion objects
* Deprecate certificationEdition in favor of edition in listing details
* Deprecate ceritficationEditionId and year of edition object
* Allow edition to be null in listing search results

### Flagged Features
* Allow upload, confirm, and get of listing without edition

---

## Version 41.2.0
_21 August 2023_

### Features
* Create listing details download files in JSON format
* Update RemoveCriteriaJob to remove b6.

### Flagged Features
* Require subscriber role on confirm and not on subscribe
* Remove b6 from CSV definition post ERD Phase 3
* Add GET subscribers/<id> and subscribers/<id>/subscriptions

### Bug Fixes
* Fix issue where Surveillance Downloadable Resource Creator job uses all memory and kills Tomcat
* Fix runtime error during the Listing Validation Report Creator job
* Remove Singluar annotation from testing labs, ICS source, targeted users; fixes issue with multiple row in upload file

---

## Version 41.1.0
_7 August 2023_

### Features
* Create admin tool to manage Test Tools for new HTI-1 rule

### Bug Fixes
* Complaints search openDuring param correctly includes end date
* Populate listing store with cert status change reason; remove it for anonymous users
* Remove data under unattested criteria in normalizers

---

## Version 41.0.0
_24 July 2023_

### Breaking Changes
* Remove deprecated endpoints eligible for deletion

### Features
* Track and analyze (g)(10) Service Base URL List availability
* Add certificationDay attribute to listing/id/ics-relationships response
* Add certificationDay property of listing details

### Flagged Features
* Add endpoint to unsubscribe-all subscriptions

---

## Version 40.7.0
_3 July 2023_

### Features
* Include messages for each allowed response of an attestation

### Bug Fixes
* Allow confirming listing with a new developer
* Send email when attestation Change Request is withdrawn

---

## Version 40.6.0
_26 June 2023_

### Features
* Use retirementDay instead of retirementDate for ONC-ATLs
* Deprecate retirementDate in favor of retirementDay for ONC-ACBs
* Remove unused ONC-ATL accredidation_number field
* Create POST /subscriptions endpoint to allow subscribing

### Flagged Features
* Add endpoint for available subscription reasons

---

## Version 40.5.0
_12 June 2023_

### Features
* Include ONC-ACBs active during quarter in surveillance activity report
* Developer URL Uptime report based on existing CHPL URL monitors
* Remove ROLE_ONC_STAFF capabilities
* Remove ROLE_ATL
* Require acknowledgement of warnings on a change request
* Warn ONC-ACBs about inconsistent attestations responses

### Flagged Features
* Ignore most assurances warnings until erd-phase-3

### Bug Fixes
* Generate Questionable Activity description more correctly

---

## Version 40.4.0
_30 May 2023_

### Features
* Allow Admin and ONC users to edit Listing with validation errors in cases of emergency
* Create questionable-activity/search and /trigger-types endpoints

---

## Version 40.3.0
_15 May 2023_

### Features
* Update UrlStatusCollectorJob to not log errors when URL is bad
* Add elements to /search/v3 endpoint to support SVAP search
** Add hasAnySvap filter on listing search v3 endpoint
** Add SVAP filtering options to /search/v3 endpoint
** Add SVAPs and SVAP Notice URL to listing search response
* Stop checking for 'Certifications Added' Questionable activity

### Bug Fixes
* Search results do show Listing updates in a timely manner
* Refactor SED statistics collection in the ChartDataCreatorJob to use less memory

---

## Version 40.2.0
_1 May 2023_

### Features
* Deprecate a number of /auth endpoints
* Email users whenever their password is changed
* Update Account Locked email to newer HTML format
* Update reset password email to newer HTML format
* Deprecate lastModifiedDate field of basic listing object
* Deprecate lastModifiedDate field of listing details

---

## Version 40.1.0
_19 April 2023_

### Features
* Improve g3Sed download file creation job
* Allow developers to acquire other developers and retain their code
* Use newer HTML email format for split developer results
* Remove updateParticpantsJob; Add fixupQuestionableActivityJob
* Cures Update report only includes b10 criteria
* Adjust questionable activity for criteria added/removed

### Bug Fixes
* Make copy of cert status events to check questionable activity

---

## Version 40.0.0
_3 April 2023_

### Breaking change
* Remove eligible deprecated endpoints and response fields

### Features
* Add complaint search filters for Listing ID, Surveillance ID, and Certification Criterion ID
* Remove Developer Attestation report
* Merge information from Developer Attestation report into the Developer Check-in Attestation Report
** Adds informational messages about Developers' Listings and their Attestation Responses

### Bug Fixes
* Handle "Jira Unavailable" better in Listing Search Service
* Update Shared Store to handle concurrency issues better

---

## Version 39.6.0
_20 March 2023_

### Features
* Use CAP Status to determine various direct review date values
* Add listingIds parameter to /search/v3 endpoint

### Bug Fixes
* Correctly read developer-associated listings from the shared store
* Correctly use DR container object to calculate total DR statistics

---

## Version 39.5.0
_6 March 2023_

### Features
* Add summary data to Real World Testing report
* Re-organize system jobs to avoid running together
* Add ADMIN-only ability to download CSV used to create listing
* Version the change-requests/search endpoints and add a v2
* Version the /complaints/search endpoints and add a v2
* Version the GET /developers/search endpoint and add v2
* Deprecate listing search service v2 in favor of /search/v3

### Bug Fixes
* Handle null developer contact name in Developer Access Report
* Product ownership change should correctly record listing activity
* Ensure a developer cannot have 0 products
* Support blank contact fields supplied during product edit
* Place the ICS error indicator in correct column in RWT Email

---

## Version 39.4.0
_21 February 2023_

### Features
* Add error message when Surveillance Requirement or Non-conformity does not match Listing Edition
* Add system job to correctly deduplicate participants
* Add check for missing ICS Surveillance to ICS Errors Report
* Remove "no longer needed" one-time jobs

### Bug Fixes
* Fix display of Non-Conformity Types on Surveillance Reports

---

## Version 39.3.0
_6 February 2023_

### Features
* Remove security restrictions for the SVAP download file
* Synchronize method to allow for better results during cache refreshes
* Add GET /functionalities-tested endpoint
* Deprecate testFunctionality and allowedTestFunctionality

---

## Version 39.2.0
_23 January 2023_

### Features
* Remove Listings from Shared Store related to Direct Reviews
* Stop accepting measures at the criteria level during upload
* Create a system job to backfill 'Cures Update' Questionable Activity
* Detect Questionable Activity if Cures Update is Removed when a listing is updated
* Add /qms-standard endpoints supporting CRUD operations
* Add new ics-relationships endpoints for listings
* Deprecate /certified_products/{id}/ics_relationships endpoints
* Give ROLE_STARTUP permission to run DRCacheRefresh job

### Bug Fixes
* Clear shared store listings in ICS Family of edited listing

---

## Version 39.1.0
_9 January 2023_

### Features
* Create job to allow sending of emails from CHPL to Developer users that haven't submitted Attestations
* Restore ability to use removed criteria to generate 15E CMS IDs
* Add totals to Developer Attestations Check-in Report
* Add /search endpoint that uses /search/v2 per new CHPL standard
* Add property to configure whether emails are deleted from sent
* Treat all date range searches as exlusive of the start/end dates
* Remove invalid date order check from search validation
* Add new sorting options for the listing search/v2 endpoint
* Only check Listing URLs for Listings with Active Certificates
* Remove "copyright" notification from email footers

### Bug Fixes
* Correctly handle SED task/participants on listing edit and upload
* Put updated data in Questionable URL report when job runs

---

## Version 39.0.0
_12 December 2022_

### Breaking Change
* Remove bulk Surveillance functionality and associated endpoint

### Features
* Add new endpoints for operations on Accessibility Standards management
  * Deprecate GET /data/accessibility_standards
* Deprecate startDate and endDate fields of Quarterly Reports
* Deprecate GET /surveillance-report/quarterly/{id}/complaints
* Improve performance of Complaints search
  * Add complaints /search endpoint
  * Deprecate GET /complaints endpoint
* Deprecate all /filters endpoints and /data/filter_types
* Look up CQM Criteria by ID rather than number
* Remove the system job to Invite Developers
* Deprecate POST /key endpoint
* Convert email sending to use Microsoft Graph modern auth
* Surveillance on a removed Criteria is no longer Questionable Activity
* Surveillance can be attached to Listing for Criteria that is not attested to

### Flagged Features
* Generate Certification IDs with "15C" syntax

### Bug Fixes
* Nonconformity Chart is not showing when a new surveillance is created
* Use correct URL in API Key registration email

---

## Version 38.0.0
_28 November 2022_

### Breaking Changes
* Remove a variety of deprecated endpoints and response fields

### Features
* Update Cures Statistics query to run on the servers using Postgres 14
* Add trigger key fields to scheduled system job endpoint response
* Add Test Data to b7 and b7 Cures Criteria
* Deprecate sedTestingEndDate -> sedTestingEndDay for listings
* Deprecate the /data/fuzzy_choices endpoints
* Alter UCD Process validation during upload and edit
* Add GET, PUT, POST, DELETE endpoints for /ucd-processes
* Deprecate GET /data/ucd_processes endpoint

---

## Version 37.5.0
_14 November 2022_

### Features
* Only run validators against Active Certificates
* Update API architecture for Surveillance domain; deprecated a variety of surveillance related values
* Add one-time job to back-fill Questionable Activity for RWT Activity outside normal periods
* Add decertification date range parameters to /search/v2 endpoint.
* Ensure product owner history has distinct transferDay per entry
* Deprecate product owner history transferDate for transferDay

---

## Version 37.4.0
_31 October 2022_

### Features
* Only check active criteria for d12/d13 attestation requirement
* More leniently parse measure types and criteria numbers

### Flagged Features
* Stop giving listing validation warnings for removed criteria
* Update required/related rules based on grace period

### Bug Fixes
* Do not save Developer, Product and Version updates if no meaningful change is made
* Do not allow developers to be created with duplicate names
* Require a developer's first status to be 'Active'.

---

## Version 37.3.0
_17 October 2022_

### Features
* Include removed criteria in Cures Update calculation

---

## Version 37.2.0
_3 October 2022_

### Features
* Add (Cures Update) title to relevant criteria in CSV definitions and sort order
* Use ET for change request search result timezone
* Create GET /developers/search endpoint
* Deprecate GET /collections/decertified-developers
* Create DELETE endpoint for SVAPs using just the ID

### Bug Fixes
* Record listing activity on dev/prod/ver merge and split
* Set product id on split/merge for shared store update
* Set version id on split/merge for shared store update
* Display correct error on DELETE of SVAP if its used in any listings

---

## Version 37.1.0
_19 September 2022_

### Features
* Re-enable Developer Attestation Reports
* Changes to RWT URLs or Check Dates outside normal dates generate Questionable Activity
* Deprecate GET /certification_ids and replace with POST /certification_ids/report-request

### Bug Fix
* Add validation connecting complaint surveillances to listings

---

## Version 37.0.0
_6 September 2022_

### Breaking Changes
* Remove all legacy pending listing endpoints, upload template versions, and related code

### Features
* Revise logic for "Associated ONC-ACBs" on change requests to be determined by the type of change request
* Update Developer Attestations submission instructions
* Send email to developer user(s) when attestation submission is re-opened
* Track "Leidos" Cures Update data in progress reports
* Set "attestation exception window" to five calendar days
* Create POST /change-requests/report-request endpoint to allow users to download all of their change requests
* Update removeCriteriaJob for ERD Phase 2 requirements

---

## Version 36.0.0
_22 August 2022_

### Breaking Change
* Revise Attestations Language and Responses for next Attestations period

### Features
* Replaced SVAP values should be allowed for new Listings with ICS
* Update RWT Eligible Criteria based on year
* Allow G1/G2 measures to be parsed at Listing level in during upload
* Update RWT Report to use new message about a Listing being withdrawn instead of errors or warnings
* Include all Withdrawn Listing statuses in the RWT Report Email
* Add Certification Date and Developer Users as columns to the RWT Report

---

## Version 35.9.0
_8 August 2022_

### Features
* Add complaints report email quartz job
* Add POST /complaints/report-request endpoint to allow users to get the new complaints report
* Use Shared Store for Listing details

---

## Version 35.8.0
_25 July 2022_

### Features
* Attestations from previous periods can be modified
* Add Product, Product Database ID, Version, and Version Database ID to the SVAP Summary Download file
* Remove deprecated fields surveillance "authority" and "userPermissionId"

### Bug Fixes
* Fix error during editing of SED tasks

---

## Version 35.7.0
_11 July 2022_

### Features
* Add Developer Users and Surveillance Count columns to Developer Attestations Report
* Deprecate requirementName and nonconformtyTypeName fields of surveillance responses. Deprecate endpoints to add and remove nonconformity supporting documents.

### Flagged Features
* Allow only valid criteria to be used in making CMS IDs

### Bug Fixes
* Add validation for surveillance requirement type + criterion

---

## Version 35.6.0
_6 July 2022_

### Features
* Create system job to add missing measures to listings

---

## Version 35.5.0
_27 June 2022_

### Features
* Deprecate GET /change-requests endpoint and add GET /change-requests/search endpoint
* Add Direct Review open/closed non-conformity counts to Developer Ban Email

---

## Version 35.4.0
_13 June 2022_

### Features
* Deprecate developerId field for Developer and remove usage in API
* Deprecate productId field for Products and remove usage from API
* Deprecate versionId field for Version and remove usage from API

### Flagged Feature
* Handle removed state of Conformance Methods

---

## Version 35.3.1
_6 June 2022_

### Bug Fixes
* Update Developer-Associated Listing Jira field ID
* Describe change in ONC-ATLs better in Questionable Activity report
* Allow scheduled job to delete unused API Keys as intended

### Flagged Features
* Combine Developer Details and Website Change Request to Developer Demographics Change Request

---

## Version 35.3.0
_31 May 2022_

### Features
* Create Attestations Check-in Report
* Add Cures Reports to show statistics over time and remove CSV files from email
* Validate direct reviews before loading them into cache
* Remove some fields from Direct Review data
* Remove promoting-interoperability flag usage. Remove deprecated meaningfulUseUserHistory field from responses.
* Add /listings/pending/{id}/submitted endpoint to return listing data as it was entered by a user during upload
* Update conformance method parsing to use "Test procedure" and "Test procedure version" columns

### Flagged Features
* Update the CSV Definition file to not include removed criteria as of ERD-Phase-2
* Add validation for f3 conformance methods with and without GAP.

### Flagged Bug Fixes
* Set allowable fields to non-null for all attested criteria

---

## Version 35.2.0
_16 May 2022_

### Features
* Add new job to convert Test Procedures to Conformance Methods
* Make Listing Cache persistent across Tomcat reboots
* Create one-time job to repair erroneous Cures update Listing Activity
* Add job to populate CHPL Product Number History

---

## Version 35.1.0
_5 May 2022_

### Feature
* Improve performance of /change-requests network call

### Flagged Feature
* Allow editing of Attestation change requests

---

## Version 35.0.0
_2 May 2022_

### Breaking Changes
* Remove deprecated endpoints and response fields eligible for removal as of 4/15/22.

### Features
* Default to ET when managing Announcements

---

## Version 34.3.0
_13 April 2022_

### Features
* Modify Cures Reporting for ONC-ACB changes to remove ONC-ACB "UL, LLC"
* Calculate and save CHPL Product Number history when Listings are updated

### Bug Fixes
* Record listing activity on Developer split
* Record listing activity on Developer merge

---

## Version 34.2.0
_1 April 2022_

### Features
* Add Attestations Requirement and Non-conformity Type values for Surveillance
* Deprecate DELETE /listings/pending for multiple pending listing IDs
* Add parsing, validation, tests for conformance methods on flexible upload
* Add ICS column to RWT Report and add ICS as part of the reason for errors or warnings in the report.

### Bug Fixes
* Update Spring to 5.3.18 to address CVE-2022-22965

---

## Version 34.1.0
_22 March 2022_

### Features
* Add public attestations to developer details
* Create Developer Attestations report
* Add /search/v2 endpoint and deprecate /search/beta endpoint

### Flagged Features
* Add controls to grant and limit access to Attestation submission process

### Bug Fixes
* Do not include b6 in criteria that need to be updated to meet Cures certification
* Record failed login count for incorrect login attempts

---

## Version 34.0.1
_10 March 2022_

### Bug Fixes
* Upon password reset only change the password of the account associated with the token

---

## Version 34.0.0
_7 March 2022_

### Breaking Change
* Remove transparency attestation from all response data

### Flagged Features
* Do not allow editing of attestation change requests

---

## Version 33.2.0
_22 February 2022_

### Features
* Add email try/resend feature
* Add new Attestation change request submission process
* Mark all /certified_products/pending* endpoints as deprecated

### Flagged Features
* Allow confirmation of listing uploaded via flexible upload

---

## Version 33.1.0
_7 February 2022_

### Features
* Upgrade to Java 17; upgrade as many compiled plugins as possible; upgrade/declare all maven plugins

---

## Version 33.0.0
_24 January 2022_

### Breaking Change
* Remove "IS_ELIGIBLE" as query option for Real World Testing

### Features
* Re-send confirmation email on login if user hasn't confirmed
* Deprecate Transparency Attestation field

---

## Version 32.8.0
_10 January 2022_

### Features
* Prevent saving a Change Request where no data was updated

### Bug Fixes
* Split Trigger Developer Job email addresses by correct character

---

## Version 32.7.0
_20 December 2021_

### Features
* Do not allow confirming a listing with removed criteria and do not give errors for sub-data.
* Add job to add and remove G1/G2 measures
* Add job to mark (a)(10), (a)(13), (e)(2) 2015 Criteria as Removed
* Remove (b)(4) and (b)(5) from the Real World Testing eligibility check
* Update account creation/confirmation emails to use the new HTML format

### Flagged Features
* Limit Demographic Change Requests by flag

---

## Version 32.6.0
_13 December 2021_

### Features
* Update log4j libraries to 2.15
* Disallow editing of User email addresses

---

## Version 32.5.1
_9 December 2021_

### Bug Fixes
* Respect "deleted" flag for Surveillance Documents

### Flagged Features
* Add SVAP parsing and validation to flexible upload
* Add updated 2015 Edition CSV definition file

---

## Version 32.5.0
_29 November 2021_

### Features
* Create two "snapshot" jobs to present a listing and/or criteria as it was at a certain point in history.
* Deprecate certificationEditions request parameter for /search/beta API
* Deprecated /collections/certified-products and related objects; remove their usage where possible
* Add derivedCertificationEditions request parameter to /search/beta endpoint
* Added rwtPlansUrl and rwtResultUrls fields to collections and search/beta endpoint responses
* Add '2015 Cures Update' to /search/beta edition filters

### Bug Fixes
* Adjust /search/beta endpoint edition sort to take into account 2015 Cures Update

---

## Version 32.4.0
_15 November 2021_

### Features
* Deprecate "authority", "userPermissionId" fields for surveillance and update ROLE_ONC permissions to allow surveillance management
* Update security so ONC-ACBs have the ability to edit Developer users
* Modify values in "Requirement" dropdown and "Non-Conformity Type" dropdown under surveillance activity
* Add logging message when user logs in using user name

### Flagged Features
* Optional Standard parsing, validation in flexible upload

---

## Version 32.3.0
_1 November 2021_

### Features
* Add job to convert Test Standards to Approved Standards
* Improve Cures Reporting reports by adding 3 new charts
* Track and report of usage of API endpoints with deprecated response fields
* Update deprecated /search endpoint to not use nonconformity_status table

### Flagged Features
* Add "Conformance Method" to Listing details

### Bug Fixes
* Allow complaint to be associated to an open surveillance

---

## Version 32.2.0
_18 October 2021_

### Features
* Give users a date for when deprecated API endpoints may be removed
* Update Questionable Activity Report to new HTML email template
* Check listing updates for questionable activity when an ONC-ACB user updates promoting interoperability
* Do not allow edit of Certification Date field via the API

---

## Version 32.1.0
_5 October 2021_

### Features
* Upgrade Swagger library version
* Revise list of test tools to select for a 2015 Edition listing
* Update overnight job schedule and improve performance
* Migrate surveillance and non-conformity timestamp fields to LocalDate
* Remove "frequency" data from Scheduled Jobs

### Flagged Features
* Add CQM Validation to flexible upload
* Add SED validation to flexible upload

### Bug Fixes
* Restore functionality of URL Status Data Collector job
* Consistently handle valid/invalid/null ID fields during listing update

---

## Version 32.0.0
_7 September 2021_

### Breaking Change
* Create and edit of Quarterly Surveillance elements must use new fields

### Features
* Remove Banned Developer errors from the Listing Validation Report
* Automate Cures Reporting Phase 1 Charts
* Use consistent names for Quarterly Surveillance data elements

### Bug Fixes
* Display consistent permissions error when splitting Products and Versions
* Move stateful date field out of Spring component, allowing for Cures Statistics Reports to be generated correctly

---

## Version 31.0.0
_31 August 2021_

### Breaking Change
* Use "Mandatory Disclosures" instead of "Transparency Attestation URL"

### Feature
* Update Real World Testing email to dynamically determine eligibility

---

## Version 30.6.0
_23 August 2021_

### Features
* Generate SVAP Summary CSV and allow download of that file and schema
* Add new Non-conformity close date field
* Restrict Listings to have only valid Test Procedures on edit
* Update email failure with custom exception and log message
* Confirmed Listings with RWT eligible parents become RWT eligible on confirmation

### User Interface Updates
* Use updated link for ONC "Contact Us" form

### Bug Fixes
* Audit Data Retention jobs failing in PROD

### Flagged Features
* Provide appropriate errors/warnings on Test Standards on removed criteria based on ROLE

---

## Version 30.5.0
_9 August 2021_

### Features
* Add Attestation change request
* Track and notify of deprecated endpoint usage by API Key
* Add "Suspended by *" statuses to ONC-ACB Questionable URL report
* Require "Actions/Response" on closed Complaints
* Validate Complaint dates for reasonableness

### Bug Fixes
* Filter ONC-ACB Questionable URL report by Status AND Edition, not Status OR Edition

---

## Version 30.4.0
_2 August 2021_

### Features
* Change Meaningful Use User term to Promoting Interoperability; deprecated MUU fields and endpoints
* Enable Real World Testing fields for all 2015 Edition Listings

### Flagged Features
* Treat Test Standards as Optional Standards on Upload/Confirm

---

## Version 30.3.0
_12 July 2021_

### Features
* Create new search endpoint /search/beta that incorporates all types of compliance
* Deprecate /search endpoint
* Remove ability for ROLE_ONC to upload surveillance
* Add acbId to Listing Activity metadata
* Add header to all responses indicating prod/non-prod environment
* Disable Merge ability for ROLE_ACB

---

## Version 30.2.0
_28 June 2021_

### Features
* Generate Surveillance Activity Report as Excel spreadsheet
* Add Direct Reviews to Listing basic endpoints
* Require Measures with multiple criteria to be entered as a single Measure
* Allow Users without phone number

### Flagged Features
* Support add/edit/view of Optional Standards

### Bug Fixes
* Display Mandatory Disclosures URL in some error messages instead of Transparency Attestation URL

---

## Version 30.1.0
_2 June 2021_

### Features
* Add cures statistics creation and basic email report
* Update README documentation
* Add developer data to version activity when creating a version
* Incorporate listing status in certified product collection direct review counts

### Bug Fixes
* All activity metadata returned for Version and Listing activity

---

## Version 30.0.0
_17 May 2021_

### Breaking Changes
* Remove background jobs, /data/job_types

### Features
* Add Developer, Product, and Version information to complaints
* Move Surveillance Upload and Meaningful Use Upload background jobs to quartz scheduled jobs
* Allow edit/view of Service Base URL List value

### Flagged Features
* Add listing-level fields to flexible upload

---

## Version 29.3.0
_4 May 2021_

### Features
* API Key registration requires confirmation of email
* Add 'processing' flag to /certified_products/pending/metadata response

### Bug Fixes
* Remove applicable UCD Processes and Test Tasks if a criterion is edited to have SED = false
* Mark pending listing as processing during confirm

---

## Version 29.2.0
_19 April 2021_

### Features
* Add surveillance and non-conformity counts to developer hierarchy
* Do not allow new test standards during upload/edit

### Flagged Features
* Basic listing validation for flexible uploads

### Bug Fixes
* Copy developerId value so it is available in /collections/certified_products response
* Show correct criterion number when (e)(1) Cures Update is missing other required criteria

---

## Version 29.1.0
_5 April 2021_

### Features
* Update the overnight job schedule
* Remove G1/G2 Data Fix job
* Add Direct Review information to Summary Statistics report
* Display "Informed ONC per 170.523" in Complaints report
* Filter a listing's direct reviews by listing status, edition
* Add quartz job to get list of user-defined triggers in email

---

## Version 29.0.0
_22 March 2021_

### Breaking Changes
* Remove developer explanation from direct review data

### Features
* Add G1-G2 measures to basic API endpoints
* Generate email when scheduled jobs are added, updated or deleted by a user
* Allow Test Functionality to be removed for (c)(3) if criterion is removed
* Add temporary Quartz Job to Fix G1/G2 Cures Criteria Data
* Enhance the current Summary Statistics report to produce PDF
* Allow ROLE_ONC_STAFF to access Complaints
* Allow ROLE_ONC_STAFF to access quarterly surveillance data
* Add ONC-ACB specific Questionable URL report with filtering
* Put CHPL Product Number in CMS ID results data

---

## Version 28.0.0
_8 March 2021_

### Breaking Changes
* Remove /key/activity endpoints

### Features
* Update quarterly/annual surveillance reports to have 2019 and 2021+ formats
* Remove dependency on tables that used to be used for security, but are now obsolete
* Add v20 upload template parsing to include old b3 criterion
* Store HTTP Method in API Key Activity table
* Support edit and display of SVAP Notice URL

---

## Version 27.2.0
_22 February 2021_

### Features
* Give ROLE_ONC_STAFF access to scheduled and background jobs
* Enable Standards Version Advancement Process Administration
* Add /collections/certified-products endpoint including Direct Review data for listings
* Create scheduled job to identify any listings that fail current validation rules

---

## Version 27.1.0
_8 February 2021_

### Features
* Add Direct Review and Non-conformity counts to downloadable CSVs
* Add Direct Reviews into Listing XML download file

### Flagged Features
* Create endpoints for flexible upload of Listings

### Bug Fixes
* Questionable Activity Report - activity details missing for old G1-G2 measure activity
* Legacy + Cures criteria get associated with measure on upload

---

## Version 27.0.0
_25 January 2021_

### Breaking Changes
* Developer PUT endpoint for developer updates and merges was modified

### Features
* Add reports on missing / soon-to-be-missing RWT Plans & Results
* Update validation for b10 and g10 criteria
* Execute developer merge as background quartz job
* Change 2014 Download file generation to be quarterly
* Ensure email is used when username does not exist
* Add /activity/metadata/beta/api-keys endpoint for paged API Key activity
* Allow edit of SVAP information for listings

### Flagged Features
* Only put RWT data in downloadable CSV files when flag is enabled

---

## Version 26.2.0
_11 January 2021_

### Features
* Add scheduled job that allows audit data to be archived

### Bug Fixes
* Able to add and remove nonconformity in a single action
* Add null check for surveillance requirement comparison

---

## Version 26.1.0
_28 December 2020_

### Features
* Update statistics history to use certification status history table

---

## Version 26.0.0
_14 December 2020_

### Breaking Change
* Move G1/G2 macra measures to measures at listing level

### Features
* Generate direct review download file and manage DR cache
* Enable ROLE_ONC_STAFF user type
* Make "username" not required for new users

---

## Version 25.17.0
_30 November 2020_

### Features
* Add upload for Real World Testing
* Stop showing "System Administration" accounts on User Management page
* Stop allowing "System Administration" accounts to log in

---

## Version 25.16.0
_16 November 2020_

### Features
* Add new endpoint to get developer hierarchy
* Remove friendly name from developer and product points of contact

### Bug Fixes
* Correct the wrong URLs in few emails
* Re-order "reason" element for dev status changes in XML

---

## Version 25.15.0
_19 October 2020_

### Features
* Add ability to validate and save Real World Testing data
* Send email to Direct Review/Jira team upon developer split
* Revise email subject and body for shared ONC-ACB reports

---

## Version 25.14.0
_5 October 2020_

### Features
* GAP=TRUE should not be allowed for 170.315 (f)(3) during upload, confirm or edit of listing
* Reschedule 2014 Download file generation to be monthly
* Allow users to log in via either username or email address

### Flagged Features
* Add error handling for any error returned by Jira with respect to Direct Review

### Bug Fixes
* Do not mark old ICS error data deleted until job completes
* Lookup user by email or name for password reset
* Do not mark old broken surveillance rule data deleted until job completes

### Flagged Bug Fixes
* Re-enable Pending "Change Request" report for ONC-ACBs

---

## Version 25.13.0
_21 September 2020_

### Features
* Clean up duplicate ONC and ONC-ACB scheduled jobs report
* Send email to user when account is first locked
* Return ambiguous msg on login if acct locked, disabled, etc
* Add new /metadata/beta activity endpoints for pending listings, pending surveillance, corrective action, atls, and announcements

### Bug Fixes
* Allow complete listing file to always be downloaded

---

## Version 25.12.0
_8 September 2020_

### Features
* Add "Ignore warnings" checkbox when editing or confirming certified product

### Flagged Features
* Get direct reviews from Jira

### Bug Fixes
* Send HTTP Error 401 when the api key is not valid

---

## Version 25.11.0
_18 August 2020_

### Features
* Create report showing developer user access
* Remove cures-specific jobs

### Bug Fixes
* Require Test Procedure & Data Versions on confirmation
* Remove no longer required Transparency Attestation validation logic
* Generate valid XSD for download files
* Include curesUpdate true/false value in XML download

---

## Version 25.10.0
_10 August 2020_

### Features
* Add Quartz job to set Real World Testing Eligibility Year for eligible listings
* Add /metadata/beta/users endpoint for paged user activity
* Remove ERD and ERD+1 week flags
* Updated Quartz jobs to handle updated ONC-ACB selection requirements

### Bug Fixes
* Correctly handle error with k2 surveillance requirement
* Upgrade jackson-core library to be compatible with databind lib
* Show "Cures Update" on Broken Surveillance Report

---

## Version 25.9.0
_27 July 2020_

### Features
* Add 2015 Cures Edition metrics to Summary Statistics Report
* Update summary statistics creator job to be non-blocking
* Check for duplicate data on listing confirm and edit
* Move data loaded on startup to caching framework
* Updated text on user invitation email messages

### Flagged Features
* Add endpoint to get direct reviews for any developer

### Bug Fixes
* Unlock user accounts on successful password reset
* Allow users to change their passwords using provided workflow

---

## Version 25.8.0
_13 July 2020_

### Features
* Split developer action moved to a quartz job
* Do not allow ONC to upload listings
* Generate warning on upload when test standard does not exist
* Create secured endpoint /activity/metadata/beta/acbs

---

## Version 25.7.1
_25 June 2020_

### Bug Fixes
* Update removeCriteriaJob parameters

---

## Version 25.7.0
_24 June 2020_

### Features
* Add "Removed | " to removed criteria in the SED download CSV file
* Update Macra Measures to match final Cures rule

### Bug Fixes
* Correct macra measure comparison logic

---

## Version 25.6.0
_15 June 2020_

### Features
* Update G3Sed2015DownloadableResourceCreatorJob to be non-blocking
* Update DownloadableResourceCreatorJob to be non-blocking
* Restrict use of old Criteria based on Certification Date & ICS
* Allow developer split without considering retired listings

### Flagged Features
* Use updated titles for new criteria

### Bug Fixes
* Provide error for invalid additional software source grouping length
* Show full criterion (incl. Cures Update) for test functionality mismatch
* Inappropriate test functionality should not be saved

---

## Version 25.5.0
_1 June 2020_

### Features
* Reset user's failed log in account when their accout is unlocked
* Stop distinguishing between bad un/pw combination and non-existent user
* Update BrokenSurveillanceRulesCreatorJob job to be non-blocking
* Add /activity/metadata/beta/versions endpoint

### Flagged Features
* Stop generating some stats for 2014 Charts

### Bug Fixes
* Lockout user after too many failed login attempts
* Update validation of user data when updating user and fix security responses on User endpoints
* Add e2 criterion into v19 upload template

---

## Version 25.4.0
_18 May 2020_

### Features
* ONC-ACB should only be able to add or remove criteria if listing is in "Active" status
* Show repeated information in Questionable URL report
* Provide better error message for QMS Standards that are too long in upload

### Bug Fixes
* Allow addition of active Macra Measures by ROLE_ACB
* Reduce incorrect "activity" in Developer reports
* Show all surveillance update activity in listing reports

---

## Version 25.3.0
_4 May 2020_

### Features
* Add inactive date, decertified date, and ids to generated CSVs
* Add /activity/metadata/beta/products endpoint
* Send individual emails when developer ban may be triggered
* Add comma between developer address line 1 and line 2 in CSV download
* Add 'Activity Level' column to questionable activity report
* Add /activity/metadata/beta/developers endpoint

### Flagged Features
* Require d12 and d13 when editing certain 2015 listings in certain scenarios

### Bug Fixes
* Remove requirement for d4 from certain a criteria
* ACBs receive error when editing a listing with removed MACRA measures
* Questionable URL Report Generator not sending report
* Use correct URL for listing report in questionable activity email

---

## Version 25.2.0
_20 April 2020_

### Features
* Add Quartz job to update listings to be marked as Cures Update
* Change the order of display of revised criteria so "Cures Update" is first
* Remove no longer needed Quartz job

### Flagged Features
* Display "Cures Update" everywhere it's appropriate
* Add "Developer Details" Change Request type

---

## Version 25.1.1
_10 April 2020_

### Bug Fixes
* Allow application startup despite missing criteria

---

## Version 25.1.0
_8 April 2020_

### Features
* Report on pending "Change website" requests for ONC-ACBs
* Add Specific d2, d3, d10 Activity to Questionable Activity Report
* Include '(Cures Update)' in criteria error/warning messages

### Flagged Features
* Update requirements for new certificates with respect to d12 and d13
* Support display of Listing's Cures Update status

### Bug Fixes
* Lookup g1/g2 measures, test proc, test data by criterion ID
* Fix bug preventing ACB from approving a change request

---

## Version 25.0.0
_23 March 2020_

### Breaking Change
* Handle duplicate criteria numbers in CMS ID generation

### Features
* Remove the ability for ONC-ACBs to add/edit values for the test functionality field from (c)(3)
* Update quartz job to handle new criteria
* Add V19 Upload Template scheduled job.
* Add criterion objects to surveillance reqs and nonconformities
* Update job to add new Test Functionalities as required

### Flagged Features
* Return errors when attesting to both old and new criteria
* Add d2, d3, and d10 revised to 2015 CSV Definition file
* Add new CSV definition file with updated fields for new and removed criteria
* Update transparency attestations to work in accordance to new Cures rule
* Disallow generation of CMS EHR IDs containing any 2014 Edition Listings
* Mark any modification of 2014 Edition Listings as "Questionable Activity"
  * Requires a "Reason for change" for any such edits, in a similar fashion to existing functionality with 2011 Edition Listings
* Update 2015 CMS EHR ID generation to handle removed and added criteria
* Restrict ONC-ACB Users from modifying any 2014 Edition listings
* Add schedulable job to update 2014 listing status
* Add new error to Inheritance Errors Report when ICS=0 and Gap=1 for any criteria and certification date after Cures rule effective date
* Restrict ONC-ACB Users from adding, editing, or removing any criteria marked as removed
* Restrict ACBs from altering Transparency Attestation
  * Restrict ACBs from altering Transparency Attestation in all locations found
  * Externalize developer validations
  * Return all attestations no matter who is logged in
* Stop allowing ONC-ACBs to add/edit surveillance and/or non-conformities associated to 2014 Edition Criteria
* Update requirements on required criteria
* Change validation based on criteria relationships

---

## Version 24.0.0
_9 March 2020_

### Breaking Change
* Update various endpoints to return ID instead of Number for criteria

### Features
* Update job to remove more macra measures
* Expand quartz job to cover more data related to new Cures specific criteria
* Update quartz job to modify test functionality related to e1
* Allow upload of v18 2015 listing template with b3, d12, d13
* Allow edit and view of Cures revised criteria

### Flagged Feature
* Restrict surveillance editing for things removed from the Program

### Bug Fixed
* Add BOM to allow for display of extended characters in CSV files

---

## Version 23.1.0
_20 February 2020_

### Bug Fixes
* Improve performance of some developer, product, and version API calls

### Flagged Features
* Add updateMacraMeasuresJob to update g1g2 MACRA measures from ACI to PI

### Features
* Discontinue validation checks against criteria marked as removed.
* Add new criteria data types to URL report

---

## Version 23.0.0
_11 February 2020_

### Bug Fixes
* Allow generation of downloadable files

### Features
* Support view of all system triggers on Scheduled Jobs page
* Add job to remove the 2015 Upload Template (v17)
* Add "Self-developer" as field for Developers
* Add Quartz Job to insert new Test Tool
* Add job to modify Test Functionality name/number

### Breaking Change Features
* Update Transparency Attestation to have a new format

---

## Version 22.1.0
_27 January 2020_

### Features
* Add job to create four new criteria b10, d12, d13 and g10
* Add endpoint to get listing activity metadata with paging
* Add Certification Status to Listing object; updates XML structure
* Add some more statistics to the Weekly Summary Statistics report

### Bug Fixes
* Allow ONC and ONC-ACB to split Developers again

---

## Version 22.0.0
_2 January 2020_

### Features
* Allow Admin/ONC users to edit listing if developer is banned
* Prevent ONC-ACBs from adding/editing surveillance referencing removed criteria
* Remove "Complaint Status Type" as distinct concept

---

## Version 21.16.0
_20 December 2019_

### Features
* Add questionable activity when b3 is added to listing after 1/1/2020

---

## Version 21.15.0
_16 December 2019_

### Features
* Update Questionable Activity Report to support a user defined range of days
* Add job to remove RT13 EC Macra Measure

---

## Version 21.14.0
_2 December 2019_

### Features
* Add scheduled job to update 2014 listing status
* Add a scheduled job that will remove 2014 upload templates
* Stop displaying "removed" criteria in download files or on Charts
  * Add new (flagged) CSV definition file to be used when criteria are removed
* Add a 'removed' field to macra measures
* Add job to mark specific 2015 criteria as removed

---

## Version 21.13.0
_18 November 2019_

### Features
* Add endpoint to retrieve API Key activity metadata
  * Update API Key calls to follow the metadata/details pattern used in other reports
* Create report for pending Website Change Requests

---

## Version 21.12.0
_4 November 2019_

### Features
* Do not require test tools or UCD Processes for 2014 listings that have ICS

---

## Version 21.11.0
_24 October 2019_

### Features
* Add Quartz job that will update a set of listing's status
* Add User scheduled job to generate and send a report of Pending Change Requests

---

## Version 21.10.0
_21 October 2019_

### Flagged Features
* Add ROLE_DEVELOPER User type
* Allow ROLE_DEVELOPER to create "Change Requests" to change their Organization's website
* Update objects in basic Certified Product response to not have errors/warnings inappropriately

### Bug Fixes
* Fix error/warning update exception and count discrepancy
  * Fix exception thrown when updating pending listing error/warning counts
  * Fix warning count discrepancy of auto-fixed warnings in confirm listings screen due to failed pending listing confirmation
* Display CHPL Product Number change popup when oldest active status date is changed

---

## Version 21.9.0
_7 October 2019_

### Features
* Give errors on confirm if missing developer data
* Add scheduled jobs to gather and report on questionable urls
  * System job to gather questionable URL data runs at 0330 GMT
  * User-triggered job to email questionable URL report runs on demand

### Bug Fixes
* Log appropriate activity upon user deletion
  * Only log one activity when a user is deleted
  * Update activity description to match in both user delete scenarios

---

## Version 21.8.0
_23 September 2019_

### Flagged Feature
* Allow users to enter and retrieve surveillance reporting data

---

## Version 21.7.0
_10 September 2019_

### Features
* Update the user to
  * Track the last logged in date
  * Return the list of organizations the user is associated with
* Allow retrieval of deleted API keys when retrieving all keys
* Move announcements security into CHPL custom framework

### Bug Fixes
* Allow upload of supporting surveillance documentation
* Reschedule nightly quartz jobs to run serially

---

## Version 21.6.0
_26 August 2019_

### Features
* Remove address re-use between organizations

### Bug Fixes
* Summary Statistics error when listing w/o status exists (listing was deleted)

---

## Version 21.5.0
_12 August 2019_

### Flagged Features
* Support entry and edit of complaints

---

## Version 21.4.0
_29 July 2019_

### New Features
* Remove "insert missing split activity" job
* Add fields for open/closed surveillance counts and surveillance dates

### Bug Fixes
* Update permissions for announcements metadata activity endpoint

---

## Version 21.3.0
_22 July 2019_

## New Features
* Add three more optional fields to the /collections/certified_products endpoint
  * openSurveillanceCount, closedSurveillanceCount, surveillanceDates
* Remove insertMissingSplitActivity quartz job
* Add new endpoint for activity metadata for announcements. Endpoint is:
  * /activity/metadata/announcements

---

## Version 21.2.0
_17 June 2019_

### New Features
* Add endpoints to provide activity metadata for users, announcements, pending listings, corrective action plans, and pending surveillances
* Add activity for split actions to allow for better history display in listing 'eye'
* Add temporary quartz job to insert missing split activity. Can only be run by ADMIN and should be removed in a following release
* Add /versions/{versionId}/split endpoint

---

## Version 21.1.0
_3 June 2019_

### New Features
* Add FF4j framework (feature flags)
* Add endpoints to provide ability for users to save filters for admin reports
* Updated product and version activity metadata to parse activity for merges and splits; better parsing of product and developer names if they have been deleted

### Bugs Fixed
* Correctly handle scheduled job update when an ACB is renamed

---

## Version 21.0.0
_20 May 2019_

### Backwards compatibility breaking features
* Moved permissions from many-to-many relationship with users to a one-to-many so that each user may only have one role. Invitations are sent slightly differently and logic enforces a user having a single role within the system while ACB and ATL users may still have access to multiple ACBs and ATLs. Returned user data is also slightly different as it has only a single role per user rather than a set of granted permissions. Affected endpoints include:
  * /users/invite
  * /users
  * /acbs/{id}/users
  * /atls/{id}/users

### New features
* Add endpoints to provide ability for users to save filters for admin report
* Updated product and version activity metadata to parse activity for merges and splits; better parsing of product and developer names if they have been deleted
* Added endpoints for ACB and ATL activity metadata
  * /activity/metadata/acbs
  * /activity/metadata/acb/{acbId}
  * /activity/metadata/atls
  * /activity/metadata/atl/{atlId}
* Deprecated existing endpoints for ACB and ATL activity
  * /activity/acbs
  * /activity/acb/{acbId}
  * /activity/atls
  * /activity/atl/{atlId}

---

## Version 20.10.0
_8 May 2019_

### New Features
* Added endpoint /collections/decertified-developers to improve response time getting that data. This endpoint will eventually replace /decertifications/developers which has been deprecated
* Updated security for edit/split/merge of developers, products, and versions

---

## Version 20.9.1
_25 April 2019_

### Bug fixes
* Convert error about g1/g2 for 2014 listings back to warning

---

## Version 20.9.0
_22 April 2019_

### New features
* Quartz jobs that gather data delete old data and insert new data within a single transaction
* Support clustering of Quartz schedulers

---

## Version 20.8.0
_8 April 2019_

### New features
* Add /system-status API call which returns the combined server up/down status and the cache initliazing/ok status
* Deprecated /status and /cache_status API calls. Will be removed in a future release
* Add new API call /certified_products/pending/metadata for high-level information about pending listings. /certified_products/pending is now deprecated
* Add new endpoints for activity metadata for lisings, developers, products, and versions. Endpoints are:
  * /activity/metadata/listings
  * /activity/metadata/listings/{id}
  * /activity/metadata/developers
  * /activity/metadata/developers/{id}
  * /activity/metadata/products
  * /activity/metadata/products/{id}
  * /activity/metadata/versions
  * /activity/metadata/versions/{id}
* Add new activity details endpoint to get the detailed activity json
  * /activity/details/{id}

### Bug Fixes
* Upload surveillance job properly runs when there is an invalid CHPL Product Number in the upload file

---

## Version 20.7.0
_1 April 2019_

### Refactoring Changes
* Removed unused references to certification status in pending listing objects

---

## Version 20.6.0
_27 March 2019_

### New features
* Add error message for unparseable certification date on Listing upload
* Allow certain users to impersonate certain other ones
* Modify security to use CHPL managed user/ATL relationship

### Refactoring changes
* Removed unused "compliance terms accepted" references

### Bug Fixes
* Fixed error on banned developer page when banned developer has listings without meaningful use counts
* Use correct ROLE for surveillance authority

---

## Version 20.5.1
_11 March 2019_

### Bug Fixes
* Fix incorrect developer appearing in product owner history after developer split

---

## Version 20.5.0
_11 March 2019_

### New Features
* HTTP caching of several resources that are pretty static
* Update subject and email text of overnight surveillance broken rules report and all surveillance broken rules report
* Add developer split functionality
* Display more helpful error message when a Reason for Change is required
* Modify security to use CHPL managed user/ACB relationship
* Update validation of test tools based on workflow and ICS

---

## Version 20.4.0
_25 February 2019_

### New features
* Allow upload / confirm of macra measures RT 13, 14, 15
* Update behavior of pre-loaded caches which will affect the /cache_status call
  * All pre-loaded caches have an additional copy loaded in the background when their data changes. The background cache is then swapped with the live cache when necessary
  * The /cache_status call was reporting OK after initial startup but would later report INITIALIZING if user actions changed any cached data. Now once it reports OK it should continue to do so
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
* Allow newly added ACBs and ATLs to have users added to them
* Remove duplicate G1/G2 macra measures and warn the user for criteria that are not attested to

---

## Version 20.2.0
_28 January 2019_

### New Features
* Added ROLE_ONC user role. ROLE_ONC replaces the existing ROLE_ADMIN and all relevant accounts were converted to the new role
* Removed references to ROLE_ONC_STAFF
* Add upload errors for test task id and participant id being too long

---

## Version 20.1.0
_14 January 2019_

### New Features
* Change surveillance and sed report filenames to include timestamp when the file was last generated
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
* Removed /notifications endpoints

### New Features
* Add retired flag to ACBs and ATLs to replace the functionality that previously used the deleted flag
* Add Quartz job to require all users to change password on next login
* Update email notification about potential Developer ban to include:
  * Reason for status change
  * Reason for listing change
* Prevent users from using the following macra measures which are under review: RT13 EH/CAH Stage 3, RT14 EH/CAH Stage 3, RT15 EH/CAH Stage 3 

### Bugs Fixed
* Do not show error for 2014 listings on upload or edit if they attest to g3, have ICS = true, and do not have any criteria marked as SED
* Fix issue with determining whether there is additional software associated with the certified product

---

## Version 19.2.0
_3 December 2018_

### New Features
* 2015 Functionality testing is restricted by criteria
* Save 'reason' for developer's status change in questionable activivty
* Output 'reason' for developer's status change on questionable activity report
* When uploading a listing, remove duplicate G1 and G2 macra measures and provide a warning
* Implement user ability to reset their password using a unique link
* Change /cache_status endpoint to report "OK" status only when all pre-loaded caches have completed
* Add "user must reset password on next login" workflow
  * Gives error when user tries to log in and needs to change password
  * Adds end point to change_expired_password
  * Update User edit to allow admins to require password change
* Add API Documentation file upload and download endpoints

---

## Version 19.1.0
_19 November 2018_

### New Features
* Update ONC contact information for CHPL API and on user invitation emails
* Check various site counts in surveillance to make sure they are reasonable
* Refresh the listing collection cache on demand when data has changed vs at timed intervals

### Bug Fixes
* Fix dependency injection problem that prevented implementing security in the chpl-service classes

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
* Removed deprecated /certified_products/meaningful_use/upload. Use /meaningful_use/upload instead

### New Features
* Added "last modified date" for surveillance and non-conformities to download files
* Move generate chart data app to quartz

### Bugs Fixed
* Fix creation of public announcement ignoring 'public' checkbox
* Fix incorrect errors for changing sed to false for a criteria
* Modified the Summary Statistics Report email
  * Re-arranged the order of some headings
  * Added totals for Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2014 Listings
  * Added totals for Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2015 Listings
  * Added active ACBs (when broken out) where the total for the ACB is 0
* Allow editing of meaningful use user count and history of muu counts for individual listings

### Bugs Fixed
* Fix creation of public announcement ignoring 'public' checkbox
* Fix incorrect error messages when editing SED = false for a criteria

---

## Version 17.0.0
_8 October 2018_

### Major Change
* When retrieving Pending Surveillances, the listing details will no longer be populated

### New Features
* Move surveillance download file generation into quartz job to allow for better job management
* Move SED G3 download file generation into quartz job for allow for better job management
* Added scheduled job to send warning to users who have not used thier API key in the last X days
* Added scheduled job to delete api keys that have not been used in X days
* Added LastUsedDate and DeleteWarningSentDate to the ./key endpoint return value(s)
* Add password strength checker implementing [a java fork](https://github.com/nulab/zxcvbn4j) of [zxcvbn](https://github.com/dropbox/zxcvbn) when anyone:
  * creates a new User
  * updates a password
* Add Developer Status to search results view
* Catch any unexpected error that occurs during a listing upload. Allow the error to be emailed to CHPL team if desired

### Bugs Fixed
* Invalid test functionality names are removed and an error is given to the user for 2014 and 2015 uploads
* Fix questionable activity report end of the month date rollover

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
* Move questionable activity email code into quartz job to allow user scheduling

---

## Version 16.3.0
_10 September 2018_

### New Features
* Add required reason business logic for developer bans
* Add API throttling
* Moved the Quartz scheduler to run in the Tomcat context
* Each Quartz job writes to a separate log file
* Add Open Surveillance Activities by ACB in the Statistics Report
* Add Open Nonconformities by ACB in the Statistics Report
* Return info messages for CMS ID missing criteria

### Bugs Fixed
* Extra test data/test procedure/test functionality/etc. no longer appears after attesting to a criteria with previously entered phantom data
* Fixed the "basic" certified product service to return the correct additional software code in the CHPL Product Number
* Fixed XML generation process to properly output `<tasks></tasks>` tag - was previously outputing as `<></>`
* Use "full name" and "friendly name" for users/contacts
* Fix reason required error to show again for certain listing updates
* Fix possibility of multiple developer status edits happening at the same time

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
* Identify questionable activity based on the confirmed date of a listing rather than the certification date
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
* Require start and end data parameters on all /activity API calls that are not for a single item

### New Features
* Added JavaMelody to provide Tomcat performance monitoring page
* Restrict certain /activity data to admin, acb, atl, or cms users as appropriate
* Update text describing /download endpoint

---

## Version 15.16.0
_18 July 2018_

### New Features
* Legacy 2014 listings (those with product numbers like "CHP-") are allowed to have attested to 170.314 (g)(3) but not required to have a criteria with SED and vice versa

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
* Logs for individually run apps will now show up in separate files under the logs directory
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
* Added check for 2015 listings to ensure if they have attested to G1 or G2 that they also have listed G1 or G2 macra measures for at least one criteria
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
* Handle blank or null test tool version in listing updates

---

## Version 15.13.0
_4 June 2018_

### New Features
* Added new service /activity/corrective_action_plans to return just legacy corrective action plan activities
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
* Handle SED boolean parsing and UCD Process existence mistmatch for 2014 upload

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
* Respect configured questionable activity window to allow a user to make edits that do not get logged as questionable activity for a short amount of time after a listing's certification date
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
* Look for required reason for certain questionable activities; error on those actions if a reason is not found
* Add reason to questionable activity report
* Add reference of pending listing to confirmed listings to enable tracking between pending and confirmed
* Add certification status change reason to listing activity entities and to questionable activity report
* Use most recently generated CMS ID if multiple exist with same product list set

---

## Version 15.4.3
_1 February 2018_

### Tiny features
* Do not require UCD Processes for 2014 criteria with SED true if the listing ICS is false
* Do not require a reason for listing certification status change

---

## Version 15.4.2
_25 January 2018_

* Do not allow users to update listings certification status history if they remove the original Active status

---

## Version 15.4.1
_19 January 2018_

### Bugs Fixed
* Save g1 and g2 success values for new 2014 listings with 170.314 (a)(4) criteria

---

## Version 15.4.0
_17 January 2018_

### Major features
* Add new surveillance upload job type; process large upload files as background jobs

### Minor features
* Allow reason for certification status change for any listing. Require reason for certification status change if new status is Withdrawn by ONC-ACB
* Stop using 'certificationStatus' field; instead use "latest" of the certificationEvents array for current status

### Bugs Fixed
* Statistics correctly account for deleted listings/unique products

---

## Version 15.3.0
_2 January 2018_

### Minor features
* Made ICS Source required for 2015 w/ICS on upload/confirm/edit
* Test tools are required for 170.315 (b)(8)
* GAP is required for 170.314 (b)(5)(B)
* Add warning messages to uploaded listings and surveillance regarding invalid characters found
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
* Do not allow duplicate QMS Standards or UCD Processes to be added
* If GAP is not specified for 170.314 (b)(5)(B) a warning will be returned instead of an error

---

## Version 15.1.0
_5 December 2017_

### Minor Features
* Added /data/test_data service to get all criteria with allowable test data
* Added /data/test_procedure service to get all criteria with allowable test procedure
* Use new test data values for upload and validation of listings
* Use new test procedure values for upload and validation of listings
* Support 2015 upload template v12

### Bugs Fixed
* Surveillance upload fixed to match on criteria number without space between number and letters

---

## Version 15.0.0
_20 November 2017_

### Backwards compatibility breaking features
* Re-wrote API search code. See documentation for the HTTP GET /search call for information

### Bugs Fixed
* Fix 60 day date range check for activity when date range overlaps daylight savings

---

## Version 14.2.1
_07 November 2017_

### Bugs Fixed
* 170.315 (b)(8) is not required to have test procedures or test tools. Will become required on the Nov 20 push

---

## Version 14.2.0
_06 November 2017_

### Minor Features
* Add certified_products/sed_details endpoint that returns the latest SED all Details document
* Add new version of 2014 upload template that adds a GAP column for 170.314 (b)(5)(B)
* Change warning message for transparency attestation saving
* Re-work handling of questionable activities. Do not send an email each time questionable activity appears, but save it all and send as a weekly report to subscribers instead

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
* Return HTTP 404 for /certified_products/{id}/details if listing has never existed or has been deleted
* Change any URL with an ID in the path to return 404 if that ID is not found
* Allow all URLs to be accessed with or without a trailing slash ('/')

### Bug Fixes
* Do not show macra measures for 2014 listings

---

## Version 13.1.0
_25 September 2017_

### Minor features
* Speed up API call to retrieve a Listing's entire ICS family
* Add /data/job_types call to get list of possible background job types that can be run
* Add background job processing and apply it to meaningful use user upload
* Update 'cleanupXml' script to allow for some parameters

### Bug Fixes
* Save ICS family data when going through pending listing confirm workflow

---

## Version 13.0.0
_11 September 2017_

### Major Features
_Backwards compatibility breaking changes_
* Reorganize SED data within the listing details request and response. UCD and Test tasks/participants are now located under a "sed" field. Listing update code has changed significantly as a result

### Minor Features
* Add warnings for 'phantom' criteria data where criteria is not attested to but has associated data from the upload
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
* Enforce ICS codes with 2 numbers (00-99) instead of just 1 number
* Improve error messages returned for unknown user-entered values for SED age and education levels
* Use subscriptions for weekly statistics email and questionable activity emails

## Bugs Fixed
* Summary statistics CSV should show same day of week that script was run
* Fixed summary statistics incorrect email counts

---

## Version 12.3.0
_31 July 2017_

## Minor Features
* Improve capturing of database errors on listing upload or update
* Improve error handling for invalid length/format codes in the CHPL unique product id
* Send a cache-cleared header when the search cache has been evicted
* Eliminate on-demand listing search cache eviction
* Add timed refresh of listings search

---

## Version 12.2.0
_17 July 2017_

## Minor Features
* Added /collections/developers API call to return a list of all developers, attestations, urls, and counts of their listings
* Completed bulk reject of pending surveillance
* Pulling pending surveillance validation out of a db table instead of calculating on the fly to speed up large uploads

## Bug Fixes
* Fixed incorrect contact name displayed if pending surveillance or listing had already been confirmed/rejected
* Only evict basic search cache if listing create/update methods return successfully

---

## Version 12.1.0
_3 July 2017_

## Minor Features
* Improve /update API call speed. Selectively update only items that have changed rather than doing a replace on the entire listing
* Allow mass reject of pending surveillances

---

## Version 12.0.0
_3 July 2017_

### Major Features
_Backwards compatibility breaking changes_
* Changed ICS field in certified product details object (used on get and update calls) from a boolean to a more complex object to include whether something was inherited and a list of parents and children

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
* Removed /decertifications/certified_products and /decertifications/inactive_certificates API calls. This same data can now be accessed through the /certified_products API method

### Minor Features
* Add decertification date, numMeaningfulUse, transparency attestation url, and api documentation url fields to the flat search results
* Fill in blank cells for subelements in nonconformtiy and surveillance downloads

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
* Disallow duplicate test standards per certification criterion
* Check for exisitng test standard in the listing's edition before adding a new one (eliminates repeated entries in the database and keeps the selection dropdowns manageable)
* Update all test standard entities, dtos, and domain objects to include certification edition
* Use error message file for error messages and support internationalization

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
* Add certification edition to test functionality and check that the right edition of test functionality is used during listing edit and upload

---

## Version 9.1.0
_24 April 2017_

### Features Added
* Update parameters for HTTP GET /search call. All the same options that were previously available only to POST exist for the GET. Updated related API documentation
* Add /cache_status endpoint. Returns a status of OK or INITIALIZING based on whether the basic search cache has been loaded
* Add statistics to ONC weekly email body and csv file
* Add endpoint /{productId}/split to allow splitting of products and moving versions to the old or new product
* Add optional contact information for a product that can be changed by ONC or ACB Admins
* Add optional showDeleted flag to the /data/search_options call. Defaults to false but setting to true will include deleted ACBs in the response

### Bugs Fixed
* Fix error saving developer with updated contact information

---

## Version 9.0.0
_10 April 2017_

### Features Added
* Show error to ONC-ACB during upload and edit when Surveillance Activity has a nonconformity with an entry for "Date Corrective Action Plan Was Approved" but no entry for "Date Corrective Action Plan Must Be Completed"
* Show error to ONC-ACB during upload and edit for a Surveillance Activity nonconformity that violates business rules for "Corrective Action Plan End Date"
* Add statusEvents field to developers to track past status changes (Active, Suspended by Onc, etc) and the date on which each status change occurred. Provide create, read, update, and delete API functionality for developer status changes
* Add decertificationDate to the data returned in the banned developers API call
* Updated basic search objects that get returned:
  * remove has open surveillance, has closed surveillance, has open nonconformities, has closed nonconformities
  * add surveillanceCount, openNonconformityCount, closedNonconformityCount fields
* Cache results of /certified_products/pending to improve view time by ~683%
* Add Surveillance Friendly ID to Surveillance (Basic) CSV file download
* Add optional "fields" parameter to /certified_products call to allow only sending back a custom list of fields
* Add nonconformity status to daily and weekly surveillance broken rules reports. Only include values in the reports with Open nonconformities

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
* No longer use ACLs that were getting added to each pending product x ACB Admin

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
* Add daily and weekly surveillance oversight reports to calculate which surveillance items have broken a given set of rules
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
* Add API methods for surveillance-related acivities. This includes upload, confirm, and reject for the bulk upload of surveillance data. It also includes create, update, and delete for singular changes to surveillance. Surveillance and associated non-conformities are returned with certified product details
* Add API call to support updating certified_product meaningful_use_user counts with a CSV upload in CMS Management
* Change behavior if certified product is marked as suspended or terminated by ONC. These statuses also result in a developer status change and require ROLE_ADMIN
* Add API call to get decertified developers with developer name, the developer's associated ONC_ACBs, the developer status, and the sum of the developer's estimated number of meaningful use users for all certified products associated with the developer
* Add API call to get decertified certified products with pageCount set to the total number of decertified products
* Add certificationDateStart and certificationDateEnd as advanced search parameters
* Change corrective action plan search parameters to new surveillance search parameters - *NOT BACKWARDS COMPATIBLE*
* Send questionable activity email when product owner changes (not during a merge)
* Add /surveillance/download[?type=all|basic|nonconformities] endpoint to allow download of CSV file with all surveillance and download of CSV file with surveillance that resulted in nonconformities
* Add lookup to new tables for certification status event history
* Add date of last certification status change to certified product details
* Add script cleanupXml.sh to remove download files older than 30 days except for the first of each month. Needs to be given executable permission and have a cron job set up
* Add decertification date to certified product details *CHANGES XML DOWNLOAD FORMAT*

### Bugs Fixed
* Changed transition of care calculations for the EHR Certification ID to more closely match the rule

---

## Version 6.0.1
_17 November 2016_

### Bugs Fixed
* Mark all DAO methods that return certified product details as transactional so they can retrieve the product owner history. Fixes issue with summary email being sent and with xml download not being generated

---

## Version 6.0.0
_15 November 2016_

### Features Added
* Add ability to retrieve and edit product ownership history (which developers a product has previously been owned by). BREAKS BACKWARDS COMPATIBILITY when retrieving certified product details
* Add "showDeleted" parameter to /developers call. Defaults to false

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
* Added "format" parameter to the download API endpoint which can be blank and will default to xml. Either xml or csv may be specified
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
* Changed certification status names previously known as Suspended, Terminated, and Withdrawn
* Created Java program to send weekly email with aggregate counts for developers, products, certified products, and 2014 + 2015 CPs
	* weekly email will be sent to weekly addresses defined in summary email
	* [TEMPLATE] for deployment, update the properties.environment to add summaryEmail property
* Updated /certification_ids/ call. Still returns the list of certification_ids and the date each was created. If user is ROLE_ADMIN, also returns a semi-colon separated list of CHPL product numbers with each certification_id
* Remove terms of use and api documentation from certified products
* Check product code, version code, and ICS code against specific sets of characters to make sure no special characters are used
* Add retired flag for test tools. Do not allow products to change associations with retired test tools
* Add script to generate weekly summary email that is run as scheduled cron job

---

## Version 5.0.0
_19 September 2016_

### Features added
* Add optional argument 'edition' to /download call
  * Generate the chpl-all file as well as a chpl-{edition} file for each edition present in the database for download
  * Downloads chpl-all by default
* Allowed c3/c4 to be connected to CQMs
* Added error messages for c1/2/3/4-CQM mismatch
* Re-factored Certification Id web services to improve performance
* Added statuses object that shows aggregate number of certified products associated with each developer and product. These objects will allow the website search page to filter on a developer/product's number of certified products that are active/retired/withdrawn/suspended/terminated
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
* Added a new service certification_ids/all to generate JSON with two fields - the certification ID and the date created. This includes all certification IDS ever and could be large
* Change all /activity calls that used to accept a lastNDays parameter to accept start and end parameters instead. Start and end are longs and treated as timestamps. (Not backwards compatible)
* Set up log4j2 and set hopefully appropriate log levels
* Updated /activity API endpoint to incorporate new parameters to filter by API-Key, sort dateAscending, and filter by start & end date
* Do not allow 170.315 (d)(3) to mark GAP as true
* Added 'responsibleUser' field with all user data for /activity reports
* Removed CORSFilter in web.xml; this was preventing some ajax calls from other domains
* Add developer object to /activity/product calls using the developer present in "newData"

### Bugs fixed
* Upload field values for SED parsed as integers but entered as floats were not saved. Fixed
* Task success avg was getting mixed up with Task errors avg
* Task time deviation optimal avg was getting mixed up with task path deviation optimal avg
* Test Task Participant Product & Professional experience was getting mixed up

---

## Version 3.0.0
_10 August 2016_

### Features Added
* By default do not return retired products. Still allow searching for retired products
* Remove visibleOnChpl filter from queries (not backwards compatible)
* Adjust developer merge code to automatically select transparency attestations for each ACB/Developer, throwing an error if an ACB has inconsistent attestations already defined for the developers being merged
* Add descriptions to CQMs in /details results
* Add descriptions to test standards and test functionality in /data/test_functionality
* For criteria that are eligible for and meet GAP, test tools, test procedure, and test data are not required
* ICS is no longer required when editing 2014 products
* Added optional inclusion of criteria met for Certification ID details through the API

---

## Version 2.0.0
_2 August 2016_

### Features Added
* The searchTerm parameter of a certified product search may now be a CHP- id or a 9-part unique ID or an ACB certification ID number. Wildcards are not supported
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
* Editing a product was requiring g1 and g2 when it should not have
* Trim spaces from the ends of all fields in the upload file
* Properly save test functionality and test tools if an invalid one was in the upload file but was edited to be a valid one during confirm
* Fix logging bug when invalid column header is in upload file

---

## Version 1.4.0
_16 May 2016_

### Features Added
* Added EHR Certification ID rest service
* Rearranged data for the corrective action plan reports/activity so there is just one activity event per CAP change
* Added EHR Certification ID rest service verify operation

### Bugs fixed
* Fix parsing error when spreadsheet has only a single row
* Only return last N days of activity as requested
* Fix 2015 upload file validation to catch missing UCD Process, test tasks, and test participants
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
* Fix 'null' in developer report when transparency attestation is changed
* Standards tested against are now saved
* Functionality tested against is now saved
* Fix error related to merging of products
* Product ownership change shows up correctly in activity report.`
* Do not allow duplicate chpl ids in the same file unless they have XXXX (new developers)

---

## Version 1.1.0
_12 April 2016_

### Features Added
* Test tools are not required for 2014 g1 and g2
* Test data is optional in 2015 for everything except g1 and g2
* Improved education level name matching and error messages for education levels that are not in the database
* Allow portions of CHPL Product Number to be changed
* Allow ACB_ADMIN role to change the name of their own ACB
* Add service for version activity
* Trigger emails on version name change, product name change, developer name change, or certified product status change

### Bugs fixed
* Test tool version is now saved
* Fix activity report link email
* Allow certification date to be changed

---

## Version 1.0.0
_30 March 2016_

### Features added
* Check for questionable certified product updates and trigger email if found
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
* Added descriptions to API methods
* Changed API for certified products and certification results to match 2014 data requirements
* Allow multiple values for the 'hasCap' field in search

---

## Version 0.2.0
_3 February 2016_

### Features added or Updated
* Set up swagger API and endpoint annotations
* Added code to create the XML download file
* Added code to allow a currently logged in user to get additional permissions if invited (/users/authorize)
* Adjust what happens when an ACB gets deleted. The ACB is marked as deleted and any users who were ONLY associated with that ACB (no other ACBs, no ATLs, no ADMIN role) will have their account disabled
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
* Added terms of use and api documentation to the certified product apis
* Added ability to update transparencyAttestation field per vendor and ACB combination. ADMINs can update the transparencyAttestation for all vendor/ACB combinations and anyone else can only update that field for the ACBs to which they have access
* Added transparencyAttestation to the fields that come back with certified product details and search results. It is inferred from the vendor/ACB mapping
* Added APIs for testing labs, found under /atls urls
* Complete CHPL Product Number for 2015 products with testing lab code

Bugs Fixed
* Correct CHPL number is inserted in corrective action plan documentation activity
* Authorizing existing users for new roles or ACBs/ATLs is fixed

---

## Version 0.0.2
_7 December 2015_

Features added or Updated
* Updated the format activity streams for users are returned
* Updated search options to remove references to 2011 edition data in UI
* Added Api key requirement and Api key activity logging
* Updated search to allow search by "hasCAP" (search by Corrective Action Plan)

Bugs fixed
* ETL inserts to correct 170.314 (b)(5)(A) or (b)(5)(B) depending on Ambulatory/Inpatient
* ETL correctly inserts CMS9 and CMS26 CQMs

---

## Version 0.0.1
_13 November 2015_

First release


## Version 15.14.0
_Date TBD_

### New Features
* Updated the XML and CSV file creation batch process to retrieve data asynchronously
* Added check for 2015 listings to ensure if they have attested to G1 or G2 that they also have listed G1 or G2 macra measures for at least one criteria.
* Added Quartz Scheduler component
  * Includes API endpoints for GET/PUT/POST/DELETE of schedule Triggers
  * Converted Cache Status Age app to Cache Status Age Quartz Job
* Modified retrieval of test functionalities to return practice type
* Updated validation of 2014 certified product to ensure that the test functionality is valid for the certified product's practice type
* Updated validation to ensure (g)(3) certification is valid iff at least one criteria attests to SED

### Bug fixes
* Fix a bug that allows ROLE_ADMIN to see pending certified products and surveillances

---

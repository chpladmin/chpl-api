
## Version 16.2.1
_Date TBD_

### New Features
* Add nonconformity charts statistics endpoint
* Identify questionable activity based on the confirmed date of a listing rather than the certification date.
* Add "Trigger Developer Ban" notification
* Migrate "Summary Statistics" to Quartz
  * Added new Quartz job to gather and store Summary Statistics
  * Added new user schedulable job that sends the Summary Statistics Email
  * Updated manager to support ACB specific Jobs
* Migrate "Broken Surveillance Rules" report to Quartz
  * Added new Quart job to gather and store surveillance error data
  * Added new user schedulable job to send the error email
* Add API throttling
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

### Bugs Fixed
* Fixed the "basic" certified product service to return the correct additional software code in the CHPL Product Number	
	
---

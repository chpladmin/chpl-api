
## Version 15.16.0
_Date TBD_

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
    * Criteria will no be structured: `<criteriaList><criteria/><criteria/> ... </criteriaList>`

---


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
* Migrate "ICS errors report" to Quartz
  * Added new Quartz job to gather and store ICS error data
  * Added new user schedulable job that sends the Summary Statistics Email
  * Updated manager to support ACB specific Jobs

---

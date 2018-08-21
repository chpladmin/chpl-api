
## Version 16.2.0
_Date TBD_

### New Features
* Migrate "ICS errors report" to Quartz
  * Added new Quartz job to gather and store ICS error data
  * Added new user schedulable job that sends the Summary Statistics Email
  * Updated manager to support ACB specific Jobs
* Migrate "Broken Surveillance Rules" report to Quartz
  * Added new Quart job to gather and store surveillance error data
  * Added new user schedulable job to send the error email

### Bugs Fixed
* Extra test data/test procedure/test functionality/etc. no longer appears after attesting to a criteria with previously entered phantom data.

---


## Version 16.3.0
_Date TBD_

### New Features
* Add required reason business logic for developer bans.
* Add API throttling
* Moved the Quartz scheduler to run in the Tomcat context
* Each Quartz job writes to a separate log file

### Bugs Fixed
* Extra test data/test procedure/test functionality/etc. no longer appears after attesting to a criteria with previously entered phantom data.
* Fixed the "basic" certified product service to return the correct additional software code in the CHPL Product Number	
* Fixed XML generation process to properly output `<tasks></tasks>` tag - was previously outputing as `<></>`.
* Use "full name" and "friendly name" for users/contacts

---

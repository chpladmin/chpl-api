
## Version 21.9.0
_Date TBD_

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
